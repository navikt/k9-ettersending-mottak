import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val dusseldorfKtorVersion = "1.5.0.8b77f1d"
val ktorVersion = ext.get("ktorVersion").toString()
val kafkaEmbeddedEnvVersion = "2.2.0"
val kafkaVersion = "2.3.0" // Alligned med version fra kafka-embedded-env

val mainClass = "no.nav.helse.K9EttersendingMottakKt"


plugins {
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

buildscript {
    apply("https://raw.githubusercontent.com/navikt/dusseldorf-ktor/8b77f1d53e98bad7c081c3463871021bb8edc51f/gradle/dusseldorf-ktor.gradle.kts")
}

repositories {
    maven("http://packages.confluent.io/maven/")
    jcenter()
    mavenLocal()
    mavenCentral()

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/navikt/dusseldorf-ktor")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}


dependencies {
    // Server
    compile("no.nav.helse:dusseldorf-ktor-core:$dusseldorfKtorVersion")
    compile("no.nav.helse:dusseldorf-ktor-jackson:$dusseldorfKtorVersion")
    compile("no.nav.helse:dusseldorf-ktor-metrics:$dusseldorfKtorVersion")
    compile("no.nav.helse:dusseldorf-ktor-health:$dusseldorfKtorVersion")
    compile("no.nav.helse:dusseldorf-ktor-auth:$dusseldorfKtorVersion")

    // Client
    compile("no.nav.helse:dusseldorf-ktor-client:$dusseldorfKtorVersion")
    compile("no.nav.helse:dusseldorf-oauth2-client:$dusseldorfKtorVersion")

    // Kafka
    compile("org.apache.kafka:kafka-clients:$kafkaVersion")

    // Test
    testCompile("no.nav:kafka-embedded-env:$kafkaEmbeddedEnvVersion")
    testCompile("no.nav.helse:dusseldorf-test-support:$dusseldorfKtorVersion")
    testCompile("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
    testCompile("org.skyscreamer:jsonassert:1.5.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}


tasks.withType<ShadowJar> {
    archiveBaseName.set("app")
    archiveClassifier.set("")
    manifest {
        attributes(
            mapOf(
                "Main-Class" to mainClass
            )
        )
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.7.1"
}

tasks.register("createDependabotFile") {
    doLast {
        mkdir("$projectDir/dependabot")
        val file = File("$projectDir/dependabot/build.gradle")
        file.writeText( "// Do not edit manually! This file was created by the 'createDependabotFile' task defined in the root build.gradle.kts file.\n")
        file.appendText("dependencies {\n")
        project.configurations.getByName("runtimeClasspath").allDependencies
            .filter { it.group != rootProject.name && it.version != null }
            .forEach { file.appendText("    compile '${it.group}:${it.name}:${it.version}'\n") }
        project.configurations.getByName("testRuntimeClasspath").allDependencies
            .filter { it.group != rootProject.name && it.version != null }
            .forEach { file.appendText("    testCompile '${it.group}:${it.name}:${it.version}'\n") }
        file.appendText("}\n")
    }
}
