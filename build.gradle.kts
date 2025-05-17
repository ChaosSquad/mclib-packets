plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.8"
    id("maven-publish")
}

group = "net.chaossquad"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "chaossquad-releases"
        url = uri("https://maven.chaossquad.net/releases")
    }
    maven {
        name = "chaossquad-snapshots"
        url = uri("https://maven.chaossquad.net/snapshots")
    }
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.chaossquad:mclib:main-92a169fd549aa8252e7010ada5beb12c781761d7")
}

java {
    withSourcesJar()
    withJavadocJar()
}

// gradle publish{PUBLICATION_NAME}To{REPOSITORY_NAME}Repository
// in this case: publishMavenToChaosSquadRepository
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "chaossquad"
            url = uri(if (version.toString().endsWith("RELEASE")) {
                "https://maven.chaossquad.net/releases"
            } else {
                "https://maven.chaossquad.net/snapshots"
            })

            credentials {
                username = findProperty("chaossquad-repository.username") as String?
                password = findProperty("chaossquad-repository.password") as String?
            }
        }
    }
}
