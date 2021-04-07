import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:10.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.4.30"
    id("org.jmailen.kotlinter").version("3.3.0")
}

group = "pl.crejk"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    jcenter()
}

val ktorVersion = "1.5.2"
val kotestVersion = "4.4.3"
val vavrVersion = "0.10.2"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("io.ktor:ktor-thymeleaf:$ktorVersion")

    implementation("io.arrow-kt:arrow-core-data:0.12.0")

    implementation("org.springframework.security:spring-security-crypto:5.3.4.RELEASE")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.5")
    implementation("commons-codec:commons-codec:1.14")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-ktor:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-arrow-jvm:$kotestVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xinline-classes")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
