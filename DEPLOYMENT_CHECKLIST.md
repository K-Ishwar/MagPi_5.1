# MAG-Pi Production Deployment Checklist

## ✅ COMPLETED - Priority Items

### 1. ✅ Maven Dependencies Configuration
**Status**: DONE
- Added jSerialComm 2.10.4 for serial communication
- Added OpenCV 4.9.0-0 for video capture
- Added iText7 8.0.2 for PDF generation
- Added SLF4J 2.0.9 + Logback 1.4.11 for logging
- Configured version properties

### 2. ✅ Build Configuration
**Status**: DONE
- Added Maven Compiler Plugin (Java 23)
- Added Maven Shade Plugin for fat JAR creation
- Configured main class: com.magpi.Main
- Added proper manifest transformer
- Configured signature file filtering

### 3. ✅ Logging Framework
**Status**: DONE
- Created `src/main/resources/logback.xml`
- Configured console, file, and error file appenders
- Daily log rotation with 30-day retention
- Log files location: `%USERPROFILE%\MagPi\logs\`
- Application-specific logger for com.magpi package

### 4. ✅ Comprehensive README
**Status**: DONE
- Added installation instructions (JAR and source build)
- Added usage guide with step-by-step instructions
- Added system requirements
- Added troubleshooting section for common issues
- Added configuration documentation
- Added serial protocol documentation
- Added development/build instructions
- Added support contact information

### 5. ✅ License File
**Status**: DONE
- Created proprietary LICENSE file
- Included third-party license attributions
- Added warranty disclaimer and liability limitations
- Added usage restrictions and terms

### 6. ✅ Logo Case Sensitivity Fix
**Status**: DONE
- Renamed `Logo.png` to `logo.png` to match code references
- Fixed inconsistency that could cause image loading issues

### 7. ✅ Password Field Removal
**Status**: DONE
- Removed unused `passwordField` from LoginPage
- Removed password field from UI dialog
- Updated grid layout positions
- Cleaned up unused variable declaration

---

## 🔄 NEXT STEPS - To Complete Before Deployment

### 8. ⚠️ Replace System.out/err with Logging
**Status**: PENDING
**Priority**: HIGH
**Files to update**:
- `SerialPortManager.java` - Lines 93, 101, 127, 130, 144
- `VLCJVideoStream.java` - Lines 32, 34, 293, 327
- `LoginPage.java` - Lines 53, 61, 64
- `Main.java` - Line 231
- `RecordedVideosPage.java` - Lines 43, 45
- `PersistentLibrary.java` - printStackTrace calls

**Action needed**:
```java
// Replace:
//System.out.println("message");
//System.err.println("error");
//e.printStackTrace();

// With:
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

//private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

//logger.info("message");
//logger.error("error", e);
```

### 9. 📦 Build and Test
**Status**: PENDING
**Priority**: HIGH
**Actions**:
1. Install Maven if not available
2. Run `mvn clean compile` to verify compilation
3. Run `mvn package` to create fat JAR
4. Test the JAR: `java -jar target/MAG-Pi_5.o-1.0-SNAPSHOT.jar`
5. Verify all dependencies are bundled
6. Test serial port detection
7. Test webcam functionality
8. Test PDF export
9. Test video recording

### 10. 🧪 Add Unit Tests
**Status**: NOT STARTED
**Priority**: MEDIUM
**Actions**:
1. Create `src/test/java` directory structure
2. Add JUnit dependency to pom.xml
3. Write tests for:
   - SerialPortManager data parsing
   - PersistentLibrary save/load operations
   - TestSession calculations
   - Measurement validation
4. Aim for >60% code coverage on critical paths

### 11. 🎨 Create Application Icon
**Status**: NOT STARTED
**Priority**: MEDIUM
**Actions**:
1. Create `.ico` file for Windows
2. Add to resources
3. Update build to include icon in executable

### 12. 📦 Create Windows Installer
**Status**: NOT STARTED
**Priority**: MEDIUM
**Tools**: Use launch4j + Inno Setup or jpackage
**Actions**:
1. Use jpackage (Java 14+) to create native installer
2. Bundle JRE with application
3. Create desktop shortcut
4. Add to Start Menu
5. Create uninstaller

Example jpackage command:
```bash
jpackage --input target/ \
  --name MAG-Pi \
  --main-jar MAG-Pi_5.o-1.0-SNAPSHOT.jar \
  --main-class com.magpi.Main \
  --type msi \
  --icon resources/magpi.ico \
  --app-version 1.0 \
  --vendor "Vinze Magnafield Controls" \
  --description "Magnetic Particle Inspection System"
```

### 13. 📚 User Documentation
**Status**: NOT STARTED
**Priority**: MEDIUM
**Actions**:
1. Create PDF user manual
2. Include screenshots of each screen
3. Add hardware setup diagrams
4. Include calibration procedures
5. Add maintenance guidelines

### 14. 🔒 Security Hardening
**Status**: NOT STARTED
**Priority**: LOW
**Actions**:
1. Implement input sanitization in all text fields
2. Validate file paths before operations
3. Add file extension validation for PDF exports
4. Consider adding authentication if needed
5. Secure log files (appropriate permissions)

### 15. 🚀 Performance Optimization
**Status**: NOT STARTED
**Priority**: LOW
**Actions**:
1. Profile with VisualVM
2. Ensure proper resource cleanup (serial ports, video)
3. Test with large datasets
4. Optimize table rendering if needed
5. Consider lazy loading for history

---

## 📋 Pre-Deployment Verification

Before deploying to end users, verify:

- [ ] Application starts without errors
- [ ] All dependencies are bundled in JAR
- [ ] Serial port detection works
- [ ] Webcam detection works
- [ ] Data persists correctly
- [ ] PDF export functions properly
- [ ] Video recording works
- [ ] Logs are being created
- [ ] All forms validate input correctly
- [ ] Application closes cleanly
- [ ] No memory leaks during extended use
- [ ] README instructions are accurate
- [ ] LICENSE file is included
- [ ] Version number is correct

---

## 🏗️ Build Commands

```powershell
# Clean build
mvn clean

# Compile only
mvn compile

# Run tests (when added)
mvn test

# Create executable JAR
mvn package

# Run application with Maven
mvn exec:java -Dexec.mainClass="com.magpi.Main"

# Run packaged JAR
java -jar target/MAG-Pi_5.o-1.0-SNAPSHOT.jar
```

---

## 📝 Release Process

1. **Update version** in pom.xml (remove -SNAPSHOT for release)
2. **Run full build**: `mvn clean package`
3. **Run all tests**: `mvn test`
4. **Manual testing**: Test all features
5. **Create release notes**: Document changes
6. **Tag release**: `git tag v1.0.0`
7. **Build installer**: Run jpackage
8. **Upload artifacts**: JAR + Installer to distribution point
9. **Update documentation**: Ensure README is current
10. **Notify users**: Send release announcement

---

## 🐛 Known Issues / Technical Debt

1. **No database**: Using file-based persistence, consider migrating to SQLite
2. **Windows-only**: Application uses Windows-specific fonts and paths
3. **No update mechanism**: Users must manually download new versions
4. **Limited error recovery**: Some failures require application restart
5. **No session auto-save**: Data could be lost if application crashes
6. **Video codec hardcoded**: MJPEG only, consider adding H.264 support

---

## 📞 Support Information

**For deployment questions:**
- Developer: Check WARP.md for architecture details
- Build issues: Review pom.xml dependencies
- Runtime issues: Check logs in `%USERPROFILE%\MagPi\logs\`

**For end-user support:**
- See README.md troubleshooting section
- Contact: support@vinzemagnafield.com

---

*Last Updated: 2025-11-01*
*Version: 1.0-SNAPSHOT*
