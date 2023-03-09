repositories {
	mavenCentral()
}

plugins {
	kotlin("jvm") version "1.8.10"
	application
	id("org.openjfx.javafxplugin") version "0.0.13"
	kotlin("plugin.serialization") version "1.8.10"
	id("com.github.johnrengelman.shadow") version "8.1.0"
	id("com.github.ben-manes.versions") version "0.46.0"
}

dependencies {
	implementation("no.tornado:tornadofx:1.7.20")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.6.4")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

	implementation("io.insert-koin:koin-core:3.3.3")
	implementation("io.insert-koin:koin-logger-slf4j:3.3.1")

	implementation("org.jetbrains.exposed:exposed-core:0.41.1")
	implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
	implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
	implementation("com.h2database:h2:2.1.214")
	implementation("org.liquibase:liquibase-core:4.19.0")
	implementation("com.mattbertolini:liquibase-slf4j:4.1.0")

	implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
	implementation("ch.qos.logback:logback-classic:1.4.5")

	fun ktor(artifact: String) = "io.ktor:ktor-$artifact:2.2.4"
	listOf(
		"client-core",
		"client-cio",
		"client-logging",
		"client-content-negotiation",
		"serialization-kotlinx-json"
	).forEach {
		implementation(ktor(it))
	}

//	implementation("io.insert-koin:koin-test:3.3.3")
	testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
	testImplementation("io.kotest:kotest-assertions-core:5.5.5")
	testImplementation("io.kotest:kotest-property:5.5.5")
}

application {
	mainClass.set("allfit.AllFit")
}

javafx {
	version = "19.0.2.1"
	modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
}

//tasks {
//	named<ShadowJar>("shadowJar") {
//		archiveBaseName.set("shadow")
//	}
//}
//tasks {
//	build {
//		dependsOn(shadowJar)
//	}
//}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
	val rejectPatterns = listOf(".*-ea.*", ".*RC", ".*[Bb]eta.*", ".*[Aa]lpha.*").map { Regex(it) }
	rejectVersionIf {
		rejectPatterns.any {
			it.matches(candidate.version)
		}
	}
}
