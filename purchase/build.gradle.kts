class Versions {
    companion object {
        const val REDIS = "3.2.4"
        const val KAFKA = "3.7.0"
        const val SPRING_KAFKA = "3.2.0"
        const val JJWT_API = "0.11.5"
    }
}

group = "com.quickdeal"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.jsonwebtoken:jjwt-api:${Versions.JJWT_API}")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:${Versions.REDIS}")
    implementation("io.lettuce:lettuce-core:6.4.0.RELEASE")
    implementation("org.springframework.kafka:spring-kafka:${Versions.SPRING_KAFKA}")
    implementation("org.apache.kafka:kafka-clients:${Versions.KAFKA}")
    implementation("org.apache.commons:commons-pool2")
    implementation(project(":common"))
    implementation(project(":product"))
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${Versions.JJWT_API}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${Versions.JJWT_API}")
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