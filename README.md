# Viewa
[![Android Build](https://github.com/NeuropsyOL/VIEWA/actions/workflows/android_build.yml/badge.svg)](https://github.com/NeuropsyOL/VIEWA/actions/workflows/android_build.yml)
[![GPL v3 License](https://img.shields.io/badge/license-GPL%20v3-blue.svg)](LICENSE)

## Overview

**Viewa** is an Android application for receiving [Lab Streaming Layer (LSL)](https://github.com/sccn/labstreaminglayer) data streams over the network and plotting them live. It is designed for researchers—especially in neuroscience—who need a lightweight, mobile tool to monitor and visualize any LSL-compatible data in real time.

## Features

- 📈 **Live plotting**  
  Real-time visualization of incoming data using smooth, interactive charts.
- 🔧 **Lightweight & self-contained**  
  No extra smartphone-side dependencies; just install the APK.

## Requirements

- **Android OS**:  
  - Minimum **Android 11** (API 30)  
  - Tested on **Android 14** (API 34)  
- **No additional apps or services** needed on the device.

## Installation 

### 1. Download the APK

1. Go to the [Viewa Releases page](https://github.com/your-username/Viewa/releases).  
2. Under **Assets**, download the latest `Viewa-vX.Y.Z.apk`.  
3. On your Android device, enable “Install unknown apps” (Settings → Security or Apps → Browser/File Manager).  
4. Open the downloaded APK and follow the prompts to install.

### 2. (Optional) Build from Source

1. Clone the repository:  
   ```bash
   git clone https://github.com/your-username/Viewa.git
   cd Viewa
   ```
   
2. Open the project in Android Studio (Arctic Fox or newer).
3. Let Gradle sync, then select your device/emulator and click Run ▶.
4. Alternatively, from the command line:
```bash
./gradlew installDebug
```

## Authors
The app is actively developed by the neuropsychology group of [Stefan Debener](https://uol.de/neuropsychologie) in Oldenburg, Germany.

#### Active Developers
* **Sarah Blum** - [sarah-blum](https://github.com/s4rify)

#### Previous Developers
* **Paul Maanen** - [pmaanen](https://github.com/pmaanen)

## Acknowledgements
* [liblsl](https://github.com/sccn/liblsl), used under MIT license
* [liblsl-Java](https://github.com/labstreaminglayer/liblsl-Java), used under MIT license
* [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart), Apache v2.0 license

