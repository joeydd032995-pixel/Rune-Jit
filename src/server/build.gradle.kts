plugins {
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    test {
        kotlin {
            // Parity tests live at repo root tests/parity/ per tests-parity.md rules
            srcDir("../../tests/parity")
        }
    }
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
    implementation("io.netty:netty-all:4.1.111.Final")
    implementation("org.slf4j:slf4j-api:2.0.13")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.6")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.6")
}

tasks.test {
    useJUnitPlatform()
    // Run tests from repo root so data/skills/woodcutting.yaml resolves correctly
    workingDir = rootProject.projectDir
}
