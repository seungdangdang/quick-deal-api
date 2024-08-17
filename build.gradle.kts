class Versions {
    companion object {
        const val SPRING_BOOT = "3.3.2"
        const val LOMBOK = "1.18.28"
        const val QUERY_DSL = "5.0.0"
    }
}

plugins {
    id("java")
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.flywaydb.flyway") version "10.10.0"
}

allprojects {
    group = "com.boot"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    java.sourceCompatibility = JavaVersion.VERSION_17

    group = "com.boot"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web:${Versions.SPRING_BOOT}")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa:${Versions.SPRING_BOOT}")
        implementation("org.projectlombok:lombok:${Versions.LOMBOK}")
        implementation("com.querydsl:querydsl-jpa:${Versions.QUERY_DSL}:jakarta")
        annotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
        annotationProcessor("com.querydsl:querydsl-apt:${Versions.QUERY_DSL}:jakarta")
        annotationProcessor("jakarta.annotation:jakarta.annotation-api")
        annotationProcessor("jakarta.persistence:jakarta.persistence-api")
        implementation("mysql:mysql-connector-java:8.0.33")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    }
}
