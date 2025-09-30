# Sonoff Binding - Smart'nyDom Enhanced Edition

[![GitHub Release](https://img.shields.io/github/v/release/tschaban/openhab-addons-sonoff)](https://github.com/tschaban/openhab-addons-sonoff/releases)
[![GitHub Issues](https://img.shields.io/github/issues/tschaban/openhab-addons-sonoff)](https://github.com/tschaban/openhab-addons-sonoff/issues)
[![GitHub Milestones](https://img.shields.io/github/milestones/closed/tschaban/openhab-addons-sonoff)](https://github.com/tschaban/openhab-addons-sonoff/milestones?state=closed)
[![CI/CD Pipeline](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/ci.yml/badge.svg)](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/ci.yml)


**Enhanced and maintained by [Smart'nyDom](https://github.com/tschaban)** | Based on the original work by [delid4ve](https://github.com/delid4ve/openhab-3.x-sonoff)

This is an enhanced branch of the original Sonoff binding for OpenHAB, featuring extended device support, improved functionality, and comprehensive testing framework.

## 🚀 What's New in This Branch

### 📱 Enhanced Device Support - Smart'nyDom Edition

This enhanced branch extends the original Sonoff binding with **89+ supported devices** across 8 categories, featuring the latest Sonoff models and comprehensive technical specifications.

#### 🆕 **Recently Added Devices (2024-2025)**
| Device | Model | Features | Power | Status |
|--------|-------|----------|-------|--------|
| **S60 Series Smart Plug** | S60TPF, S60TPG | Type E/F/G WiFi plug | 16A | ✅ Tested |
| **POW Elite Series** | POWR316, POWR316D, POWR320D | Advanced power monitoring | 16A/20A | ✅ Tested |
| **POWCT Ring Meter** | POWCT | Split core current transformer | N/A | ✅ Tested |
| **MINI-D Dry Switch** | MINI-D | Matter-enabled dry contact | 10A | ✅ Tested |
| **MINI Extreme** | MINI-R4, MINI-R4M | Ultra small, detach relay mode | 10A | ✅ Tested |
| **SwitchMan M5** | M5-1C, M5-2C, M5-3C | 1-3 gang wall switches | 10A/gang | ⚠️ Testing needed |
| **TX Ultimate** | T5-1C, T5-2C, T5-3C, T5-4C | Touch wall switches | 10A/gang | ⚠️ Partially tested |
| **ZBBridge Pro** | ZB-Bridge-Pro | Enhanced Zigbee coordinator | N/A | ✅ Tested |
| **ZBBridge Ultra** | ZB-Bridge-Ultra | Zigbee + Matter bridge | N/A | ✅ Tested |
| **SNZB Sensors** | SNZB-02P, SNZB-02D, SNZB-04P | Temp/humidity, door/window | N/A | ⚠️ Testing needed |
| **Generic 3-Way Switch** | CK-BL602-4SW-HS | Bouffalo Lab BL602 | 10A | ✅ Tested |

#### 🔧 **Development & Testing Enhancements**
- **Comprehensive testing framework** with unit and integration tests
- **Bridge handler foundation testing** for abstract class coverage  
- **Automated CI/CD scripts** with quality gates
- **Mock-based unit testing** with Mockito integration
- **Device validation system** for new device additions

## 📋 Quick Links

- **📦 [Latest Release](https://github.com/tschaban/openhab-addons-sonoff/releases)** - Download the newest version
- **🐛 [Report Issues](https://github.com/tschaban/openhab-addons-sonoff/issues)** - Bug reports and feature requests
- **📈 [Changelog](https://github.com/tschaban/openhab-addons-sonoff/milestones?state=closed)** - All enhancements and fixes
- **👨‍💻 [Development Docs](docs/development/README.md)** - For developers and contributors

---

## 📖 About This Binding

The Sonoff binding allows control and monitoring of eWeLink-based devices using both cloud and local LAN connections. This enhanced version extends the original functionality with additional device support and improved reliability.

### 🌐 Connection Modes
- **Cloud Mode** - Connect through eWeLink cloud services
- **Local Mode** - Direct LAN communication (where supported)
- **Mixed Mode** - Automatic fallback between local and cloud

### 🔌 Complete Device Support Matrix

**89+ Sonoff devices supported** across 8 categories. Enhanced with official [Itead.cc](https://itead.cc/) manufacturer specifications and Smart'nyDom testing status.

#### Legend
**Connection Types:**
- 🌐 **Cloud**: Cloud-only connection via eWeLink servers
- 🔄 **Mixed**: Both local LAN and cloud connections supported

**Smart'nyDom Testing Status:**
- ✅ **Fully tested**: Confirmed working with comprehensive testing
- ✅ **Smart'nyDom Enhanced**: Newly added/improved in this branch
- ⚠️ **Testing needed**: Added but requires community feedback
- 🔄 **Awaiting integration**: Latest Itead releases pending implementation

#### Core Device Support (UUID-based)

| UUID | Models | Connection | Features | Power Rating | Smart'nyDom Status |
|------|--------|------------|----------|--------------|-------------------|
| **1** | S20, S26, BasicR1, BasicR2, Mini, Mini PCIe Card | 🔄 Mixed | Single relay switch | 10A/16A | ✅ Fully tested |
| **2** | DUALR2 | 🔄 Mixed | Dual relay switch | 10A per channel | ✅ Fully tested |
| **4** | 4CHPROR3 | 🔄 Mixed | 4-channel relay | 10A per channel | ✅ Fully tested |
| **5** | POW | 🔄 Mixed | Power monitoring switch | 16A/3500W | ✅ Fully tested |
| **6** | T11C, TX1C, G1 | 🔄 Mixed | Single touch switch | 10A | ✅ Fully tested |
| **7** | T12C, TX2C | 🔄 Mixed | Dual touch switch | 10A per gang | ✅ Fully tested |
| **8** | T13C, TX3C | 🔄 Mixed | Triple touch switch | 10A per gang | ✅ Fully tested |
| **15** | TH10, TH16, TH16R2, TH Origin | 🔄 Mixed | Temperature/humidity monitoring | 10A/16A/20A | ✅ Enhanced versions tested |
| **28** | RF-BRIDGE (RF3), RF BridgeR2 433 | 🔄 Mixed | 433MHz RF bridge | N/A | ✅ Fully tested |
| **32** | POWR2, POWR316, POWR316D, POWR320D, POW Origin | 🔄 Mixed | Advanced power monitoring | 16A/20A | ✅ **Smart'nyDom Enhanced** |
| **66** | ZB Bridge, ZBBridge Pro | 🌐 Cloud | Zigbee bridge | N/A | ✅ **Smart'nyDom Enhanced** |
| **77** | WiFi MICRO (USB), Micro | 🔄 Mixed | Compact WiFi switch | 5V/10A | ✅ Fully tested |
| **102** | OPL-DMA, DW2, DW2-WIFI | 🔄 Mixed | Door/window sensor | N/A | ✅ Fully tested |
| **104** | B05 Bulb, B02-BL, B05-BL | 🔄 Mixed | Smart bulb | 9W/E27 | ✅ Fully tested |
| **126** | DUAL R3, DUALR3 Lite | 🔄 Mixed | Dual relay + power monitoring | 16A per channel | ✅ Fully tested |
| **138** | MINI-D, MINI-R4, MINI-R4M, MINIR4M (Matter) | 🔄 Mixed | Compact dual relay | 10A | ✅ **Smart'nyDom Enhanced** |
| **140** | CK-BL602-4SW-HS (Bouffalo Lab BL602) | 🔄 Mixed | 3-way wall switch | 10A | ✅ **Smart'nyDom Added** |
| **160-162** | M5-1C/2C/3C, SwitchMan M5 | 🔄 Mixed | 1-3 channel switches | 10A per gang | ⚠️ **Testing needed** |
| **168** | ZBBridge-P, ZBBridge Pro | 🌐 Cloud | Zigbee bridge Pro | N/A | ✅ **Smart'nyDom Enhanced** |
| **181** | THR320D, THR316D, TH Elite | 🔄 Mixed | Temperature/humidity sensor | 16A/20A | ✅ Fully tested |
| **190** | S60TPF4, S60TPF, S60TPG, S60 Series | 🔄 Mixed | Smart plug | 16A | ✅ **Smart'nyDom Enhanced** |
| **209-212** | T5-1C/2C/3C/4C-86, TX Ultimate | 🔄 Mixed | 1-4 gang touch switches | 10A per gang | ⚠️ **Partially tested** |
| **243** | ZBridge-U, ZBBridge Ultra | 🌐 Cloud | Zigbee bridge USB | N/A | ✅ **Smart'nyDom Enhanced** |
| **256** | SlimCAM2, CAM Slim Gen2 | 🌐 Cloud | Security camera | N/A | ✅ Fully tested |
| **2026** | Motion Sensor, SNZB-03P | 🌐 Cloud | Motion detection | N/A | ✅ **Smart'nyDom Enhanced** |
| **7003** | SNZB-04P | 🌐 Cloud | Door/window sensor | N/A | ⚠️ **Testing needed** |
| **7014** | SNZB-02P, SNZB-02D | 🌐 Cloud | Temperature/humidity sensor | N/A | ✅ **Smart'nyDom Enhanced** |

#### GSM Socket Support (Cloud Only)
- **UUID24, 27, 81, 107:** Single channel GSM sockets (10A)
- **UUID29, 82:** Dual channel GSM sockets (10A per channel)  
- **UUID30, 83:** Triple channel GSM sockets (10A per channel)
- **UUID31, 84:** Quad channel GSM sockets (10A per channel)

#### 🆕 Smart'nyDom Enhanced & Latest Additions (2024-2025)

**Fully Tested & Integrated:**
- ✅ **S60 Series (UUID 190):** S60TPF, S60TPG - Type E/F/G smart plugs (16A)
- ✅ **POW Elite Series (UUID 32):** POWR316, POWR316D, POWR320D - Advanced power monitoring (16A/20A)
- ✅ **POWCT:** Split core current transformer for non-invasive monitoring
- ✅ **MINI-D (UUID 138):** Matter-enabled dry contact switch (10A)
- ✅ **MINI Extreme (UUID 138):** MINI-R4, MINI-R4M - Ultra small switches (10A)
- ✅ **ZBBridge Pro/Ultra (UUID 66/243):** Enhanced Zigbee coordinators with Matter support
- ✅ **SNZB Sensors (UUID 2026/7014):** SNZB-02P, SNZB-02D - Temp/humidity with LCD
- ✅ **Generic 3-Way (UUID 140):** CK-BL602-4SW-HS - Bouffalo Lab BL602 switch

**Partially Tested - Feedback Welcome:**
- ⚠️ **SwitchMan M5 (UUID 160-162):** M5-1C/2C/3C - 1-3 gang wall switches (10A/gang)
- ⚠️ **TX Ultimate (UUID 209-212):** T5-1C/2C/3C/4C - Touch wall switches (10A/gang)
- ⚠️ **SNZB-04P (UUID 7003):** Door/window sensor (1st version)

**Latest Itead Releases (Awaiting Integration):**
- 🔄 **MINI-RBS:** Smart Roller Shutter Switch with motor control
- 🔄 **ZBMINIR2:** Zigbee router with detach relay mode
- 🔄 **POWR3:** 25A/5500W high-power switch
- 🔄 **NSPanel Pro:** Smart home control panel (86/120 Type)
- 🔄 **ZBM5:** Zigbee wall switches (1-3 gang, 80/86/120 Type)
- 🔄 **M5 Matter:** Matter-enabled wall switches
- 🔄 **iHost:** Local smart home hub

#### 📊 Device Categories Summary

| Category | Models | Power Range | Smart'nyDom Status | Key Features |
|----------|--------|-------------|-------------------|--------------|
| **DIY Smart Switches** | 28+ | 5V-25A, up to 5500W | ✅ **Enhanced** | Power monitoring, Matter support, detach relay |
| **Smart Wall Switches** | 7+ | 1-4 gang, 10A/gang | ⚠️ **Partial** | Touch/traditional, 80/86/120 Type, Ultimate series |
| **Smart Plugs** | 8+ | 15A-16A | ✅ **Enhanced** | Type A/B/E/F/G/H/N, power monitoring |
| **Gateway & Sensors** | 19+ | N/A | ✅ **Enhanced** | Zigbee 3.0, RF 433MHz, Matter bridge, LCD sensors |
| **Security Cameras** | 3+ | N/A | ✅ **Tested** | Indoor/outdoor, pan/tilt, Gen2 features |
| **Smart Lighting** | 10+ | 9W-400W | ✅ **Tested** | RGB/RGBW/RGBIC, waterproof strips, dimmers |
| **Home Appliances** | 3+ | N/A | 🔄 **Pending** | BBQ thermometer, water valve, TRV |
| **Accessories** | 8+ | N/A | ✅ **Tested** | Enclosures, adapters, cables, covers |

**Total Supported**: **89+ devices** across **8 categories**

> **📋 Complete Device List:** See [SUPPORTED_DEVICES.md](docs/SUPPORTED_DEVICES.md) for comprehensive device specifications and UUID mappings.

> **🏭 Manufacturer:** All devices manufactured by [Itead Co., Ltd.](https://itead.cc/) - Official Sonoff producer.

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

- **[Development Documentation](docs/development/README.md)** - Development guide and resources
- **[Testing Documentation](docs/testing/README.md)** - Testing framework overview
- **[Testing Framework](docs/development/testing-framework.md)** - Test execution and coverage
- **[CI/CD Automation](docs/development/cicd-automation.md)** - Automated workflows

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

**🔗 Links:** [Original Repository](https://github.com/delid4ve/openhab-3.x-sonoff) | [Enhanced Repository](https://github.com/tschaban/openhab-addons-sonoff)