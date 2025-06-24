# Draggable Rotational Cube Library

A powerful Android library that provides a draggable, rotatable 3D cube interface with sliding content panels. The library allows developers to create interactive, floating user interfaces that can be dragged around the screen, rotated in 3D space, and display content through sliding panels.

## Key Features

### Core Functionality
- ✨ **Draggable Floating Interface**: The cube can be freely dragged around the screen using touch gestures
- 🔄 **3D Rotation Animation**: Smooth 3D rotation animations when interacting with the cube
- 🔍 **Sliding Content Panels**: Multiple content panels that slide in/out as the cube rotates
- 📱 **Floating Window Support**: Works as a system overlay window, allowing it to float above other apps

### Customization Options
- 🎨 **UI Customization**: Fully customizable UI components including headers, images, and descriptions
- 🔄 **Animation Control**: Control over animation durations and smoothness
- 📱 **Position Management**: Programmatically control the cube's position on screen
- 🔧 **State Persistence**: Automatic saving and restoring of cube position and state

### Technical Features
- 📱 **Android 8.0+ Support**: Compatible with Android 8.0 (Oreo) and above
- 🔧 **Modern Architecture**: Built using modern Android architecture components
- 📦 **Lightweight**: Minimal dependencies and optimized performance
- 📱 **Cutout Support**: Smart handling of screen cutouts (notch areas)

## System Requirements

- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 14)
- **Java Version**: 11
- **Build Tools**: Android Gradle Plugin 8.0+
- **Gradle Version**: 8.0+

## Installation Guide

### Step 1: Add Repository
Add the following repository to your project-level build.gradle:

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add Dependency
Add the library dependency to your app-level build.gradle:

```gradle
dependencies {
    implementation 'com.github.your-username:DraggableRotationalCubeLibrary:1.0.0'
}
```

## Usage

### 1. Add Permissions
Add the following permissions to your AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

### 2. Initialize Cube Mode
Initialize the cube mode in your Application class or before using the cube:

```kotlin
InitApplication.instance.setIsCubeModeEnabled(true)
```

### 3. Create Cube Items
Create a list of CubeItemData objects:

```kotlin
val cubeItems = mutableListOf(
    CubeItemData(
        header = "Full Content",
        headerVisible = true,
        image = R.drawable.your_image,
        imageVisible = true,
        description = "This is a full content item",
        descriptionVisible = true
    ),
    // Add more items as needed
)
```

### 4. Start Floating View Service
Start the floating view service with your cube items:

```kotlin
val intent = Intent(this, CustomFloatingViewService::class.java)
intent.putExtra(CustomFloatingViewService.EXTRA_CUTOUT_SAFE_AREA, 
    FloatingViewManager.findCutoutSafeArea(this))
intent.putParcelableArrayListExtra(CustomFloatingViewService.EXTRA_CUBE_DATA, 
    ArrayList(cubeItems))
startService(intent)
```

## Customization

### CubeItemData Properties

- `header`: String - Header text
- `headerVisible`: Boolean - Whether to show header
- `image`: Any? - Image resource (Int for drawable ID or String for URL)
- `imageVisible`: Boolean - Whether to show image
- `description`: String - Description text
- `descriptionVisible`: Boolean - Whether to show description

### Floating View Options

- `floatingViewX`: Initial X position
- `floatingViewY`: Initial Y position
- `animateInitialMove`: Enable/disable initial animation

## Project Structure

```
DraggableRotationalCubeLibrary/
├── src/main/
│   ├── java/
│   │   └── com.ext.draggablerotationalcubelibrary/
│   │       ├── CubeItemData.kt
│   │       ├── CustomFloatingViewService.kt
│   │       ├── FloatingViewManager.kt
│   │       ├── SliderAdapter.kt
│   │       └── InitApplication.kt
│   └── res/
│       ├── layout/
│       │   └── floating_slider_item.xml
│       └── values/
│           └── attrs.xml
```

## Dependencies

- Glide: For image loading
- AndroidX libraries: For modern Android development
- Material Design Components: For UI elements

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

MIT License

Copyright (c) 2025 Yash Raiyani

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Support

For support, please:
- Check the existing issues
- Open a new issue if you can't find a solution
- Contact us at [your email]

## Acknowledgments

- Thanks to all contributors
- Special thanks to the Android community
