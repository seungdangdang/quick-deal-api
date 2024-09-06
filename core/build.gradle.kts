class Versions {
    companion object {
        const val FLYWAY = "10.10.0"
    }
}
plugins {
    id("java")
}

group = "com.quickdeal"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":purchase"))
    implementation(project(":product"))
    implementation(project(":user"))
    implementation(project(":auth"))
    implementation(project(":scheduler"))
    implementation("org.flywaydb:flyway-core:${Versions.FLYWAY}")
    implementation("org.flywaydb:flyway-mysql")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
}

tasks.bootJar {
    enabled = true
}