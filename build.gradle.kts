plugins {
    id("java")
}

group = "org.siu"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.8.0")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("io.vavr:vavr:0.9.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("siulang")
    manifest {
        attributes["Main-Class"] = "org.siu.Main"
    }
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles a jar archive containing the main classes and all dependencies."
    archiveBaseName.set("siulang")
    archiveClassifier.set("fat")

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "org.siu.Main"
    }
}

tasks.register<Copy>("copyLog4jConfig") {
    from("src/main/resources/log4j2.xml")
    into("$buildDir/classes/java/main")
}

tasks.register<Copy>("copyFatJarLog4jConfig") {
    from("src/main/resources/log4j2-fatjar.xml")
    into("$buildDir/classes/java/main")
    rename { "log4j2.xml" }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("copyLog4jConfig")
}

tasks.named<Jar>("fatJar") {
    dependsOn("copyFatJarLog4jConfig")
}
