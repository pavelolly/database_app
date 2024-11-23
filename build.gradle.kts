plugins {
    id("java")
}

group = "com.km"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // https://mvnrepository.com/artifact/org.postgresql/postgresql
    implementation("org.postgresql:postgresql:42.7.2")

    implementation("org.projectlombok:lombok:1.16.10")

    implementation(files("external/Raylib-J-0.5.2.jar"))

    // implementation("org.lwjgl:lwjgl")
}

tasks.test {
    useJUnitPlatform()
}


