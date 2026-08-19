plugins {
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("jacoco")
    java
}

allprojects {
    group = "com.yurupari"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "jacoco")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    jacoco {
        toolVersion = "0.8.14"
    }

    tasks.withType<JacocoReport> {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
        }

        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/aspect/**", 
                        "**/annotation/**", 
                        "**/config/**", 
                        "**/dto/**", 
                        "**/exception/**",
                        "**/OpenApiConfig*"
                    )
                }
            })
        )
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        extensions.configure<JacocoTaskExtension> {
            includes = listOf("com.yurupari.*")
        }
        finalizedBy(tasks.jacocoTestReport)
    }
}
