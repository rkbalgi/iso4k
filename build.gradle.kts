import com.vanniktech.maven.publish.SonatypeHost

buildscript {

    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.19.0")
    }

}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.6.20")
    id("com.vanniktech.maven.publish").version("0.19.0")
}



repositories {
    mavenLocal()
    mavenCentral()
}

val jacksonVersion by properties
val logbackVersion by properties
val guavaVersion by properties
val projectVersion by properties

group = "io.github.rkbalgi"
version = projectVersion!!

java.sourceCompatibility=org.gradle.api.JavaVersion.VERSION_1_8
java.targetCompatibility=org.gradle.api.JavaVersion.VERSION_1_8

allprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        mavenPublish {
            sonatypeHost = SonatypeHost.S01

        }
    }
}

dependencies {

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")


    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.google.guava:guava:$guavaVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
