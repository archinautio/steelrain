import io.archinaut.steelrain.gradle.JlinkRuntime
import io.archinaut.steelrain.gradle.LauncherSmokeTest
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("steelrain.kotlin-application")
    id("steelrain.kotlin-serialization")
    alias(libs.plugins.shadow)
}

// Mordant's FFM terminal backend runs only with native access enabled; without it Mordant falls back
// to JNA, whose native library load makes the JVM print restricted-method warnings at startup.
val nativeAccess = "ALL-UNNAMED"

application {
    mainClass.set("io.archinaut.steelrain.tui.MainKt")
    applicationDefaultJvmArgs = listOf("--enable-native-access=$nativeAccess")
}

tasks.named<ProcessResources>("processResources") {
    from(rootProject.layout.projectDirectory.dir("theme")) {
        into("theme")
        exclude(".DS_Store")
    }
}

// See docs/tui-testing.md for why this task cannot work and what to run instead.
tasks.named<JavaExec>("run") {
    doFirst {
        throw GradleException(
            "The TUI requires a direct terminal connection that Gradle cannot provide " +
            "(Gradle always forks a separate JVM detached from the terminal).\n" +
            "Build and run the JAR directly:\n" +
            "  ./gradlew :tui:shadowJar && java -jar tui/build/libs/tui.jar"
        )
    }
}

dependencies {
    implementation(project(":tactical"))
    implementation(project(":network"))
    implementation(libs.tenter)
    implementation(libs.clikt)
    implementation(libs.mordant)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.konsist)
}

tasks.shadowJar {
    archiveBaseName.set("tui")
    archiveClassifier.set("")
    archiveVersion.set("")
    // Shadow excludes duplicate paths before transformers run, which would keep only one backend's
    // service file; include them so service files merge, and exclude every other duplicate.
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
    filesNotMatching("META-INF/services/**") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    // GPL-3.0: ship the license text with the binary. A distinct name so a dependency's own
    // META-INF/LICENSE cannot win the duplicate exclusion above.
    from(rootProject.layout.projectDirectory.file("LICENSE")) {
        into("META-INF")
        rename { "LICENSE-steelrain" }
    }
    manifest {
        attributes(
            // `java -jar` reads this instead of a command-line flag.
            "Enable-Native-Access" to nativeAccess,
            // `--version` reads this.
            "Implementation-Version" to project.version,
        )
    }
}

// Release downloads; see docs/build.md "Release packaging". Everything lands in build/release.
val releaseDirectory = layout.buildDirectory.dir("release")
val releaseVersion = project.version.toString()

val releaseJar = tasks.register<Copy>("releaseJar") {
    group = "distribution"
    description = "Copies the shadow jar to build/release as steelrain-<version>.jar."
    from(tasks.shadowJar)
    rename(".*", "steelrain-$releaseVersion.jar")
    into(releaseDirectory)
}

// The bundle with its own Java is built for the OS and CPU Gradle runs on.
val isWindows = System.getProperty("os.name").startsWith("Windows")
val platform = run {
    val osName = System.getProperty("os.name")
    val os = when {
        osName.startsWith("Mac") -> "macos"
        isWindows -> "windows"
        else -> "linux"
    }
    val arch = when (val arch = System.getProperty("os.arch")) {
        "aarch64", "arm64" -> "aarch64"
        "amd64", "x86_64" -> "x64"
        else -> arch
    }
    "$os-$arch"
}
val bundleName = "steelrain-$releaseVersion-$platform"

val jlinkRuntime = tasks.register<JlinkRuntime>("jlinkRuntime") {
    description = "Builds a trimmed Java runtime with the modules the shadow jar uses."
    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
    launcher.set(javaToolchains.launcherFor(java.toolchain))
    outputDirectory.set(layout.buildDirectory.dir("jlink/runtime"))
}

val stageBundle = tasks.register<Sync>("stageBundle") {
    description = "Lays out the bundle: launcher, steelrain.jar and runtime/."
    into(layout.buildDirectory.dir("bundle/$bundleName"))
    from(jlinkRuntime) {
        into("runtime")
        // jlink writes legal/ read-only, which would stop the next run from replacing those files.
        // Add owner write to each file's own mode; `permissions { }` alone would start from Gradle's
        // default mode and drop bin/'s execute bits.
        eachFile {
            val mode = permissions.toUnixNumeric() or "200".toInt(8)
            permissions { unix(mode) }
        }
    }
    from(tasks.shadowJar) { rename { "steelrain.jar" } }
    from(rootProject.layout.projectDirectory.file("LICENSE"))
    from("bundle") {
        include(if (isWindows) "steelrain.bat" else "steelrain")
        filePermissions { unix("rwxr-xr-x") }
    }
}

val bundleSmokeTest = tasks.register<LauncherSmokeTest>("bundleSmokeTest") {
    group = "verification"
    description = "Runs the staged bundle's launcher with its bundled runtime."
    dependsOn(stageBundle)
    bundleDirectory.set(layout.buildDirectory.dir("bundle/$bundleName"))
    expectedVersion.set(releaseVersion)
}

val bundleArchive = if (isWindows) {
    tasks.register<Zip>("bundle") { archiveFileName.set("$bundleName.zip") }
} else {
    tasks.register<Tar>("bundle") {
        archiveFileName.set("$bundleName.tar.gz")
        compression = Compression.GZIP
    }
}
bundleArchive.configure {
    group = "distribution"
    description = "Packs the bundle with its own Java runtime for this OS and CPU into build/release."
    dependsOn(bundleSmokeTest)
    // Archives normalize permissions by default; keep the launcher and runtime/bin executables runnable.
    from(stageBundle) {
        into(bundleName)
        useFileSystemPermissions()
    }
    destinationDirectory.set(releaseDirectory)
}

tasks.named("startScripts") {
    dependsOn(tasks.shadowJar)
}

tasks.named("startShadowScripts") {
    dependsOn(tasks.named("jar"))
}

val createExecutable = tasks.register("createExecutable") {
    group = "distribution"
    description = "Creates a self-executing tui binary (Unix/macOS)"
    dependsOn(tasks.shadowJar)
    val jarFile = tasks.shadowJar.flatMap { it.archiveFile }
    val outputFile = layout.buildDirectory.file("tui")
    inputs.file(jarFile)
    outputs.file(outputFile)
    doLast {
        val stub = "#!/bin/sh\nexec java -jar \"\$0\" \"\$@\"\n"
        outputFile.get().asFile.apply {
            writeBytes(stub.toByteArray() + jarFile.get().asFile.readBytes())
            setExecutable(true)
        }
    }
}
