# MAG-Pi 5.0 - Magnetic Particle Inspection System

**Developed by Vinze Magnafield Controls**

MAG-Pi is a professional desktop application for magnetic particle inspection data acquisition and quality control testing. It interfaces with Arduino-based measurement devices and webcam systems to provide real-time monitoring, video recording, and comprehensive reporting capabilities.

![Version](https://img.shields.io/badge/version-1.0--SNAPSHOT-blue)
![Java](https://img.shields.io/badge/java-23-orange)
![License](https://img.shields.io/badge/license-Proprietary-red)

---

## Features

- **Real-time Data Acquisition**: Serial communication with Arduino devices for headshot and coilshot measurements
- **Video Recording**: Integrated webcam capture with timestamped recordings per test part
- **Session Management**: Comprehensive tracking of operator, machine, and test parameters
- **Quality Control**: Automatic accept/reject/rework status based on configurable thresholds
- **Historical Data**: Review past test sessions with searchable history
- **PDF Export**: Generate professional test reports with measurement tables and session metadata
- **Persistent Library**: Save and recall operators, part descriptions, and test parameters

---

## System Requirements

### Hardware
- **Operating System**: Windows 10 or later (64-bit)
- **RAM**: Minimum 4 GB (8 GB recommended)
- **Storage**: 500 MB free space (plus additional space for video recordings)
- **USB Ports**: Available for Arduino connection
- **Webcam**: USB webcam for video capture functionality
- **Arduino Device**: Compatible Arduino with serial communication (USB-SERIAL CH340 or similar)

### Software
- **Java Runtime Environment**: JRE 23 or later
- **Maven**: 3.6+ (for building from source)

---

## Installation

### Option 1: Run Pre-built JAR (Recommended for End Users)

1. **Download the latest release**:
   - Download `MAG-Pi_5.o-1.0-SNAPSHOT.jar` from the releases page

2. **Install Java 23** (if not already installed):
   - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/)
   - Verify installation: `java -version`

3. **Run the application**:
   ```bash
   java -jar MAG-Pi_5.o-1.0-SNAPSHOT.jar
   ```

4. **Create a desktop shortcut** (optional):
   - Right-click on desktop → New → Shortcut
   - Location: `"C:\Program Files\Java\jdk-23\bin\javaw.exe" -jar "C:\path\to\MAG-Pi_5.o-1.0-SNAPSHOT.jar"`

### Option 2: Build from Source (For Developers)

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd MAG-Pi_5.o1
   ```

2. **Install dependencies**:
   - Ensure Java 23 and Maven are installed
   - Run: `mvn clean install`

3. **Build the executable JAR**:
   ```bash
   mvn clean package
   ```
   The executable JAR will be created in `target/MAG-Pi_5.o-1.0-SNAPSHOT.jar`

4. **Run the application**:
   ```bash
   java -jar target/MAG-Pi_5.o-1.0-SNAPSHOT.jar
   ```
   Or use Maven:
   ```bash
   mvn exec:java -Dexec.mainClass="com.magpi.Main"
   ```

---

## Usage Guide

### First-Time Setup

1. **Connect Hardware**:
   - Connect Arduino device via USB
   - Connect webcam (if using video capture)
   - Ensure drivers are installed

2. **Launch Application**:
   - Run the JAR file
   - The login screen will appear

### Starting a Test Session

1. **Login Screen**:
   - Click "Login" button
   - Fill in required fields:
     - Company Name
     - Machine ID
     - Supervisor ID
     - Operator Name (select existing or add new)
     - Part Description (select existing or add new)

2. **Set Parameters**:
   - Click "Next" to go to Parameters tab
   - Enter Headshot Threshold (kA)
   - Enter Coilshot Threshold (kA)
   - Click "Save Parameters" to store for future use
   - Click "Submit" to start session

3. **Testing Parts**:
   - Serial data from Arduino is automatically captured
   - Click "Capture Video" to open webcam window
   - Click "Start Recording" to record video for current part
   - Measurements appear in Headshot and Coilshot tables
   - Status is automatically determined (Accept/Reject/Rework)
   - Click "Next Part" to move to next test piece

4. **End Session**:
   - Click "End Session" when testing is complete
   - Navigate to "View History" to review or export data

### Viewing History

1. Click "View History" in the menu bar
2. Browse past test sessions
3. Select a session to view details
4. Click "Export to PDF" to generate a report

---

## Configuration

### Data Storage Locations

- **Library Data**: `C:\Users\{username}\MagPi\Library\`
  - `operators.txt` - Saved operator names
  - `parts.txt` - Saved part descriptions
  - `parameters.txt` - Current parameters per part
  - `parameter_history.txt` - Historical parameters

- **Video Recordings**: `C:\Users\{username}\MagPi\Videos\`
  - Format: `Part{N}_{yyyyMMdd_HHmmss}.avi`

- **Log Files**: `C:\Users\{username}\MagPi\logs\`
  - `magpi.log` - Application logs
  - `magpi-error.log` - Error logs only

### Serial Communication Protocol

The Arduino should send data in the following format:
```
Meter 1:<currentValue>:<duration>
Meter 2:<currentValue>:<duration>
```

Where:
- `Meter 1` = Headshot measurement
- `Meter 2` = Coilshot measurement
- `currentValue` = Current in amperes (double)
- `duration` = Duration in seconds (double)

**Example**:
```
Meter 1:2.5:1.2
Meter 2:3.8:0.9
```

---

## Troubleshooting

### Application Won't Start

**Problem**: Double-clicking JAR does nothing

**Solutions**:
- Verify Java is installed: `java -version`
- Run from command line to see errors: `java -jar MAG-Pi_5.o-1.0-SNAPSHOT.jar`
- Check if another instance is already running
- Review logs in `%USERPROFILE%\MagPi\logs\magpi-error.log`

### Arduino Not Detected

**Problem**: "Failed to open serial port" or no data received

**Solutions**:
- Check USB connection
- Verify Arduino is powered on
- Install CH340 USB driver if using clone Arduino
- Check Device Manager for COM port assignment
- Disconnect and reconnect Arduino
- Try a different USB port or cable

### Webcam Not Working

**Problem**: "Failed to open webcam" error

**Solutions**:
- Verify webcam is connected and not in use by another application
- Check Windows Privacy Settings → Camera → Allow apps to access camera
- Try unplugging and reconnecting webcam
- Test webcam with Windows Camera app first
- Update webcam drivers

### Video Files Too Large

**Problem**: Video recordings consuming too much disk space

**Solutions**:
- Record only critical tests
- Regularly archive old videos to external storage
- Delete unnecessary video files from `%USERPROFILE%\MagPi\Videos\`
- Stop recording when not actively testing

### PDF Export Fails

**Problem**: Error when generating PDF report

**Solutions**:
- Ensure you have write permissions to the selected directory
- Close any open PDFs with the same name
- Check available disk space
- Review error log for specific details

### Application Running Slow

**Problem**: UI freezes or sluggish performance

**Solutions**:
- Close other resource-intensive applications
- Increase Java heap size: `java -Xmx2G -jar MAG-Pi_5.o-1.0-SNAPSHOT.jar`
- Clear old log files from `%USERPROFILE%\MagPi\logs\`
- Restart the application
- Check if webcam resolution is too high (720p recommended)

---

## Support

For technical support, please contact:

**Vinze Magnafield Controls**
- Email: support@vinzemagnafield.com
- Website: www.vinzemagnafield.com

---

## Development

### Project Structure

```
MAG-Pi_5.o1/
├── src/
│   ├── main/
│   │   ├── java/com/magpi/
│   │   │   ├── Main.java              # Application entry point
│   │   │   ├── model/                 # Data models
│   │   │   ├── ui/                    # User interface components
│   │   │   ├── util/                  # Utilities (serial, PDF, persistence)
│   │   │   └── video/                 # Video capture
│   │   └── resources/
│   │       ├── logback.xml            # Logging configuration
│   │       ├── Logo.png               # Application logo
│   │       └── BackroundImage.png     # Login background
│   └── test/                          # Unit tests (to be added)
├── pom.xml                            # Maven build configuration
├── README.md                          # This file
└── LICENSE                            # Software license
```

### Building

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Package executable JAR
mvn package

# Run application
mvn exec:java -Dexec.mainClass="com.magpi.Main"
```

### Dependencies

- **jSerialComm 2.10.4** - Serial port communication
- **OpenCV 4.9.0-0** - Video capture and processing
- **iText7 8.0.2** - PDF generation
- **SLF4J 2.0.9** - Logging API
- **Logback 1.4.11** - Logging implementation

---

## Version History

### Version 1.0-SNAPSHOT (Current)
- Initial release
- Real-time serial data acquisition
- Video recording with OpenCV
- PDF report generation
- Persistent operator and part libraries
- Session history tracking

---

## License

Copyright © 2025 Vinze Magnafield Controls. All rights reserved.

This software is proprietary. See LICENSE file for details.
