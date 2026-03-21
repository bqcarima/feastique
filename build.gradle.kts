import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "2.2.20"
	kotlin("plugin.spring") version "2.2.20"
    kotlin("plugin.jpa") version "2.2.20"
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.qinet"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-crypto")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.security:spring-security-test")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.springframework.boot:spring-boot-starter-json")

	compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

	//Provides the Json Web Token functionality
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1") // JAXB API
	implementation("org.glassfish.jaxb:jaxb-runtime:3.0.1")       // JAXB runtime
	implementation("javax.activation:javax.activation-api:1.2.0") // Activation framework (optional)
	implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	implementation("com.google.code.gson:gson:2.10.1")

    // UUIDv7 generator
    implementation("com.github.f4b6a3:uuid-creator:5.3.2")

	// Mockito core + Kotlin extensions (mockito-kotlin)
	testImplementation("org.mockito:mockito-core:5.15.2")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

/*tasks.withType<Test> {
	jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}*/
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}