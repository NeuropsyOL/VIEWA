[![Android Build](https://github.com/NeuropsyOL/VIEWA/actions/workflows/android_build.yml/badge.svg)](https://github.com/NeuropsyOL/VIEWA/actions/workflows/android_build.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/NeuropsyOL/VIEWA)](https://github.com/NeuropsyOL/VIEWA/releases/latest)

# VIEWA — Live LSL Data Viewer for Android

<div align="center">
  <img width="270" src="./docs/Screenshots/VIEWA_Main.png" alt="VIEWA main screen showing live sensor streams">
</div>

> 📋 See [CHANGELOG.md](CHANGELOG.md) for the full version history.

---

## Citation

> **If you use VIEWA in your research, please cite the following publication:**
>
> Haupt et al. — *Title TBA upon publication* — accepted. Citation will be updated once a DOI is available.

VIEWA is provided **without any warranty**, and without guarantee of fitness for a particular purpose. Use it at your own risk. See [LICENSE](LICENSE) for the full terms.

---

**VIEWA** turns your Android phone into a live oscilloscope for [Lab Streaming Layer (LSL)](https://labstreaminglayer.readthedocs.io/) [[1]](#lsl-ref) data.  
Discover any LSL stream on your local network and watch all channels scroll in real time — no laptop, no cables, no extra software required.

---

## Table of Contents

1. [Citation](#citation)
2. [Why VIEWA?](#why-viewa)
3. [Features](#features)
4. [Getting Started](#getting-started)
5. [Building from Source](#building-from-source)
6. [Scientific Use Cases & Publications](#scientific-use-cases--publications)
7. [Contributing](#contributing)
8. [Authors](#authors)
9. [License](#license)
10. [Acknowledgements](#acknowledgements)

---

## Why VIEWA?

### A signal monitor that fits in your pocket
Debugging a mobile EEG or sensor pipeline usually means squinting at a laptop screen across the room. VIEWA puts a live, scrolling view of every channel directly in your hand — useful during setup, during data collection, and during troubleshooting.

### Works with any LSL stream
VIEWA auto-discovers every stream advertised on the local network, regardless of the device or software that sends it. If it speaks LSL, VIEWA can plot it.

### Designed for use with RECORDA and SENDA
VIEWA pairs naturally with [RECORDA](https://github.com/NeuropsyOL/RECORDA) (our Android LSL recorder) and [SENDA](https://github.com/NeuropsyOL/SENDA) (our Android sensor streamer), forming a fully self-contained mobile neuroscience toolkit — stream, record, and visualise, all from Android devices.

---

## Features

- 📈 **Live multi-channel plotting** — smooth, scrolling real-time charts for every incoming channel
- 📡 **Auto-discovery** — finds all active LSL streams on the local network automatically
- 🔧 **Lightweight & self-contained** — no extra services or companion software needed on the phone
- 🌙 **Dark & light theme** — follows the system-wide display setting
- 📱 **Minimum Android 11** (API 30), tested on **Android 14** (API 34)

---

## Getting Started

Download the [latest release APK](https://github.com/NeuropsyOL/VIEWA/releases/latest) and sideload it onto your Android smartphone or tablet (Android 11 or higher).

> **Tip:** Make sure the phone running VIEWA and all streaming devices are on the **same local network** (same Wi-Fi access point). LSL uses multicast discovery, which does not work across separate network segments.

---

## Building from Source

We recommend installing the pre-built release APK. If you need to build from source:

### Prerequisites

| Tool | Version |
|---|---|
| Android Studio | Arctic Fox or newer |
| Android SDK Platform | API 34 |

### Steps

```bash
git clone https://github.com/NeuropsyOL/VIEWA.git
cd VIEWA
```

Open the project in Android Studio, let Gradle sync, then select your device/emulator and click **Run ▶**.

Or from the command line:

```bash
./gradlew installDebug
```

---

## Scientific Use Cases & Publications

VIEWA was developed to support mobile neuroscience and biosignal research at the [Neuropsychology Lab of Stefan Debener](https://uol.de/neuropsychologie), University of Oldenburg, Germany.

The VIEWA / RECORDA / SENDA ecosystem builds on a broader line of mobile neuroscience research from our lab:

- **Blum S, Hölle D, Bleichner MG, Debener S.** Pocketable Labs for Everyone: Synchronized Multi-Sensor Data Streaming and Recording on Smartphones with the Lab Streaming Layer. *Sensors*, 2021; 21(23):8135. https://doi.org/10.3390/s21238135

- **Debener S, Minow F, Emkes R, Gandras K, de Vos M.** How about taking a low-cost, small, and wireless EEG for a walk? *Psychophysiology*, 2012; 49(11):1449–1453. https://doi.org/10.1111/j.1469-8986.2012.01471.x

<!-- 
  TODO: Add further publications that have used VIEWA for data collection.
  Example template:
  - **Author(s).** Title. *Journal*, Year; Vol(Issue):Pages. https://doi.org/...
-->

### Lab Streaming Layer
<a name="lsl-ref"></a>
- **Kothe C, Shirazi SY, Stenner T, Medine D, Boulay C, Grivich MI, Artoni F, Mullen T, Delorme A, Makeig S.** The Lab Streaming Layer for Synchronized Multimodal Recording. *Imaging Neurosci (Camb).* 2025;3:IMAG.a.136. https://doi.org/10.1162/IMAG.a.136

---

## Contributing

Contributions are welcome! Please:
1. [Open an issue](https://github.com/NeuropsyOL/VIEWA/issues) describing the bug or feature request.
2. Fork the repository and create a branch from `master`.
3. Submit a pull request referencing the issue.

See [CONTRIBUTING.md](CONTRIBUTING.md) for details on our workflow.

---

## Authors

Developed by the [Neuropsychology Lab of Stefan Debener](https://uol.de/neuropsychologie), University of Oldenburg, Germany.

**Active Developers**
- **Sarah Blum** — [s4rify](https://github.com/s4rify)

**Previous Developers**
- **Paul Maanen** — [pmaanen](https://github.com/pmaanen)

---

## License

GNU General Public License v3.0 — see [LICENSE](LICENSE) for details.

---

## Acknowledgements

- [liblsl](https://github.com/sccn/liblsl) — MIT License
- [liblsl-Java](https://github.com/labstreaminglayer/liblsl-Java) — MIT License
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) — Apache 2.0 License
