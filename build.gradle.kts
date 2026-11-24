import java.util.regex.Matcher

plugins {
    base
    alias(libs.plugins.release)
}

// `./gradlew release` turns the SNAPSHOT version in gradle.properties into a release version,
// builds, commits and tags vX.Y.Z, then starts the next SNAPSHOT. Pushing the tag runs the Release
// workflow (.github/workflows/release.yml), which builds the downloads and creates the GitHub release.
release {
    tagTemplate.set("v\${version}")
    preTagCommitMessage.set("chore(release): release")
    tagCommitMessage.set("chore(release): release")
    newVersionCommitMessage.set("chore(release): start")
    buildTasks.set(listOf("build"))
    // The next development version raises the minor version: 0.1.0 is followed by 0.2.0-SNAPSHOT.
    versionPatterns = mapOf(
        """^(\d+)\.(\d+)\.(\d+)(.*)$""" to KotlinClosure2<Matcher, Project, String>({ matcher, _ ->
            "${matcher.group(1)}.${matcher.group(2).toInt() + 1}.0${matcher.group(4)}"
        }),
    )
    git {
        requireBranch.set("main")
        pushToRemote.set(false)
    }
}
