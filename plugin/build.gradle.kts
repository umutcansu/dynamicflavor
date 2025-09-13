
plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    id("com.gradle.plugin-publish") version "1.2.1"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "io.github.umutcansu.dynamicflavor"
version = "1.0.15"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(gradleApi())

    implementation("com.android.tools.build:gradle:8.4.1")

    implementation("com.google.code.gson:gson:2.10.1")
}

java {
    withJavadocJar()
    withSourcesJar()
}

mavenPublishing {
    pom {
        name = "Dynamic Flavor Plugin"
        description = "A Gradle plugin that dynamically creates Android Product Flavors from a JSON source."
        url = "https://github.com/umutcansu/Resolved-Artifacts-Exporter"
        licenses {
            license {
                name.set("The MIT License")
                url.set("http://www.opensource.org/licenses/mit-license.php")
            }
        }
        developers {
            developer {
                id = "umutcansu"
                name = "Umut Cansu"
                email = "umutcansu@gmail.com"
            }
        }
        scm {
            url = "https://github.com/umutcansu/Resolved-Artifacts-Exporter"
            connection = "scm:git:git://github.com/umutcansu/Resolved-Artifacts-Exporter.git"
        }
    }

    signAllPublications()
}

mavenPublishing {
    //publishToMavenCentral(false)
}

publishing {
    repositories {
        maven {
            name = "Nexus"
            url = uri("http://localhost:8081/repository/maven-releases/")
            isAllowInsecureProtocol = true
            credentials {
                username = project.findProperty("nexusUser") as? String
                password = project.findProperty("nexusPassword") as? String
            }
        }
    }
}

gradlePlugin {
    website = "https://github.com/umutcansu"
    vcsUrl = "https://github.com/umutcansu/DynamicFlavor.git"
    plugins {
        create("dynamicFlavorPlugin") {
            id = "io.github.umutcansu.dynamicflavor"
            implementationClass = "io.github.umutcansu.dynamicflavor.DynamicFlavorPlugin"

            displayName = "Android Dynamic Flavor Plugin"
            description = "A Gradle plugin that dynamically creates Android Product Flavors from a JSON source."
            tags = listOf("android", "flavor", "flavors", "productflavor", "dynamic", "variant", "automation", "json", "config")

        }
    }
}