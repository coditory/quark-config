plugins {
    id("build.java")
    id("build.test")
    id("build.coverage")
    id("build.publish")
    id("build.version")
}

dependencies {
    api(libs.jetbrains.annotations)
    implementation(libs.snakeyaml)
    implementation(libs.gson)
    testImplementation(libs.spock.core)
    testImplementation(libs.jsonassert)
    testImplementation(libs.junit.platform)
}
