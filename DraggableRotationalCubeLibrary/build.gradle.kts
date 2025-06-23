plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ext.draggablerotationalcubelibrary"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.dynamicanimation.ktx)
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    

    // Exclude old support library from all dependencies
    configurations.all {
        exclude(group = "com.android.support", module = "support-compat")
        exclude(group = "com.android.support", module = "support-v4")
        exclude(group = "com.android.support", module = "appcompat-v7")
        exclude(group = "com.android.support", module = "recyclerview-v7")
        exclude(group = "com.android.support", module = "cardview-v7")
        exclude(group = "com.android.support", module = "design")
        exclude(group = "com.android.support", module = "versionedparcelable")
    }
}