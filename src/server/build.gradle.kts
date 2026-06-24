plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
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

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    // Run tests from repo root so data/skills/woodcutting.yaml resolves correctly
    workingDir = rootProject.projectDir
}
