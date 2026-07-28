plugins {
    `java-library`
    application
    id("io.freefair.aspectj") version "9.5.0"
    jacoco
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("Main") 
}

dependencies {
    implementation("org.aspectj:aspectjrt:1.9.22")
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}