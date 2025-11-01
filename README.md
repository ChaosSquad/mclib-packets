# mclib-packets
This library contains Utilities for NMS packets.

### Features
- Packet Events
- Packet Entity Manager

### Javadocs
You can visit the JavaDocs here: [JavaDocs](https://chaossquad.github.io/mclib-packets)

### Import

Using Gradle:
```kotlin
repositories {
    // [...]
    maven {
        name = "chaossquad-releases"
        url = uri("https://maven.chaossquad.net/releases")
    }

    maven {
        name = "chaossquad-snapshots"
        url = uri("https://maven.chaossquad.net/snapshots")
    }
}

dependencies {
    // [...]
    implementation("net.chaossquad:mclib:main-f54795e7aa44aff7e2da665360ccafb9405d90ee") // Required
    implementation("net.chaossquad:mclib-packets:main-900330d162f9f1b5d72158275e3880b85c109364")
}
```