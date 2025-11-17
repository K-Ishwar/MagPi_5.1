# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

MAG-Pi is a Magnetic Particle Inspection System - a Java Swing desktop application for data acquisition and quality control testing. Built by Vinze Magnafield Controls, it manages test sessions with serial communication to Arduino devices and webcam video recording.

## Build & Development Commands

### Build
```powershell
mvn clean compile
```

### Package
```powershell
mvn package
```

### Run
```powershell
mvn exec:java -Dexec.mainClass="com.magpi.Main"
```

Or after packaging:
```powershell
java -jar target/MAG-Pi_5.o-1.0-SNAPSHOT.jar
```

## Architecture

### Application Flow
1. **Login** → Operator enters session metadata (operator name, machine ID, supervisor, company, part description, thresholds)
2. **Test Session** → Active testing with real-time serial data collection and optional video recording
3. **History View** → Review past test results with PDF export capability

### Core Components

**Model Layer** (`com.magpi.model`)
- `TestSession`: Central session object holding operator info, test parts, and thresholds
- `TestPart`: Individual part with measurements (headshot/coilshot), status (Accept/Reject/Rework), and associated video paths
- `Measurement`: Single measurement reading with type, current value, and duration

**UI Layer** (`com.magpi.ui`)
- `Main`: Entry point, manages JTabbedPane navigation between Login/Table/History pages, custom Swing theme
- `LoginPage`/`LoginPanel`: Session initialization forms
- `TablePage`: Active testing interface with real-time measurement display
- `HistoryPage`: Historical data viewer
- `table/`: Custom table renderers/editors for status buttons and persistent color coding

**Hardware Integration** (`com.magpi.util`, `com.magpi.video`)
- `SerialPortManager`: Communicates with Arduino via jSerialComm, auto-detects "USB-SERIAL CH340" ports, parses "Meter X:current:duration" format
- `VLCJVideoStream`: OpenCV-based webcam capture and recording, saves to `~/MagPi/Videos/Part{N}_{timestamp}.avi`

**Utilities** (`com.magpi.util`)
- `PdfExporter`: Generates test reports from session data
- `PersistentLibrary`: Data persistence layer

### Serial Communication Protocol
Arduino sends data as: `Meter 1:<currentValue>:<duration>` or `Meter 2:<currentValue>:<duration>`
- Meter 1 = Headshot measurement
- Meter 2 = Coilshot measurement

### Video Recording
- Default save location: `C:\Users\{user}\MagPi\Videos\`
- Naming convention: `Part{partNumber}_{yyyyMMdd_HHmmss}.avi`
- Uses OpenCV via nu.pattern library loader

## Technology Stack

- **Language**: Java 23
- **Build Tool**: Maven
- **GUI**: Swing with custom theming (Segoe UI fonts, modern color scheme)
- **Serial**: jSerialComm library
- **Video**: OpenCV (via nu.pattern loader)
- **Resources**: Images in `src/main/resources/` (Logo.png, BackroundImage.png)

## Platform Notes

- Designed for Windows (uses Segoe UI fonts, Windows-style paths)
- Serial port detection looks for "USB-SERIAL CH340" or "Arduino" descriptors
- Video codec: MJPEG (fourcc 'MJPG')
