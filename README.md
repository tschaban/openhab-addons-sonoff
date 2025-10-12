# Sonoff Binding - Smart'nyDom Enhanced Edition

[![GitHub Release](https://img.shields.io/github/v/release/tschaban/openhab-addons-sonoff)](https://github.com/tschaban/openhab-addons-sonoff/releases)
[![GitHub Issues](https://img.shields.io/github/issues/tschaban/openhab-addons-sonoff)](https://github.com/tschaban/openhab-addons-sonoff/issues)
[![GitHub Milestones](https://img.shields.io/github/milestones/closed/tschaban/openhab-addons-sonoff)](https://github.com/tschaban/openhab-addons-sonoff/milestones?state=closed)
[![CI/CD Pipeline](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/ci.yml/badge.svg)](https://github.com/tschaban/openhab-addons-sonoff/actions/workflows/ci.yml)

**Enhanced and maintained by [Smart'nyDom](https://github.com/tschaban)** | Based on the original work by [delid4ve](https://github.com/delid4ve/openhab-3.x-sonoff)

This is an enhanced branch of the Sonoff binding for OpenHAB, featuring extended device support, improved functionality, with testing framework.

## 📋 Quick Links

- **📦 [Latest Release](https://github.com/tschaban/openhab-addons-sonoff/releases)** - Download the newest version
- **🐛 [Report Issues](https://github.com/tschaban/openhab-addons-sonoff/issues)** - Bug reports and feature requests
- **📈 [Changelog](https://github.com/tschaban/openhab-addons-sonoff/milestones?state=closed)** - All enhancements and fixes

---

## 📖 About This Binding

The Sonoff binding allows control and monitoring of eWeLink-based devices using both cloud and local LAN connections. This enhanced version extends the original functionality with additional device support and improved reliability.

### 🌐 Connection Modes
- **Cloud Mode** - Connect through eWeLink cloud services
- **Local Mode** - Direct LAN communication (where supported)
- **Mixed Mode** - Automatic fallback between local and cloud

### 🔌 Complete Device Support Matrix

#### Legend
**Connection Types:**
- 🌐 **Cloud**: Cloud-only connection via eWeLink servers
- 🔄 **Mixed**: Both local LAN and cloud connections supported

**Testing Status:**
- ⚠️ **Testing needed**:Limited testing (I don't have that device to test it) feedback welcome

#### Device supported

| UUID | Models | Connection | Features | Status |
|------|--------|------------|----------|--------|
| **1** | Sonoff S20, [S26](https://s.smartnydom.pl/r/sonoff-s26), [BasicR1](https://s.smartnydom.pl/r/sonoff-basic-r4), [BasicR2](https://s.smartnydom.pl/r/sonoff-basic-r4), Mini, Mini PCIe Card | 🔄 Mixed | Single relay switch | |
| **2** | Sonoff DUALR2 | 🔄 Mixed | Dual relay switch | |
| **3** | Unknown Models | 🔄 Mixed | Socket (3 channels) | |
| **4** | [Sonoff 4CHPro R3](https://s.smartnydom.pl/r/sonoff-4chr3) | 🔄 Mixed | Socket (4 channels) | |
| **5** | [Sonoff POW](https://s.smartnydom.pl/r/sonoff-pow-r2-yt) | 🔄 Mixed | Power monitoring switch | |
| **6** | Sonoff [T11C](https://s.smartnydom.pl/r/sonoff-wall-switches), [TX1C](https://s.smartnydom.pl/r/sonoff-tx-sd), [G1](https://s.smartnydom.pl/r/sonoff-wall-switches) | 🔄 Mixed | Single touch switch | |
| **7** | Sonoff [T12C](https://s.smartnydom.pl/r/sonoff-wall-switches), [TX2C](https://s.smartnydom.pl/r/sonoff-tx-sd) | 🔄 Mixed | Dual touch switch | |
| **8** | Sonoff [T13C](https://s.smartnydom.pl/r/sonoff-wall-switches), [TX3C](https://s.smartnydom.pl/r/sonoff-tx-sd) | 🔄 Mixed | Triple touch switch | |
| **9** | Unknown Models | 🔄 Mixed | Switch (4 channels) | |
| **14** | [Sonoff BasicR1](https://s.smartnydom.pl/r/sonoff-basic-r4) | 🔄 Mixed | Single relay switch | |
| **15** | [Sonoff TH10, TH16, TH16R2](https://s.smartnydom.pl/r/sonoff-th-origin-sd) | 🔄 Mixed | Temperature/humidity monitoring | |
| **24** | GSM Socket | 🌐 Cloud | Single channel GSM socket | |
| **27** | GSM Socket | 🌐 Cloud | Single channel GSM socket | |
| **28** | RF-BRIDGE (RF3) | 🔄 Mixed | 433MHz RF bridge | |
| **29** | GSM Socket | 🌐 Cloud | Dual channel GSM socket | |
| **30** | GSM Socket | 🌐 Cloud | Triple channel GSM socket | |
| **31** | GSM Socket | 🌐 Cloud | Quad channel GSM socket | |
| **32** | [Sonoff POWR2, POWR316, POWR320D](https://s.smartnydom.pl/r/sonoff-pow-r2-yt) | 🔄 Mixed | Advanced power monitoring | |
| **59** | LED Controller | 🔄 Mixed | LED strip controller | |
| **66** | [Sonoff ZigBee Bridge](https://s.smartnydom.pl/r/sonoff-zigbee-sensors) | 🌐 Cloud | Zigbee bridge | |
| **77** | Sonoff WiFi MICRO (USB) | 🔄 Mixed | Compact WiFi switch | |
| **78** | Unknown | 🔄 Mixed | | |
| **81** | GSM Socket | 🌐 Cloud | Single channel GSM socket | |
| **82** | GSM Socket | 🌐 Cloud | Dual channel GSM socket | |
| **83** | GSM Socket | 🌐 Cloud | Triple channel GSM socket | |
| **84** | GSM Socket | 🌐 Cloud | Quad channel GSM socket | |
| **102** | OPL-DMA, DW2 | 🔄 Mixed | Magnetic door/window sensor | |
| **104** | B05 Bulb | 🔄 Mixed | Smart bulb | |
| **107** | GSM Socket | 🌐 Cloud | Single channel GSM socket | |
| **126** | [Sonoff DUAL R3](https://s.smartnydom.pl/r/sonoff-dual-r3) | 🔄 Mixed | Dual relay + power monitoring | |
| **138** | Sonoff [MINI-D](https://s.smartnydom.pl/r/sonoff-mini-d-itead), [MINI-R4](	https://s.smartnydom.pl/r/sonoff-mini-extreme-r4), [MINI-R4M](https://s.smartnydom.pl/r/sonoff-minir4-matter) | 🔄 Mixed | Single relay | |
| **140** | CK-BL602-4SW-HS (Bouffalo Lab BL602) | 🔄 Mixed | 3-way wall switch | |
| **160** | [Sonoff M5-1C](https://s.smartnydom.pl/r/sonoff-switchman-m5) | 🔄 Mixed | Single channel switch | ⚠️ Testing needed |
| **161** | [Sonoff M5-2C](https://s.smartnydom.pl/r/sonoff-switchman-m5) | 🔄 Mixed | Dual channel switch | ⚠️ Testing needed |
| **162** | [Sonoff M5-3C](https://s.smartnydom.pl/r/sonoff-switchman-m5) | 🔄 Mixed | Triple channel switch | ⚠️ Testing needed |
| **168** | [Sonoff ZigBee ZBBridge-P](https://s.smartnydom.pl/r/sonoff-zb-bridge-pro) | 🌐 Cloud | Zigbee bridge Pro | |
| **181** | [Sonoff THR320D, THR316D](https://s.smartnydom.pl/r/sonoff-th-elite-smart-temperature-and-humidity-monitoring-switch-yt) | 🔄 Mixed | Single relay with Temperature/humidity sensor | |
| **190** | [Sonoff S60TPF, S60TPG](https://s.smartnydom.pl/r/sonoff-s60tpf) | 🔄 Mixed | Smart plug | |
| **209** | [Sonoff T5-1C-86](https://s.smartnydom.pl/r/sonoff-wall-switches) | 🔄 Mixed | Single touch switch | ⚠️ Testing needed |
| **210** | [Sonoff T5-2C-86](https://s.smartnydom.pl/r/sonoff-wall-switches) | 🔄 Mixed | Dual touch switch | ⚠️ Testing needed |
| **211** | [Sonoff T5-3C-86](https://s.smartnydom.pl/r/sonoff-wall-switches) | 🔄 Mixed | Triple touch switch | ⚠️ Testing needed |
| **212** | [Sonoff T5-4C-86](https://s.smartnydom.pl/r/sonoff-wall-switches) | 🔄 Mixed | Quad touch switch | ⚠️ Testing needed |
| **237** | Sonoff SG200 | 🔄 Mixed | Smart gateway | Prototype device |
| **243** | [Sonoff ZBridge-U](https://s.smartnydom.pl/r/sonoff-zbbridge-u) | 🌐 Cloud | Zigbee bridge USB | |
| **256** | [Sonoff SlimCAM2](https://s.smartnydom.pl/r/sonoff-cam-pan-tilt-2-itead-eu) | 🌐 Cloud | Security camera | |
| **260** | [Sonoff CAM-B1P](https://s.smartnydom.pl/r/sonoff-b1p-itead) | 🌐 Cloud | Security camera | |
| **268** | [Sonoff BASIC-1GS](https://s.smartnydom.pl/r/sonoff-basic-1gs-itead-en) | 🔄 Mixed | BASIC 5Gen single switch with Matter support | |
| **1770** | [Sonoff ZigBee SNZB-02](https://s.smartnydom.pl/r/sonoff-zigbee-sensors) | 🌐 Cloud | Temperature monitoring, 1st version| |
| **2026** | [Sonoff ZigBee Motion Sensor](https://s.smartnydom.pl/r/sonoff-zigbee-sensors) | 🌐 Cloud | Motion detection, 1st version | |
| **7000** | Sonoff [SNZB-01P](https://s.smartnydom.pl/r/sonoff-snzb-01p) | 🌐 Cloud | Wireless ZigBee switch (single/double/long press) | |
| **7003** | Sonoff [SNZB-04P](https://s.smartnydom.pl/r/sonoff-snzb-04p-itead) | 🌐 Cloud | Door/window sensor | ⚠️ Testing needed |
| **7014** | Sonoff ZigBee [SNZB-02P](https://s.smartnydom.pl/r/sonoff-snzb-02p), [SNZB-02D](	https://s.smartnydom.pl/r/sonoff-snzb-02d-sd) | 🌐 Cloud | Temperature/humidity sensor | |
| **ZCONTACT** | Generic Contact Sensor | 🌐 Cloud | Contact detection | |
| **ZWATER** | Water Leak Sensor | 🌐 Cloud | Water leak detection | |
| **ZSWITCH1** | Single Channel Switch | 🌐 Cloud | Single relay | |
| **ZSWITCH2** | Dual Channel Switch | 🌐 Cloud | Dual relay | |
| **ZSWITCH3** | Triple Channel Switch | 🌐 Cloud | Triple relay | |
| **ZSWITCH4** | Quad Channel Switch | 🌐 Cloud | Quad relay | |
| **ZLIGHT** | Dimmable Light | 🌐 Cloud | Dimmable white light | |
| **RF1** | Single Button Remote | 🔄 Mixed | 1 button control | |
| **RF2** | Dual Button Remote | 🔄 Mixed | 2 button control | |
| **RF3** | Triple Button Remote | 🔄 Mixed | 3 button control | |
| **RF4** | Quad Button Remote | 🔄 Mixed | 4 button control | |
| **RF6** | RF Sensor | 🔄 Mixed | Motion/contact detection | |


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