package io.archinaut.battletech.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Builds a trimmed Java runtime for [jar] with the JDK's own `jdeps` and `jlink`, taken from
 * [launcher]'s installation. `jdeps` finds the modules the jar uses; [extraModules] adds any it
 * cannot see, such as modules reached only through reflection or service loading.
 *
 * The runtime is for the platform Gradle runs on: a runtime for each OS and CPU is built on that
 * OS and CPU.
 */
@CacheableTask
abstract class JlinkRuntime : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val jar: RegularFileProperty

    @get:Nested
    abstract val launcher: Property<JavaLauncher>

    @get:Input
    abstract val extraModules: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    protected abstract val exec: ExecOperations

    @get:Inject
    protected abstract val files: FileSystemOperations

    init {
        extraModules.convention(emptyList())
    }

    @TaskAction
    fun build() {
        val javaHome = launcher.get().metadata.installationPath.asFile
        val feature = launcher.get().metadata.languageVersion.asInt()
        val exe = if (System.getProperty("os.name").startsWith("Windows")) ".exe" else ""

        val jdepsOutput = ByteArrayOutputStream()
        exec.exec {
            executable = javaHome.resolve("bin/jdeps$exe").path
            args(
                "--print-module-deps",
                "--ignore-missing-deps",
                "--multi-release", feature.toString(),
                jar.get().asFile.path,
            )
            standardOutput = jdepsOutput
        }
        val modules = (jdepsOutput.toString().trim().split(',') + extraModules.get())
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .sorted()
        logger.lifecycle("jlink modules: ${modules.joinToString(",")}")

        // jlink refuses to write into an existing directory.
        val output = outputDirectory.get().asFile
        files.delete { delete(output) }
        exec.exec {
            executable = javaHome.resolve("bin/jlink$exe").path
            args(
                "--add-modules", modules.joinToString(","),
                "--strip-debug",
                "--no-header-files",
                "--no-man-pages",
                "--compress=zip-6",
                "--output", output.path,
            )
        }
    }
}
