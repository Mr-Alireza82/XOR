// Standalone build: a pure Java 17 library.
// Published through JitPack: com.github.Mr-Alireza82:XOR:<tag>
plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.mr-alireza82"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // Hex <-> bytes <-> bits: https://github.com/Mr-Alireza82/Base-Conversion
    api("com.github.Mr-Alireza82:Base-Conversion:v1.0.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withSourcesJar()
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
