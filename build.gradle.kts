import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
}

group = "pl.crejk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion = "1.3.1"
val kotestVersion = "4.2.0"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.springframework.security:spring-security-crypto:5.3.4.RELEASE")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.5")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("io.ktor:ktor-thymeleaf:$ktorVersion")

    implementation("commons-codec:commons-codec:1.14")
    implementation("org.litote.kmongo:kmongo-coroutine:4.1.1")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
    jvmTarget = "1.8"
}
