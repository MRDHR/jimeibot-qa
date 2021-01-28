plugins {
    val kotlinVersion = "1.4.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.0.0" // mirai-console version
}

mirai {
    coreVersion = "2.1.1" // mirai-core version
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")
}

group = "com.dhr.bot.qa"
version = "0.1.0"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}