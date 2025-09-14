## Android Dynamic Flavor Gradle Plugin
Turn Android Product Flavor management from a nightmare into a breeze.

Eliminate the repetitive, hard-to-maintain productFlavor blocks that clutter your build.gradle file. Dynamic Flavor is a powerful Gradle plugin that revolutionizes your Android build process by allowing you to manage your product configurations from a single, centralized JSON source.

Designed especially for projects with numerous white-label applications or complex flavor structures, this plugin promotes a "single source of truth" for your configurations. The result? A pristine build.gradle file, error-free and fast builds, and the freedom to add new flavors in seconds.

✨ Why Dynamic Flavor?
End Boilerplate Code: Replace hundreds of lines of productFlavor blocks with just a few elegant lines in the dynamicFlavor block.

Centralized Management: Manage all your flavors from a single JSON file. Adding a new client or environment is as simple as adding a new JSON object.

Error-Free Builds: Eliminate human errors that occur from manually copying and pasting configurations.

Fully Automated: Automatically handles complex settings like buildConfig fields, resValue resources, manifestPlaceholders, and flavorDimensions.

Limitless Flexibility: Whether you're working with a simple GET request or a complex API requiring POST and Authorization headers, fromCustomJson gives you the power to connect to any data source.

A Team Player: It works in perfect harmony with your existing, manually-defined flavors. You can use both dynamic and static configurations together.

🚀 Installation
Step 1: Add the Gradle Plugin Portal (settings.gradle.kts)
Ensure that gradlePluginPortal() is included in the repositories block of your pluginManagement section in your root settings.gradle.kts file. This is usually present by default in new projects.

```kotlin
// settings.gradle.kts (project)
pluginManagement {
    repositories {
        mavenCentral()
    }
}
```

```kotlin
// build.gradle.kts (app module)
plugins {
    id("io.github.umutcansu.dynamicflavor") version "1.0.0"
}
```

⚙️ Providing the JSON Data: The Four Data Sources
The Dynamic Flavor plugin is highly flexible, offering four distinct methods to provide your flavor configuration data. You must use one of these functions inside your dynamicFlavor block to specify the source of truth for your flavors.

1. fromFile("...")
Loads the configuration from a local file within your project. This is the recommended approach for most projects as it keeps the configuration version-controlled alongside your source code.

```kotlin
// build.gradle.kts
dynamicFlavor {
    fromFile("config/flavors.json")
    mapFlavorName(fromJson = "flavor_name")
}
```

2. fromJson("...")
Reads the configuration directly from a raw string inside your build.gradle.kts file. This method is ideal for very simple configurations or for quick tests without the need to create a separate file.

```kotlin
// build.gradle.kts
dynamicFlavor {
    fromJson("""
        [
          { "flavor_name": "test", "dimension": "qa" }
        ]
    """)
    mapFlavorName(fromJson = "flavor_name")
}
```

3. fromUrl("...")
Fetches the configuration from a remote URL using a simple GET request. This is useful for configurations hosted on a server, a CMS, or a service like GitHub Gist.

```kotlin
// build.gradle.kts
dynamicFlavor {
    fromUrl("https://api.example.com/android/flavors.json")
    mapFlavorName(fromJson = "flavor_name")
}
```

4. fromCustomJson { ... }
Provides ultimate flexibility for complex scenarios. This function accepts a block of Kotlin code that you write to fetch the data. Use it for POST requests, adding authentication headers, or any custom logic that needs to run to produce the final JSON string.


```kotlin
// build.gradle.kts
dynamicFlavor {
    fromCustomJson {
        // Your custom logic to fetch data, e.g., a POST request using OkHttp or HttpClient
        val jsonString = myCustomApiClient.getFlavors("android", "v2")
        jsonString // The block must return the final JSON as a String
    }
    mapFlavorName(fromJson = "flavor_name")
}
```

## Comprehensive JSON Template (for Automatic Mode)
Once you've chosen a data source, you need to structure your JSON. The following template contains all the standard keys that the plugin recognizes automatically. If your JSON follows this structure, you do not need to use the mappings block.

```json
[
  {
    "flavor_name": "pro",
    "dimension": "tier",
    "applicationIdSuffix": ".pro",
    "versionNameSuffix": "-pro",
    "versionCode": 200,
    "minSdk": 26,
    "targetSdk": 34,
    "signing": "release",
    "build_configs": [
      { "type": "String", "key": "API_URL", "val": "https://api.pro.server.com/v2/" },
      { "type": "boolean", "key": "PREMIUM_FEATURES_ENABLED", "val": true },
      { "type": "long", "key": "CACHE_EXPIRATION_MS", "val": 3600000 },
      { "type": "float", "key": "ANALYTICS_SAMPLING_RATE", "val": 1.0 }
    ],
    "resources": [
      { "type": "string", "key": "app_name", "val": "DynamicApp PRO" },
      { "type": "color", "key": "primary_color", "val": "#6A1B9A" },
      { "type": "bool", "key": "is_pro_build", "val": true },
      { "type": "integer", "key": "max_download_count", "val": 100 },
      { "type": "dimen", "key": "default_margin", "val": "24dp" }
    ],
    "manifest_values": [
      { "key": "appIcon", "val": "@mipmap/ic_launcher_pro" },
      { "key": "deepLinkHost", "val": "pro.dynamic.app" }
    ]
  },
  {
    "flavor_name": "demo",
    "dimension": "tier",
    "applicationIdSuffix": ".demo",
    "versionNameSuffix": "-demo",
    "versionCode": 150,
    "minSdk": 24,
    "targetSdk": 34,
    "signing": "debug",
    "build_configs": [
      { "type": "String", "key": "API_URL", "val": "https://api.demo.server.com/v2/" },
      { "type": "boolean", "key": "PREMIUM_FEATURES_ENABLED", "val": false }
    ],
    "resources": [
      { "type": "string", "key": "app_name", "val": "DynamicApp DEMO" }
    ]
  }
]
```

| Key | Description |
| :--- | :--- |
| **`flavor_name`** | (**Required**) Specifies the name of the `ProductFlavor` to be created. |
| **`dimension`** | Defines the dimension (group) to which the flavor belongs. |
| **`applicationIdSuffix`**| Appended to the application ID (e.g., `com.example.app.pro`). |
| **`versionNameSuffix`**| Appended to the version name (e.g., `1.0-pro`). |
| **`versionCode`**| Assigns a specific `versionCode` to the flavor. |
| **`minSdk`**| Assigns a specific `minSdk` level to the flavor. |
| **`targetSdk`**| Assigns a specific `targetSdk` level to the flavor. |
| **`signing`**| Specifies the name of a `signingConfig` defined in your `build.gradle` file to be used for signing this flavor. |
| **`build_configs`**| A list of fields to be added to the `BuildConfig.java` file. Each object must contain `key`, `type` (`String`, `boolean`, `int`, `long`, `float`, etc.), and `val`. |
| **`resources`**| Used to dynamically add resources under `res/values`. Each object must contain `key`, `type` (`string`, `bool`, `color`, `dimen`, `integer`, etc.), and `val`. |
| **`manifest_values`**| Used to populate placeholders (`${placeholder}`) in `AndroidManifest.xml`. Each object must contain `key` and `val`. |


## The Custom JSON Data (hybrid-flavors.json)
First, let's define our custom-structured JSON data. This file uses non-standard keys that will require our mappings block to parse correctly.

app/config/hybrid-flavors.json

```json
[
  {
    "id": "partnerX",
    "grouping": "whitelabel",
    "package_suffix": ".partnerx",
    "build_metadata": [
      {
        "name": "PARTNER_ID",
        "dataType": "String",
        "content": "px-12345"
      },
      {
        "name": "ENABLE_ANALYTICS",
        "dataType": "boolean",
        "content": true
      }
    ]
  },
  {
    "id": "partnerY",
    "grouping": "whitelabel",
    "package_suffix": ".partnery",
    "build_metadata": [
      {
        "name": "PARTNER_ID",
        "dataType": "String",
        "content": "py-67890"
      },
      {
        "name": "ENABLE_ANALYTICS",
        "dataType": "boolean",
        "content": false
      }
    ]
  }
]
```

Note the use of non-standard keys like id, grouping, and build_metadata.

```kotlin
// app/build.gradle.kts

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.github.umutcansu.dynamicflavor") version "1.0.0"
}

dynamicFlavor {
    // 1. Specify the data source as our custom JSON file.
    fromFile("config/hybrid-flavors.json")

    // 2. Specify that the flavor name will come from the 'id' key.
    mapFlavorName(fromJson = "id")

    // 3. Map the custom keys in the JSON to standard flavor properties.
    mappings {
        property("dimension", fromJson = "grouping")
        property("applicationIdSuffix", fromJson = "package_suffix")
        
        mapBuildConfigFields(fromListInJson = "build_metadata") {
            name(fromJson = "name")
            type(fromJson = "dataType")
            value(fromJson = "content")
        }
    }

    // 4. Apply manual configurations on top of ALL dynamically created flavors.
    allFlavors {
        // Add a common resource value to ALL dynamic flavors.
        resValue("string", "powered_by", "Dynamic Flavor Platform")

        // Add an extra buildConfigField ONLY for the flavor named 'partnerX'.
        if (name == "partnerX") {
            buildConfigField("boolean", "ENABLE_SPECIAL_PROMOTIONS", "true")
        }
    }
}

android {
    namespace = "com.example.hybridapp"
    compileSdk = 34

    defaultConfig {
        // ...
    }

    productFlavors {
        // 5. This is a manually-defined flavor.
        // The Dynamic Flavor plugin will NOT interfere with this block.
        create("staging") {
            dimension = "whitelabel" // Can share a dimension with dynamic flavors
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
        }
    }
}
```

# How It Works & The Result
When this configuration is synchronized, the following happens:

Data is Read: The plugin reads the hybrid-flavors.json file.

Mapping is Applied: The mappings block correctly translates the custom keys (id, grouping, etc.) for the partnerX and partnerY objects into ProductFlavor properties.

Manual Overrides are Executed: The allFlavors block runs for both partnerX and partnerY:

Both flavors receive the powered_by string resource.

The if (name == "partnerX") condition is met for the partnerX flavor, so it gets an additional buildConfigField named ENABLE_SPECIAL_PROMOTIONS set to true. The partnerY flavor does not get this field.

Manual Flavor is Preserved: The plugin respects the existing staging flavor.

After syncing, your Build Variants panel will contain flavors for partnerX, partnerY, and staging.

You can verify the result by building the partnerXDebug variant and checking its BuildConfig.java file—it will contain ENABLE_SPECIAL_PROMOTIONS = true;. The BuildConfig.java for the partnerYDebug variant will not contain this field.
