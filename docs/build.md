# Build and packaging

buildSrc convention plugins, module dependency edges, TUI shadow-jar packaging, versioning and
release downloads. Read this when touching `buildSrc/`, a `build.gradle.kts`, module dependencies,
how root content resources and the shadow jar get packaged, or how a release is cut.

## buildSrc convention plugins

Applied via `id("steelrain.<name>")`. `buildSrc` also holds two task types used by release
packaging, in `io.archinaut.steelrain.gradle` (see [Release packaging](#release-packaging)):

- **`steelrain.kotlin-common`** — base for every module: applies `kotlin("jvm")`, sets the JVM toolchain (JVM 21 when `CLAUDE_CODE` env var is set — Claude Cloud constraint — otherwise the catalog version), enables `explicitApi()`, configures JUnit Platform + test logging, wires standard test deps (JUnit BOM/bundle, MockK, AssertJ).
- **`steelrain.kotlin-library`** — applies `kotlin-common`; used by `strategic`, `tactical`, and `network`.
- **`steelrain.kotlin-application`** — applies `kotlin-common` + the `application` plugin; used by `tui`.
- **`steelrain.kotlin-serialization`** — applies the Kotlin serialization plugin; used by `tactical` and `network` (both need kotlinx-serialization for `GameState`/wire types).

## Module dependency edges

- `network → tactical`: `api(project(":tactical"))` in `network/build.gradle.kts` — deliberately transitive (not `implementation`). `network` re-exports `tactical` types (`GameCommand`, `GameEvent`, `PlayerGameState`, `LogEntry`, `TurnState`) directly as wire DTOs instead of redefining them, so consumers of `network` need `tactical`'s types on their compile classpath too.
- `tui → tactical`, `tui → network`: `implementation(project(...))` in `tui/build.gradle.kts`.
- `strategic` and `tactical` declare no `project(...)` dependencies on other modules.

## TUI packaging

`tui/build.gradle.kts` applies `alias(libs.plugins.shadow)` — `com.gradleup.shadow`, version `9.6.1` per `gradle/libs.versions.toml`.

Tactical's `processResources` copies the repository's root `map/`, `unit/`, and `mech/` directories into the tactical runtime resources under the same names. Because `tui` depends on `tactical`, Shadow JAR assembly includes the packaged maps, unit collections, and mech collections transitively; none is configured separately on `shadowJar`.

Themes are packaged the same way but one module closer to the jar: `tui/build.gradle.kts` configures its OWN `processResources` (tactical's block cannot reach it) to copy the repository's root `theme/` directory into `tui`'s runtime resources under `theme/`, so the six built-in `theme/*.json` files and `theme/index.json` land in the shadow jar directly rather than transitively. See `docs/site/themes.md` for the file format, and `docs/architecture.md`'s resource-loading section for the shared loader design.

`tasks.shadowJar` is configured with `archiveBaseName = "tui"`, `archiveClassifier = ""`, `archiveVersion = ""`, so the fat jar lands at exactly `tui/build/libs/tui.jar` (no `-all`/version suffix). `mergeServiceFiles()` merges `META-INF/services` entries from bundled dependencies, but Shadow drops duplicate paths before its transformers run, so `duplicatesStrategy` is `INCLUDE` for the whole jar and `EXCLUDE` for everything outside `META-INF/services/`. Without that, Mordant's terminal-backend service file keeps only one of its three providers (FFM, GraalVM native image, JNA).

### Native access

Mordant prefers its FFM terminal backend, which runs only when the JVM grants native access; otherwise it falls back to JNA, whose native library load makes the JVM print restricted-method warnings at startup. `tui/build.gradle.kts` grants it to both launchers: the shadow jar's manifest carries `Enable-Native-Access: ALL-UNNAMED`, which `java -jar` (and so `createExecutable`'s `build/tui`) reads, and `applicationDefaultJvmArgs` passes `--enable-native-access=ALL-UNNAMED` to the `installDist` start script. Any other launch — an IDE run configuration, or `java -cp` with `MainKt` — needs that flag as a VM option, or it runs on JNA with the warnings.

The `createExecutable` task (group `distribution`) depends on `shadowJar` and prepends a POSIX shell stub — `#!/bin/sh\nexec java -jar "$0" "$@"\n` — to the shadow jar's bytes, writing the result to `build/tui` and marking it executable. A shell script prepended to a zip/jar is still a valid jar (the JVM's zip reader scans from the end of the file), so `build/tui` is simultaneously a runnable shell script and a runnable jar.

## Versioning and releases

The version lives in the root `gradle.properties` (`version=X.Y.Z-SNAPSHOT`) and applies to every
module. The shadow jar's manifest carries it as `Implementation-Version`, which `--version`
prints (`Cli.kt`'s `STEELRAIN_VERSION`; `development` when running from classes).

The root `build.gradle.kts` applies `net.researchgate.release`, configured like Tenter's. On a clean
`main`, `./gradlew release` asks for the release version (default: the SNAPSHOT without its
suffix), runs `build`, commits `chore(release): release`, tags `vX.Y.Z`, then sets the next
SNAPSHOT with the minor version raised (0.1.0 → 0.2.0-SNAPSHOT) and commits
`chore(release): start`. It does not push; push both commits and the tag with
`git push origin main vX.Y.Z`.

Pushing the tag runs `.github/workflows/release.yml`:

1. `jar` (Linux) checks that the tag matches `gradle.properties`, runs `build`, and packages
   `:tui:releaseJar`.
2. `bundle` runs `:tui:bundle` on `macos-latest`, `ubuntu-latest`, `ubuntu-24.04-arm` and
   `windows-latest`. Each runner builds the bundle for its own OS and CPU.
3. `release` writes notes with git-cliff (`cliff.toml`: `feat`, `fix`, `perf`, `build(deps)`, `docs`
   commits since the previous tag) and creates the GitHub release with every file attached.

## Release packaging

All release files land in `tui/build/release/`:

- **`:tui:releaseJar`** copies the shadow jar as `steelrain-<version>.jar`, for players who
  already have Java 25.
- **`:tui:bundle`** packs `steelrain-<version>-<os>-<arch>` (`.tar.gz`, or `.zip` on Windows) with
  its own Java runtime. `<os>` is `macos`, `linux` or `windows`; `<arch>` is `aarch64` or `x64`, both
  taken from the JVM Gradle runs on. Its tasks:
  - `jlinkRuntime` (`buildSrc`'s `JlinkRuntime`) runs the toolchain JDK's `jdeps
    --print-module-deps` on the shadow jar and `jlink`s exactly those modules into
    `build/jlink/runtime`. A module reached only through reflection or a service loader is
    invisible to `jdeps`; add it to the task's `extraModules`.
  - `stageBundle` lays out `build/bundle/<name>/`: `steelrain.jar`, `runtime/`, `LICENSE`, and the launcher
    from `tui/bundle/` (`steelrain` for POSIX shells, `steelrain.bat` on Windows). The launcher
    runs `runtime/bin/java -jar steelrain.jar`; native access comes from the jar's manifest.
  - `bundleSmokeTest` (`buildSrc`'s `LauncherSmokeTest`) runs the staged launcher with
    `--version` and every `--list-*` flag, so a runtime missing a module fails the release
    before anything is uploaded. It cannot reach the interactive screen, which needs a TTY.
  - The archive task calls `useFileSystemPermissions()`, because Gradle archives otherwise
    normalize permissions and drop the execute bits of the launcher and `runtime/bin/*`.

macOS runs a downloaded bundle without a Gatekeeper warning, even though the project signs
nothing: the launcher is a shell script, which Gatekeeper does not assess, and every native file
in `runtime/` comes from Temurin, signed with the Eclipse Foundation's Developer ID. This was
checked with a Chrome download unpacked in Finder, where every file carried
`com.apple.quarantine`. It holds only while the release workflow's `setup-java` uses
`distribution: temurin` (or another signed JDK); with an unsigned JDK, players would need to clear
the quarantine attribute before the first run.
