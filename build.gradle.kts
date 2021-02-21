import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
}

group = "pl.crejk"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

val ktorVersion = "1.5.1"
val kotestVersion = "4.2.5"
val vavrVersion = "0.10.2"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.springframework.security:spring-security-crypto:5.3.4.RELEASE")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.5")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("io.ktor:ktor-thymeleaf:$ktorVersion")

    implementation("io.vavr:vavr-kotlin:$vavrVersion")
    implementation("io.vavr:vavr-jackson:$vavrVersion")

    implementation("org.hashids:hashids:1.0.3")
    implementation("commons-codec:commons-codec:1.14")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}
