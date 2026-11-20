// Standalone build: pure Java 17 library, checked against the Android API (minSdk 26, core library desugaring).
// Published through JitPack: com.github.Mr-Alireza82:XOR:<tag>
plugins {
    `java-library`
    `maven-publish`
    id("ru.vyarus.animalsniffer") version "2.0.1"
}

group = "com.github.Mr-Alireza82"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // Hex <-> bytes <-> bits: https://github.com/Mr-Alireza82/Base-Conversion
    api("com.github.Mr-Alireza82:Base-Conversion:v1.0.0")

    signature("com.toasttab.android:gummy-bears-api-26:0.16.0:coreLib2@signature")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withSourcesJar()
}

animalsniffer {
    sourceSets = listOf(project.sourceSets.main.get())
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 17
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
