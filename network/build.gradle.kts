plugins {
    id("steelrain.kotlin-library")
    id("steelrain.kotlin-serialization")
}

dependencies {
    api(project(":tactical"))
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("reflect"))
    testImplementation(libs.konsist)
}
