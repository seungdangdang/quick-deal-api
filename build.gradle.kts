class Versions {
    companion object {
        const val SPRING_BOOT = "3.3.2"
        const val JACKSON = "2.15.2"
        const val LOMBOK = "1.18.28"
        const val FLYWAY = "10.10.0"
    }
}

plugins {
    id("java")
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.flywaydb.flyway") version "10.10.0"
}

group = "com.boot"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.SPRING_BOOT}")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:${Versions.SPRING_BOOT}")
    implementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    implementation("org.flywaydb:flyway-core:${Versions.FLYWAY}")
    implementation("org.flywaydb:flyway-mysql")
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.JACKSON}")
    implementation("com.fasterxml.jackson.core:jackson-core:${Versions.JACKSON}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:${Versions.JACKSON}")
    annotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
    implementation("mysql:mysql-connector-java:8.0.33")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
