package io.archinaut.steelrain.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Runs a staged download's launcher with its bundled runtime: `--version` must print
 * [expectedVersion], and every `--list-*` flag must succeed, which loads the packaged maps, mechs,
 * units and themes. This catches a runtime that lacks a module the game needs.
 */
@UntrackedTask(because = "A smoke test runs every time it is asked for.")
abstract class LauncherSmokeTest : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val bundleDirectory: DirectoryProperty

    @get:Input
    abstract val expectedVersion: Property<String>

    @get:Inject
    protected abstract val exec: ExecOperations

    @TaskAction
    fun run() {
        val version = run("--version").trim()
        val expected = "steelrain ${expectedVersion.get()}"
        if (version != expected) throw GradleException("--version printed '$version', expected '$expected'")
        for (flag in listOf("--list-maps", "--list-mechs", "--list-units", "--list-themes")) {
            if (run(flag).isBlank()) throw GradleException("$flag printed nothing")
        }
    }

    private fun run(vararg args: String): String {
        val dir = bundleDirectory.get().asFile
        val output = ByteArrayOutputStream()
        exec.exec {
            if (System.getProperty("os.name").startsWith("Windows")) {
                commandLine("cmd", "/c", dir.resolve("steelrain.bat").path, *args)
            } else {
                commandLine(dir.resolve("steelrain").path, *args)
            }
            standardOutput = output
        }
        return output.toString()
    }
}
