package io.github.umutcansu.dynamicflavor

import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.ProductFlavor
import com.android.build.api.variant.AndroidComponentsExtension
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URL

class DynamicFlavorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("dynamicFlavor", FlavorConfig::class.java)

        val androidComponents = project.extensions.findByType(AndroidComponentsExtension::class.java)
        if (androidComponents == null) {
            project.logger.warn("DynamicFlavorPlugin: AndroidComponentsExtension not found. This plugin only works in Android projects.")
            return
        }

        androidComponents.finalizeDsl { androidExtension ->
            if (androidExtension !is ApplicationExtension) return@finalizeDsl

            val jsonContent: String? = when {
                extension.fromCustomJson.isPresent -> {
                    project.logger.lifecycle(">>> Data source: fromCustomJson (custom block)")
                    try {
                        val remoteBlock = extension.fromCustomJson.get()
                        remoteBlock() // Kullanıcının kod bloğunu çalıştır ve string sonucunu al
                    } catch (e: Exception) {
                        throw GradleException("The fromCustomJson block failed to execute.", e)
                    }
                }
                extension.fromJson.isPresent -> {
                    project.logger.lifecycle(">>> Data source: fromJson (direct string)")
                    extension.fromJson.get()
                }
                extension.fromFile.isPresent -> {
                    project.logger.lifecycle(">>> Data source: fromFile")
                    val file = project.file(extension.fromFile.get())
                    if (!file.exists()) throw IllegalStateException("Specified file not found: ${file.absolutePath}")
                    file.readText()
                }
                extension.fromUrl.isPresent -> {
                    project.logger.lifecycle(">>> Data source: fromUrl")
                    val url = extension.fromUrl.get()
                    try { URL(url).readText() } catch (e: Exception) { throw RuntimeException("Could not read data from URL: $url", e) }
                }
                else -> null
            }

            if (jsonContent == null) {
                project.logger.info("DynamicFlavorPlugin: No data source specified. Plugin is inactive.")
                return@finalizeDsl
            }
            if (!extension.flavorNameJsonKey.isPresent) {
                throw IllegalStateException("Flavor name key must be specified using the `mapFlavorName(fromJson = ...)` function.")
            }

            val remoteData = parseJsonToMap(jsonContent)
            val flavorNameKey = extension.flavorNameJsonKey.get()
            val mappingRules = extension.mappingConfig.rules.ifEmpty {
                createDefaultMappingRules()
            }

            if (androidExtension.buildFeatures.buildConfig != true) {
                val buildConfigRule = mappingRules.filterIsInstance<ListBuildConfigFieldRule>().firstOrNull()
                if (buildConfigRule != null && remoteData.any { it.containsKey(buildConfigRule.jsonListKey) }) {
                    project.logger.lifecycle("DynamicFlavorPlugin: BuildConfig fields found. Automatically enabling the buildConfig feature.")
                    androidExtension.buildFeatures.buildConfig = true
                }
            }

            remoteData.forEach { dataMap ->
                val flavorName = dataMap[flavorNameKey] as? String ?: return@forEach
                project.logger.lifecycle("DynamicFlavorPlugin: Creating flavor '${flavorName}'...")
                androidExtension.productFlavors.create(flavorName) {
                    mappingRules.forEach { rule ->
                        applyRule(project, it, rule, dataMap, androidExtension.signingConfigs)
                    }
                    extension.manualAction?.execute(it)
                }
            }

            val allDimensions = androidExtension.productFlavors.mapNotNull { it.dimension }.distinct()
            if (allDimensions.isNotEmpty()) {
                project.logger.lifecycle("DynamicFlavorPlugin: Dynamically added flavor dimensions: $allDimensions")
                androidExtension.flavorDimensions.addAll(allDimensions)
            }
        }
    }

    private fun applyRule(
        project: Project,
        flavor: ProductFlavor,
        rule: MappingRule,
        data: Map<String, Any>,
        signingConfigs: NamedDomainObjectContainer<out ApkSigningConfig>
    ) {
        when (rule) {
            is PropertyRule -> {
                val value = data[rule.jsonKey] ?: return
                val transformedValue = rule.transformer(value)
                when (rule.propertyName) { "dimension" -> flavor.dimension = transformedValue.toString() }
                if (flavor is ApplicationProductFlavor) {
                    when (rule.propertyName) {
                        "applicationIdSuffix" -> flavor.applicationIdSuffix = transformedValue.toString()
                        "versionNameSuffix" -> flavor.versionNameSuffix = transformedValue.toString()
                        "versionCode" -> flavor.versionCode = (transformedValue as? Number)?.toInt()
                        "minSdk" -> flavor.minSdk = (transformedValue as? Number)?.toInt()
                        "targetSdk" -> flavor.targetSdk = (transformedValue as? Number)?.toInt()
                        "signingConfig" -> {
                            val configName = transformedValue.toString()
                            signingConfigs.findByName(configName)?.let { flavor.signingConfig = it } ?: project.logger.warn("DynamicFlavorPlugin: A signing configuration named '${configName}' was not found.")
                        }
                    }
                }
            }
            is ListBuildConfigFieldRule, is ListResValueRule, is ListManifestPlaceholderRule -> {
                if (flavor is ApplicationProductFlavor) {
                    when(rule) {
                        is ListBuildConfigFieldRule -> applyListRule(data, rule.jsonListKey, rule.mapping) { type, name, value -> flavor.buildConfigField(type, name, formatBuildConfigValue(type, value)) }
                        is ListResValueRule -> applyListRule(data, rule.jsonListKey, rule.mapping) { type, name, value -> flavor.resValue(type, name, value.toString()) }
                        is ListManifestPlaceholderRule -> applyListRule(data, rule.jsonListKey, rule.mapping) { _, name, value -> flavor.manifestPlaceholders[name] = value }
                        else -> {}
                    }
                }
            }
        }
    }

    private inline fun applyListRule(data: Map<String, Any>, listKey: String, mapping: ListMappingConfig, action: (type: String, name: String, value: Any) -> Unit) {
        val list = data[listKey] as? List<*> ?: return
        list.forEach { item ->
            val itemMap = item as? Map<*, *> ?: return@forEach
            val type = itemMap[mapping.typeKey]?.toString()
            val name = itemMap[mapping.nameKey]?.toString() ?: return@forEach
            val value = itemMap[mapping.valueKey] ?: return@forEach
            action(type ?: "", name, value)
        }
    }

    private fun createDefaultMappingRules(): List<MappingRule> = listOf(
        PropertyRule("dimension", "dimension"),
        PropertyRule("applicationIdSuffix", "applicationIdSuffix"),
        PropertyRule("versionNameSuffix", "versionNameSuffix"),
        PropertyRule("signingConfig", "signing"),
        PropertyRule("versionCode","versionCode"),
        PropertyRule("minSdk","minSdk"),
        PropertyRule("targetSdk","targetSdk"),
        ListBuildConfigFieldRule("build_configs", ListMappingConfig("type", "key", "val")),
        ListResValueRule("resources", ListMappingConfig("type", "key", "val")),
        ListManifestPlaceholderRule("manifest_values", ListMappingConfig(nameKey = "key", valueKey = "val"))
    )

    private fun formatBuildConfigValue(type: String, value: Any): String = when (type.lowercase()) {
        "string" -> "\"$value\""
        "long" -> "${value}L"
        "float" -> "${value}f"
        else -> value.toString()
    }

    private fun parseJsonToMap(jsonContent: String): List<Map<String, Any>> = try {
        Gson().fromJson(jsonContent, object : TypeToken<List<Map<String, Any>>>() {}.type)
    } catch (e: Exception) {
        throw RuntimeException("An error occurred while parsing JSON data. Error: ${e.message}", e)
    }
}