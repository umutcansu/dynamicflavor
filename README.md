## Dynamic Flavor Gradle Plugin
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
