# Supported Devices - Sonoff Binding

This document provides a comprehensive list of all devices supported by the Sonoff binding, organized by their UUID (Universal Unique Identifier) as defined in the eWeLink protocol.

## Overview

Each Sonoff device is identified by a UUID (Universal Unique Identifier) number that determines its capabilities, channels, and supported features. This UUID is used internally by the binding to determine the correct device handler and available channels.

The UUID corresponds to the `THING_TYPE_X` constants defined in [`SonoffBindingConstants.java`](../src/main/java/org/openhab/binding/sonoff/internal/SonoffBindingConstants.java), where X is typically the UUID number (e.g., UUID 1 → `THING_TYPE_1`).

## Quick Reference

| Category | UUID Range | Count | Examples |
|----------|------------|-------|----------|
| **WiFi Devices** | 1-237 | 45+ | Switches, sensors, power monitoring |
| **Zigbee Bridges** | 66, 168, 243 | 3 | ZB Bridge, ZBBridge-P, ZBridge-U |
| **433MHz RF Bridge** | 28 | 1 | RF-BRIDGE (RF3) |
| **Camera Devices** | 256, 260 | 2 | SlimCAM2, CAM-B1P |
| **Zigbee Child Devices** | 1770, 2026, 7003, 7014, Z* | 11 | Motion sensors, switches, lights |
| **433MHz RF Child Devices** | RF* | 5 | Remote controls, RF sensors |

## Device Support Matrix

### Legend
- **UUID**: Device type identifier used in eWeLink protocol
- **Connection**: Supported connection modes
  - 🌐 **Cloud**: Cloud-only connection via eWeLink servers
  - 🏠 **Local**: Local LAN connection supported
  - 🔄 **Mixed**: Both local and cloud connections supported
- **Features**: Device capabilities and channels

---

## WiFi Devices

### Basic Switches and Relays

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **1** | S20, S26, BasicR1, BasicR2, Mini, Mini PCIe Card | 🔄 Mixed | Single relay switch | Most common single-channel devices |
| **2** | DUALR2 | 🔄 Mixed | Dual relay switch | Two independent relay channels |
| **3** | Unknown Models | 🔄 Mixed | Socket (3 channels) | TODO: Identify specific models |
| **4** | 4CHPROR3 | 🔄 Mixed | Socket (4 channels) | Four-channel relay device |
| **9** | Unknown Models | 🔄 Mixed | Switch (4 channels) | TODO: Identify specific models |
| **14** | BasicR1 (older firmware) | 🔄 Mixed | Single relay switch | Legacy basic switch |

### Touch Switches

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **6** | T11C, TX1C, G1 | 🔄 Mixed | Single touch switch | Wall-mounted touch switches |
| **7** | T12C, TX2C | 🔄 Mixed | Dual touch switch | Two-gang touch switches |
| **8** | T13C, TX3C | 🔄 Mixed | Triple touch switch | Three-gang touch switches |

### T5 Touch Switch Series (86mm Wall Switches)

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **209** | T5-1C-86 | 🔄 Mixed | Single touch switch | 86mm single-gang |
| **210** | T5-2C-86 | 🔄 Mixed | Dual touch switch | 86mm two-gang |
| **211** | T5-3C-86 | 🔄 Mixed | Triple touch switch | 86mm three-gang |
| **212** | T5-4C-86 | 🔄 Mixed | Quad touch switch | 86mm four-gang |

### SwitchMan Series

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **160** | M5-1C | 🔄 Mixed | Single channel switch | SwitchMan single-channel |
| **161** | M5-2C | 🔄 Mixed | Dual channel switch | SwitchMan dual-channel |
| **162** | M5-3C | 🔄 Mixed | Triple channel switch | SwitchMan triple-channel |

### Power Monitoring Devices

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **5** | POW | 🔄 Mixed | Power monitoring switch | Current, voltage, power measurement |
| **32** | POWR2, POWR316, POWR316D, POWR320D | 🔄 Mixed | Advanced power monitoring | Enhanced power measurement |
| **126** | DUAL R3 | 🔄 Mixed | Dual relay + power monitoring | Two relays with power measurement |

### Temperature and Humidity Sensors

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **15** | TH10, TH16, TH16R2 | 🔄 Mixed | Temperature/humidity monitoring | External sensor support |
| **181** | THR320D, THR316D | 🔄 Mixed | Temperature/humidity sensor | Integrated sensor |

### Door/Window Sensors

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **102** | OPL-DMA, DW2 | 🔄 Mixed | Magnetic door/window sensor | Contact sensor |

### Lighting Controllers

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **59** | LED Controller | 🔄 Mixed | LED strip controller | RGB/RGBW LED control |
| **104** | B05 Bulb | 🔄 Mixed | Smart bulb | Dimmable white light |

### Compact/Mini Devices

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **77** | WiFi MICRO (USB) | 🔄 Mixed | Compact WiFi switch | USB-powered mini switch |
| **138** | MINI-D, MINI-R4, MINI-R4M | 🔄 Mixed | Compact dual relay | Mini dry contact switch |

### Specialized Devices

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **78** | Unknown | 🔄 Mixed | Unknown functionality | TODO: Identify device type |
| **140** | CK-BL602-4SW-HS (Bouffalo Lab BL602) | 🔄 Mixed | 3-way wall switch | Generic 3-way switch |
| **190** | S60TPF4, S60TPF, S60TPG | 🔄 Mixed | Smart plug | S60 series smart plugs |
| **237** | SG200 | 🔄 Mixed | Smart gateway | Gateway device |

### GSM/Cellular Devices

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **24** | GSM Socket | 🌐 Cloud | Single channel GSM socket | Cellular connectivity |
| **27** | GSM Socket | 🌐 Cloud | Single channel GSM socket | Cellular connectivity |
| **29** | GSM Socket | 🌐 Cloud | Dual channel GSM socket | Cellular connectivity |
| **30** | GSM Socket | 🌐 Cloud | Triple channel GSM socket | Cellular connectivity |
| **31** | GSM Socket | 🌐 Cloud | Quad channel GSM socket | Cellular connectivity |
| **81** | GSM Socket | 🌐 Cloud | Single channel GSM socket | Cellular connectivity |
| **82** | GSM Socket | 🌐 Cloud | Dual channel GSM socket | Cellular connectivity |
| **83** | GSM Socket | 🌐 Cloud | Triple channel GSM socket | Cellular connectivity |
| **84** | GSM Socket | 🌐 Cloud | Quad channel GSM socket | Cellular connectivity |
| **107** | GSM Socket | 🌐 Cloud | Single channel GSM socket | Cellular connectivity |

---

## Bridge Devices

### Zigbee Bridges

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **66** | ZB Bridge | 🌐 Cloud | Zigbee bridge | Original Zigbee bridge |
| **168** | ZBBridge-P | 🌐 Cloud | Zigbee bridge Pro | Enhanced Zigbee bridge |
| **243** | ZBridge-U | 🌐 Cloud | Zigbee bridge USB | USB-powered Zigbee bridge |

### 433MHz RF Bridge

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **28** | RF-BRIDGE (RF3) | 🔄 Mixed | 433MHz RF bridge | Supports RF sensors and remotes |

---

## Camera Devices

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **256** | SlimCAM2 | 🌐 Cloud | Security camera | IP camera with cloud recording |
| **260** | CAM-B1P | 🌐 Cloud | Security camera | Battery-powered camera |

---

## Zigbee Child Devices

These devices connect through Zigbee bridges (UUID 66, 168, 243):

### Sensors

| UUID | Models | Features | Notes |
|------|--------|----------|-------|
| **1770** | Temperature Sensor | Temperature monitoring | Zigbee temperature sensor |
| **2026** | Motion Sensor | Motion detection | PIR motion sensor |
| **7003** | SNZB-04P | Door/window sensor | Contact sensor (1st version) |
| **7014** | SNZB-02P, SNZB-02D | Temperature/humidity sensor | Temp/humidity sensor |
| **ZCONTACT** | Generic Contact Sensor | Contact detection | Generic door/window sensor |
| **ZWATER** | Water Leak Sensor | Water leak detection | Water/flood sensor |

### Switches and Lights

| UUID | Models | Features | Notes |
|------|--------|----------|-------|
| **ZSWITCH1** | Single Channel Switch | Single relay | Zigbee single-channel switch |
| **ZSWITCH2** | Dual Channel Switch | Dual relay | Zigbee dual-channel switch |
| **ZSWITCH3** | Triple Channel Switch | Triple relay | Zigbee triple-channel switch |
| **ZSWITCH4** | Quad Channel Switch | Quad relay | Zigbee quad-channel switch |
| **ZLIGHT** | Dimmable Light | Dimmable white light | Zigbee dimmable bulb |

---

## 433MHz RF Child Devices

These devices connect through RF bridges (UUID 28):

### Remote Controls

| UUID | Models | Features | Notes |
|------|--------|----------|-------|
| **RF1** | Single Button Remote | 1 button control | 433MHz single-button remote |
| **RF2** | Dual Button Remote | 2 button control | 433MHz dual-button remote |
| **RF3** | Triple Button Remote | 3 button control | 433MHz triple-button remote |
| **RF4** | Quad Button Remote | 4 button control | 433MHz quad-button remote |

### Sensors

| UUID | Models | Features | Notes |
|------|--------|----------|-------|
| **RF6** | RF Sensor | Motion/contact detection | 433MHz PIR or door/window sensor |

---

## Channel Types by Device Category

### Switch Channels
- `switch` - Main relay control (ON/OFF)
- `switch1`, `switch2`, etc. - Multiple relay channels

### Power Monitoring Channels
- `current` - Current measurement (A)
- `voltage` - Voltage measurement (V)
- `power` - Power consumption (W)
- `todayKwh` - Today's energy consumption (kWh)
- `yesterdayKwh` - Yesterday's energy consumption (kWh)

### Sensor Channels
- `temperature` - Temperature reading (°C)
- `humidity` - Humidity reading (%)
- `motion` - Motion detection (ON/OFF)
- `contact` - Contact state (OPEN/CLOSED)
- `battery` - Battery level (%)

### Status Channels
- `cloudOnline` - Cloud connection status
- `localOnline` - Local LAN connection status
- `rssi` - WiFi signal strength (dBm)
- `trigTime` - Last trigger timestamp

### RF Device Channels
- `button0`, `button1`, etc. - Remote button states
- `rf0External` - External RF trigger timestamp
- `rf0Internal` - Internal RF trigger timestamp

---

## Configuration Notes

### Local vs Cloud Operation

**Local Mode Benefits:**
- Faster response times
- No internet dependency
- Enhanced privacy
- Firewall-friendly operation

**Local Mode Limitations:**
- Not all devices support local communication
- Some features may be limited
- Requires devices to be on same network

**Cloud Mode:**
- Full feature support for all devices
- Remote access capability
- Requires internet connection
- Data passes through eWeLink servers

### Power Monitoring Configuration

For POW/POWR2 devices:
- **Local Polling**: Enable for real-time power data
- **Consumption Polling**: Enable for energy statistics (recommend 24h intervals)
- **Polling Intervals**: Balance between data freshness and system load

### Bridge Device Setup

1. **Add bridge device first** (RF Bridge, Zigbee Bridge)
2. **Run discovery** to detect child devices
3. **Configure child devices** as needed
4. **Re-run discovery** after adding new sensors/remotes

---

## Development References

### Code References
- **Constants File**: [`SonoffBindingConstants.java`](../src/main/java/org/openhab/binding/sonoff/internal/SonoffBindingConstants.java) - Contains all UUID mappings and thing type definitions
- **Device Handlers**: Located in `src/main/java/org/openhab/binding/sonoff/internal/handler/`
- **Thing Types**: Defined in `src/main/resources/OH-INF/thing/thing-types.xml`

### UUID to ThingType Mapping
The binding uses the following mapping structure:

```java
// WiFi Devices (THING_TYPE_X where X is the UUID)
THING_TYPE_1 → UUID 1 (S20, S26, BasicR1, BasicR2, Mini)
THING_TYPE_2 → UUID 2 (DUALR2)
THING_TYPE_5 → UUID 5 (POW)
// ... and so on

// Zigbee Bridges
THING_TYPE_66 → UUID 66 (ZB Bridge)
THING_TYPE_168 → UUID 168 (ZBBridge-P)
THING_TYPE_243 → UUID 243 (ZBridge-U)

// Zigbee Child Devices (descriptive names)
THING_TYPE_ZSWITCH1 → Single channel Zigbee switch
THING_TYPE_ZCONTACT → Zigbee contact sensor
// ... and so on

// 433MHz RF Devices (descriptive names)
THING_TYPE_RF1 → Single button RF remote
THING_TYPE_RF6 → RF sensor
// ... and so on
```

### Device Discovery Process
1. **Device UUID Detection**: Binding reads device UUID from eWeLink API
2. **ThingType Mapping**: UUID is mapped to appropriate `THING_TYPE_X` constant
3. **Handler Selection**: Correct device handler is instantiated based on thing type
4. **Channel Creation**: Available channels are determined by device capabilities

---

## Contributing Device Information

If you have information about unknown device models or new devices, please:

1. **Check device files** in `userdata/sonoff/deviceid.txt`
2. **Report device details** via GitHub issues
3. **Include UUID, model, and capabilities**
4. **Test functionality** and report results

---

*Last updated: Based on binding version with 64+ supported device types*