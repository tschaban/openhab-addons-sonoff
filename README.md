# Sonoff Binding - Smart'nyDom Enhanced Edition

[![GitHub Release](https://img.shields.io/github/v/release/tschaban/openhab-addons-sonoff)](https://github.com/tschaban/openhab-addons-sonoff/releases)
[![GitHub Issues](https://img.shields.io/github/issues/tschaban/openhab-addons-sonoff)](https://github.com/tschaban/openhab-addons-sonoff/issues)
[![GitHub Milestones](https://img.shields.io/github/milestones/closed/tschaban/openhab-addons-sonoff)](https://github.com/tschaban/openhab-addons-sonoff/milestones?state=closed)
[![CI/CD Pipeline](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/ci.yml/badge.svg)](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/ci.yml)
[![Release Pipeline](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/release.yml/badge.svg)](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/release.yml)

**Enhanced and maintained by [Smart'nyDom](https://github.com/tschaban)** | Based on the original work by [delid4ve](https://github.com/delid4ve/openhab-3.x-sonoff)

This is an enhanced branch of the original Sonoff binding for OpenHAB, featuring extended device support, improved functionality, and comprehensive testing framework.

## 🚀 What's New in This Branch

### 📱 Extended Device Support
**Added: SONOFF Supported Devices**
- SONOFF WiFi iPlug Smart Plug | S60 Series: Models: S60TPF, S60TPG
- SONOFF WiFi POW Smart Power Meter Switch | Models: POWR316, POWR316D, POWR320D
- SONOFF WiFi POW Ring Smart Power Meter | Models: POWCT
- SONOFF WiFi MINI Dry Smart Switch | Model: MINI-D
- SONOFF WiFi MINI Smart Switch | Models: MINI-R4, MINI-R4M
- SONOFF WiFi Smart Gate Controller (Prototype device)
- SONOFF WiFi SwitchMan 1-3 Channel Switch | Model: M5-1C, M5-2C, M5-3C [Not tested, let me know if it works]
- SONOFF WiFi TX ULTIMATE 1-4 Channel Wall Switch | Model: T5-1C, T5-2C, T5-3C, T5-4C [Partially tested, let me know if it works]
- SONOFF Zigbee Bridge | Model: ZB-Bridge-Pro
- SONOFF Zigbee Bridge | Model: ZB-Bridge-Ultra
- SONOFF Zigbee Sensor: Temp and Humidity | Models: SNZB-02P, SNZB-02D
- SONOFF Zigbee Sensor: Door and Window: | Model: SNZB-04P (1st version) [Not tested, let me know if it works]

**Added: GENERIC Devices**
- 3 Way Wall Switch | Model: Bouffalo Lab BL602 (CK-BL602-4SW-HS)

### 🔧 Development Enhancements
- **Comprehensive testing framework** with 100+ test methods
- **Automated CI/CD scripts** with quality gates
- **Mock-based unit testing** with Mockito integration
- **Professional development workflow** with detailed progress tracking

## 📋 Quick Links

- **📦 [Latest Release](https://github.com/tschaban/openhab-addons-sonoff/releases)** - Download the newest version
- **🐛 [Report Issues](https://github.com/tschaban/openhab-addons-sonoff/issues)** - Bug reports and feature requests
- **📈 [Changelog](https://github.com/tschaban/openhab-addons-sonoff/milestones?state=closed)** - All enhancements and fixes
- **👨‍💻 [Development Docs](docs/development/testing-framework.md)** - For developers and contributors

---

## 📖 About This Binding

The Sonoff binding allows control and monitoring of eWeLink-based devices using both cloud and local LAN connections. This enhanced version extends the original functionality with additional device support and improved reliability.

### 🌐 Connection Modes
- **Cloud Mode** - Connect through eWeLink cloud services
- **Local Mode** - Direct LAN communication (where supported)
- **Mixed Mode** - Automatic fallback between local and cloud

### 🔌 Supported Things

Currently known to support (non-exhaustive list):

#### Mixed Mode Support (Local + Cloud)
- **UUID1:** S20, S26, Basic, Mini, Mini PCIe Card
- **UUID2:** Unknown Models
- **UUID3:** Unknown Models
- **UUID4:** Unknown Models
- **UUID5:** POW
- **UUID6:** T11C, TX1C, G1
- **UUID7:** T12C, TX2C
- **UUID8:** T13C, TX3C
- **UUID9:** Unknown Models
- **UUID15:** TH10, TH16
- **UUID28:** RFBRIDGE (Only sensors currently supported, awaiting remote logs)
- **UUID32:** POWR2
- **UUID77:** MICRO USB

#### Cloud Only Support
- **UUID24:** 1 Channel GSM Socket
- **UUID27:** 1 Channel GSM Socket
- **UUID29:** 2 Channel GSM Socket
- **UUID30:** 3 Channel GSM Socket
- **UUID31:** 4 Channel GSM Socket
- **UUID66:** Zigbee Bridge
- **UUID81:** 1 Channel GSM Socket
- **UUID82:** 2 Channel GSM Socket
- **UUID83:** 3 Channel GSM Socket
- **UUID84:** 4 Channel GSM Socket
- **UUID107:** 1 Channel GSM Socket
- **UUID2026:** Zigbee Motion Sensor

## 🛠️ Setup

### Initial Configuration
1. **Add Account Thing**
   - Email: Your eWeLink email address
   - Password: Your eWeLink password
   - Access Mode: Choose your preferred mode (`local`, `cloud`, or `mixed`)

2. **Run Discovery**
   - The account should come online automatically
   - Run discovery to create device cache
   - Cache is required even when using text files

3. **Add Devices**
   - Use automatic discovery (recommended)
   - Or manually add via text files after cache creation

> **⚠️ Known Issue:** If using text files and devices don't come online after config changes, remove and re-add the file. In rare cases, you may need to remove and re-add the binding.

## 🔍 Discovery

- **Initial Discovery:** Run after account setup to create device cache
- **Automatic Discovery:** All devices support automatic discovery
- **Sub-devices:** For RF Bridge or Zigbee bridge sensors, add the main device first, then run discovery again

## 🌍 Local vs Cloud Operation

### Local Mode Benefits
- **Faster response** - Direct LAN communication
- **Privacy** - No external internet dependency
- **Firewall friendly** - Block external access while maintaining functionality

### Device Compatibility
- **Local Supported:** Most switches, POW devices, RF Bridge
- **Cloud Only:** Zigbee Bridge, GSM sockets, some sensors
- **Mixed Mode:** Automatically uses local when available, falls back to cloud

### POW/POWR2 Special Configuration

**Local Mode Energy Data:**
- Enable Local Polling: On/Off
- Polling Interval: Seconds between polls (not required in LAN Development mode)

**Cloud Mode Consumption Data:**
- Enable Consumption Polling: On/Off
- Polling Interval: Recommended 24 hours (86400 seconds) for consumption data

> **💡 Performance Tip:** Consumption data polling is resource-intensive. Use longer intervals (24+ hours) for consumption statistics.

## ⚙️ Configuration Examples

### Bridge Configuration
```
Bridge sonoff:account:uniqueName "Sonoff Account" @ "myLocation" 
[ email="account@example.com", password="myPassword", accessmode="mixed"] {
    
    // POW Device with consumption polling
    32 PowR2 "PowR2" @ "thingLocation" 
    [ deviceid="1000bd9fe9", local=false, localPoll=10, consumption=false, consumptionPoll=10]
    
    // USB Switch with local polling
    77 USBSwitch "USB Switch" @ "thingLocation" 
    [ deviceid="1000dc155b", local=false, localPoll=10 ]
    
    // RF Bridge with sensors and remotes
    Bridge sonoff:28:uniqueName:RFBridge "RFBridge" @ "thingLocation" 
    [ deviceid="1000e72cb8" ] { 
        rfsensor DoorContact "Door Contact" @ "contactLocation" [ deviceid="0" ]
        rfsensor WindowContact "Window Contact" @ "contactLocation" [ deviceid="1" ]
        rfsensor PIRSensor "PIR Sensor" @ "sensorLocation" [ deviceid="2" ]
        rfremote2 Remote1 "2 Button Remote" @ "wherever" [ deviceid="3" ]
        rfremote4 Remote2 "4 Button Remote" @ "wherever" [ deviceid="5" ]
    }
    
    // Zigbee Bridge with motion sensor
    Bridge sonoff:66:benfleet:ZigbeeBridge "Zigbee Bridge" @ "bridgeLocation" 
    [ deviceid="1000f60f3d"] {
        zmotion MotionSensor "Motion Sensor" @ "sensorLocation" [ deviceid="a48000a933"]
    }
}
```

### Item Configuration Examples

#### Main Device Items
```
// POW Device Channels
Switch      Switch          "Switch"                    {channel="sonoff:32:uniqueName:PowR2:switch"}
Number      Current         "Current"                   {channel="sonoff:32:uniqueName:PowR2:current"}
Number      Voltage         "Voltage"                   {channel="sonoff:32:uniqueName:PowR2:voltage"}
Number      Power           "Power"                     {channel="sonoff:32:uniqueName:PowR2:power"}
Number      Today           "Energy Usage Today"        {channel="sonoff:32:uniqueName:PowR2:todayKwh"}
Number      Yesterday       "Energy Usage Yesterday"    {channel="sonoff:32:uniqueName:PowR2:yesterdayKwh"}
String      CloudConnected  "Cloud Connected"           {channel="sonoff:32:uniqueName:PowR2:cloudOnline"}
String      LocalConnected  "LAN Connected"             {channel="sonoff:32:uniqueName:PowR2:localOnline"}
Number      Rssi            "Signal Strength"           {channel="sonoff:32:uniqueName:PowR2:rssi"}
```

#### RF Sensor Items
```
DateTime    DoorOpened      "Door Opened"               {channel="sonoff:rfsensor:uniqueName:RFBridge:DoorContact:rf0External"}
DateTime    WindowOpened    "Window Opened"             {channel="sonoff:rfsensor:uniqueName:RFBridge:WindowContact:rf0External"}
DateTime    MotionDetected  "Motion Detected"           {channel="sonoff:rfsensor:uniqueName:RFBridge:PIRSensor:rf0External"}
```

#### RF Remote Items
```
Switch      Remote1Arm      "Arm Alarm"                 {channel="sonoff:rfremote2:uniqueName:RFBridge:Remote1:button0"}
Switch      Remote1Disarm   "Disarm Alarm"              {channel="sonoff:rfremote2:uniqueName:RFBridge:Remote1:button1"}
DateTime    Remote1External "Remote Triggered"          {channel="sonoff:rfremote2:uniqueName:RFBridge:Remote1:rf0External"}
DateTime    Remote1Internal "OpenHAB Triggered"         {channel="sonoff:rfremote2:uniqueName:RFBridge:Remote1:rf0Internal"}
```

#### Zigbee Sensor Items
```
Switch      MotionDetected      "Motion Detected"       {channel="sonoff:zmotion:uniqueName:ZigbeeBridge:MotionSensor:motion"}
Number      MotionSensorBattery "PIR Battery Level"     {channel="sonoff:zmotion:uniqueName:ZigbeeBridge:MotionSensor:battery"}
DateTime    MotionActivated     "PIR Activated"         {channel="sonoff:zmotion:uniqueName:ZigbeeBridge:MotionSensor:trigTime"}
```

## 🔧 Development Documentation

For developers working on the Sonoff binding, comprehensive technical documentation is available:

- **[Development Setup Guide](docs/development/setup-guide.md)** - Complete development environment setup and workflow guide

## 🐛 Bug Reports & Support

### For Smart'nyDom Enhanced Features
- **Issues:** [GitHub Issues](https://github.com/tschaban/openhab-addons-sonoff/issues)
- **Enhancements:** [Milestones](https://github.com/tschaban/openhab-addons-sonoff/milestones?state=closed)

### For Original Binding Issues
- **Original Repository:** [delid4ve/openhab-sonoff](https://github.com/delid4ve/openhab-sonoff/issues)

### When Reporting Issues
Please include:
- **Version** you're using
- **Debug log** information
- **Device file** from `userdata/sonoff/deviceid.txt`
- **Device model** and behavior description

## 🙏 Acknowledgments

### Original Development
This binding is based on the excellent work by **[delid4ve](https://github.com/delid4ve/openhab-3.x-sonoff)** and the OpenHAB community. The original binding provided the foundation for all eWeLink device integration.

### Community Contributors
Special thanks to the community members who made this possible:
- **[skydiver](https://github.com/skydiver)** - Core contributions
- **[bwp91](https://github.com/bwp91)** - Protocol insights
- **[AlexxIT](https://github.com/AlexxIT)** - Device support
- **[RealZimboGuy](https://github.com/RealZimboGuy)** - Testing and feedback

### Smart'nyDom Enhancements
Enhanced and maintained by **[Smart'nyDom](https://github.com/tschaban)** with focus on:
- Extended device support for newer SONOFF models
- Improved testing framework and code quality
- Enhanced development workflow and automation
- Comprehensive documentation and examples

---

**📄 License:** This project maintains the same license as the original OpenHAB binding.

**🔗 Links:** [Original Repository](https://github.com/delid4ve/openhab-3.x-sonoff) | [Enhanced Repository](https://github.com/tschaban/openhab-addons-sonoff) | [OpenHAB Community](https://community.openhab.org/)