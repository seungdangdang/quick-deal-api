class Versions {
    companion object {
        const val REDIS = "3.2.4"
        const val KAFKA = "3.7.0"
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
    implementation("org.springframework.boot:spring-boot-starter-data-redis:${Versions.REDIS}")
    implementation("org.apache.kafka:kafka-clients:${Versions.KAFKA}")
    implementation(project(":common"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}