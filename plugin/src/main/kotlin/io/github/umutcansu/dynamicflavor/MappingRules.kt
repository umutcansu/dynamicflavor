package io.github.umutcansu.dynamicflavor


import com.android.build.api.dsl.ProductFlavor
import org.gradle.api.Action

// ... Veri sınıfları (PropertyRule, ListMappingConfig vb.) aynı kalıyor ...
internal sealed interface MappingRule
internal data class PropertyRule(
    val propertyName: String,
    val jsonKey: String,
    val transformer: (Any) -> Any = { it }
) : MappingRule
data class ListMappingConfig(
    var typeKey: String = "type", var nameKey: String = "name", var valueKey: String = "value"
) {
    fun type(fromJson: String) { typeKey = fromJson }
    fun name(fromJson: String) { nameKey = fromJson }
    fun value(fromJson: String) { valueKey = fromJson }
}
internal data class ListBuildConfigFieldRule(val jsonListKey: String, val mapping: ListMappingConfig) : MappingRule
internal data class ListResValueRule(val jsonListKey: String, val mapping: ListMappingConfig) : MappingRule
internal data class ListManifestPlaceholderRule(val jsonListKey: String, val mapping: ListMappingConfig) : MappingRule


class MappingConfig {
    internal val rules = mutableListOf<MappingRule>()

    fun property(name: String, fromJson: String, transform: (Any) -> Any = { it }) {
        rules.add(PropertyRule(name, fromJson, transform))
    }

    fun mapBuildConfigFields(fromListInJson: String, block: ListMappingConfig.() -> Unit) {
        val config = ListMappingConfig().apply(block)
        rules.add(ListBuildConfigFieldRule(fromListInJson, config))
    }
    fun mapBuildConfigFields(fromListInJson: String, action: Action<ListMappingConfig>) {
        val config = ListMappingConfig()
        action.execute(config)
        rules.add(ListBuildConfigFieldRule(fromListInJson, config))
    }

    fun mapResValues(fromListInJson: String, block: ListMappingConfig.() -> Unit) {
        val config = ListMappingConfig().apply(block)
        rules.add(ListResValueRule(fromListInJson, config))
    }
    fun mapResValues(fromListInJson: String, action: Action<ListMappingConfig>) {
        val config = ListMappingConfig()
        action.execute(config)
        rules.add(ListResValueRule(fromListInJson, config))
    }

    fun mapManifestPlaceholders(fromListInJson: String, block: ListMappingConfig.() -> Unit) {
        val config = ListMappingConfig(nameKey = "key", valueKey = "val").apply(block)
        rules.add(ListManifestPlaceholderRule(fromListInJson, config))
    }
    fun mapManifestPlaceholders(fromListInJson: String, action: Action<ListMappingConfig>) {
        val config = ListMappingConfig(nameKey = "key", valueKey = "val")
        action.execute(config)
        rules.add(ListManifestPlaceholderRule(fromListInJson, config))
    }
}