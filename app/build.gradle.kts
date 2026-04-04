import java.util.Properties
import java.io.File // Thêm import File nếu bị thiếu

val localProperties = Properties()
val localPropertiesFile: File? = rootProject.file("local.properties")
if (localPropertiesFile?.exists() == true) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.tanh.datsan"
    compileSdk = 36 // Đã sửa lại cho đúng cú pháp

    defaultConfig {
        applicationId = "com.tanh.datsan"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Đọc BASE_URL từ local.properties, nếu không có thì dùng mặc định là localhost của máy ảo
        // Nếu không có trong file local.properties, tự động lấy "http://10.0.2.2:3000/"
        val baseUrl = localProperties.getProperty("API_BASE_URL") ?: "http://10.0.2.2:3000/"

        // Tương tự, tự động lấy "10.0.2.2" nếu không tìm thấy
        val apiHost = localProperties.getProperty("API_HOST") ?: "10.0.2.2"

        buildConfigField("String", "API_BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "API_HOST", "\"$apiHost\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig=true
    }
    buildToolsVersion = "36.1.0"
}

dependencies {
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Icons
    implementation(libs.androidx.material.icons.extended)

    // Retrofit & Coroutines (Sử dụng version catalog, đã xóa các dòng hardcode bị trùng ở dưới)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.play.services.location)
    implementation(libs.logging.interceptor)
    implementation(libs.coil.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // ViewModel & Navigation cho Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose.android)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Thư viện Đăng nhập Google thế hệ mới (Credential Manager)
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
}