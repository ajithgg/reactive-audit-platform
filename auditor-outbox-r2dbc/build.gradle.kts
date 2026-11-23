plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.2")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    api(project(":auditor-module"))
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("tools.jackson.core:jackson-databind")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.slf4j:slf4j-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<Test> {
    useJUnitPlatform()
}