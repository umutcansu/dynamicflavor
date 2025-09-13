package io.github.umutcansu.dynamicflavor


import com.android.build.api.dsl.ProductFlavor
import org.gradle.api.Action
import org.gradle.api.provider.Property

abstract class FlavorConfig {

    abstract val fromUrl: Property<String>
    abstract val fromFile: Property<Any>
    abstract val fromJson: Property<String>
    abstract val fromCustomJson: Property<() -> String>

    internal abstract val flavorNameJsonKey: Property<String>
    internal val mappingConfig = MappingConfig()
    internal var manualAction: Action<ProductFlavor>? = null

    fun fromUrl(url: String) = this.fromUrl.set(url)
    fun fromFile(file: Any) = this.fromFile.set(file)
    fun fromJson(jsonString: String) = this.fromJson.set(jsonString)
    fun fromCustomJson(block: () -> String) = this.fromCustomJson.set(block)

    fun mapFlavorName(fromJson: String) {
        flavorNameJsonKey.set(fromJson)
    }

    fun mappings(block: MappingConfig.() -> Unit) {
        mappingConfig.apply(block)
    }
    fun mappings(action: Action<MappingConfig>) {
        action.execute(mappingConfig)
    }

    fun allFlavors(block: ProductFlavor.() -> Unit) {
        this.manualAction = Action { it.block() }
    }
    fun allFlavors(action: Action<ProductFlavor>) {
        this.manualAction = action
    }
}