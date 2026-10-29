plugins { id("com.android.application") }
android {
    namespace = "org.taskdeck.app"
    compileSdk = 35
    defaultConfig { applicationId = "org.taskdeck.app"; minSdk = 29; targetSdk = 35; versionCode = 6; versionName = "0.6.0-prototype" }
    buildFeatures { aidl = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    signingConfigs.getByName("debug") { storeFile = rootProject.file(".local/debug.keystore") }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.systemProperty("java.io.tmpdir", layout.buildDirectory.dir("test-temp").get().asFile.apply { mkdirs() }.absolutePath)
            it.systemProperty("user.home", layout.buildDirectory.dir("test-home").get().asFile.apply { mkdirs() }.absolutePath)
            it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
            it.jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED")
        }
    }
}
dependencies {
    compileOnly("androidx.annotation:annotation:1.3.0")
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
}
val createLocalDebugKey by tasks.registering(Exec::class) {
    val key = rootProject.file(".local/debug.keystore")
    onlyIf { !key.exists() }
    doFirst { key.parentFile.mkdirs() }
    commandLine("${System.getProperty("java.home")}/bin/keytool", "-genkeypair", "-noprompt", "-keystore",key.absolutePath,
        "-storepass","android","-keypass","android","-alias","androiddebugkey","-dname","CN=TaskDeck Debug,O=SilasDV",
        "-keyalg","RSA","-keysize","2048","-validity","10000")
}
tasks.configureEach { if(name=="validateSigningDebug") dependsOn(createLocalDebugKey) }
