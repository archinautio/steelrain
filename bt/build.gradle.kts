plugins {
    id("steelrain.kotlin-application")
}

application {
    mainClass.set("io.archinaut.steelrain.MainKt")
}

dependencies {
    implementation(project(":strategic"))
}
