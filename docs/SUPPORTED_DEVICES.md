# Supported Devices - Sonoff Binding

This document provides a comprehensive list of all devices supported by the Sonoff binding, organized by their UUID (Universal Unique Identifier) as defined in the eWeLink protocol.

## Overview

Each Sonoff device is identified by a UUID number that determines its capabilities, channels, and supported features. 


### Connection Types Legend
- 🌐 **Cloud**: Cloud-only connection via eWeLink servers
- 🔄 **Mixed**: Both local LAN and cloud connections supported

## Supported Devices

The following devices are currently supported by this Sonoff binding:

| UUID | Models | Connection | Features | Notes |
|------|--------|------------|----------|-------|
| **ACCOUNT** | eWeLink Account Bridge | 🔄 Mixed | Account management | Required bridge for cloud/mixed mode |
| **1** | S20, S26, BasicR1, BasicR2, Mini, Mini PCIe Card | 🔄 Mixed | Single relay switch | Most common single-channel devices |
| **2** | DUALR2 | 🔄 Mixed | Dual relay switch | Two independent relay channels |
| **3** | Unknown Models | 🔄 Mixed | Socket (3 channels) | TODO: Identify specific models |
| **4** | 4CHPROR3 | 🔄 Mixed | Socket (4 channels) | Four-channel relay device |
| **5** | POW | 🔄 Mixed | Power monitoring switch | Current, voltage, power measurement |
| **6** | T11C, TX1C, G1 | 🔄 Mixed | Single touch switch | Wall-mounted touch switches |
| **7** | T12C, TX2C | 🔄 Mixed | Dual touch switch | Two-gang touch switches |
| **8** | T13C, TX3C | 🔄 Mixed | Triple touch switch | Three-gang touch switches |
| **9** | Unknown Models | 🔄 Mixed | Switch (4 channels) | TODO: Identify specific models |
| **14** | BasicR1 (older firmware) | 🔄 Mixed | Single relay switch | Legacy basic switch |
| **15** | TH10, TH16, TH16R2 | 🔄 Mixed | Temperature/humidity monitoring | External sensor support |
| **24** | GSM Socket | 🌐 Cloud | Single channel GSM socket | Cellular connectivity |
| **27** | GSM Socket | 🌐 Cloud | Single channel GSM socket | Cellular connectivity |
| **28** | RF-BRIDGE (RF3) | 🔄 Mixed | 433MHz RF bridge | Supports RF sensors and remotes |
| **29** | GSM Socket | 🌐 Cloud | Dual channel GSM socket | Cellular connectivity |
| **30** | GSM Socket | 🌐 Cloud | Triple channel GSM socket | Cellular connectivity |
| **31** | GSM Socket | 🌐 Cloud | Quad channel GSM socket | Cellular connectivity |
| **32** | POWR2, POWR316, POWR316D, POWR320D | 🔄 Mixed | Advanced power monitoring | Enhanced power measurement |
| **59** | LED Controller | 🔄 Mixed | LED strip controller | RGB/RGBW LED control |
| **66** | ZB Bridge | 🌐 Cloud | Zigbee bridge | Original Zigbee bridge |
| **77** | WiFi MICRO (USB) | 🔄 Mixed | Compact WiFi switch | USB-powered mini switch |
| **78** | Unknown | 🔄 Mixed | Unknown functionality | TODO: Identify device type |
| **81** | GSM Socket | 🌐 Cloud | Single channel GSM socket | Cellular connectivity |
| **82** | GSM Socket | 🌐 Cloud | Dual channel GSM socket | Cellular connectivity |
| **83** | GSM Socket | 🌐 Cloud | Triple channel GSM socket | Cellular connectivity |
| **84** | GSM Socket | 🌐 Cloud | Quad channel GSM socket | Cellular connectivity |
| **102** | OPL-DMA, DW2 | 🔄 Mixed | Magnetic door/window sensor | Contact sensor |
| **104** | B05 Bulb | 🔄 Mixed | Smart bulb | Dimmable white light |
| **107** | GSM Socket | 🌐 Cloud | Single channel GSM socket | Cellular connectivity |
| **126** | DUAL R3 | 🔄 Mixed | Dual relay + power monitoring | Two relays with power measurement |
| **138** | MINI-D, MINI-R4, MINI-R4M | 🔄 Mixed | Compact dual relay | Mini dry contact switch |
| **140** | CK-BL602-4SW-HS (Bouffalo Lab BL602) | 🔄 Mixed | 3-way wall switch | Generic 3-way switch |
| **160** | M5-1C | 🔄 Mixed | Single channel switch | SwitchMan single-channel |
| **161** | M5-2C | 🔄 Mixed | Dual channel switch | SwitchMan dual-channel |
| **162** | M5-3C | 🔄 Mixed | Triple channel switch | SwitchMan triple-channel |
| **168** | ZBBridge-P | 🌐 Cloud | Zigbee bridge Pro | Enhanced Zigbee bridge |
| **181** | THR320D, THR316D | 🔄 Mixed | Temperature/humidity sensor | Integrated sensor |
| **190** | S60TPF4, S60TPF, S60TPG | 🔄 Mixed | Smart plug | S60 series smart plugs |
| **209** | T5-1C-86 | 🔄 Mixed | Single touch switch | 86mm single-gang |
| **210** | T5-2C-86 | 🔄 Mixed | Dual touch switch | 86mm two-gang |
| **211** | T5-3C-86 | 🔄 Mixed | Triple touch switch | 86mm three-gang |
| **212** | T5-4C-86 | 🔄 Mixed | Quad touch switch | 86mm four-gang |
| **237** | SG200 | 🔄 Mixed | Smart gateway | Gateway device |
| **243** | ZBridge-U | 🌐 Cloud | Zigbee bridge USB | USB-powered Zigbee bridge |
| **256** | SlimCAM2 | 🌐 Cloud | Security camera | IP camera with cloud recording |
| **260** | CAM-B1P | 🌐 Cloud | Security camera | Battery-powered camera |
| **1770** | Temperature Sensor | 🌐 Cloud | Temperature monitoring | Zigbee temperature sensor |
| **2026** | Motion Sensor | 🌐 Cloud | Motion detection | Zigbee PIR motion sensor |
| **7003** | SNZB-04P | 🌐 Cloud | Door/window sensor | Zigbee contact sensor (1st version) |
| **7014** | SNZB-02P, SNZB-02D | 🌐 Cloud | Temperature/humidity sensor | Zigbee temp/humidity sensor |
| **ZCONTACT** | Generic Contact Sensor | 🌐 Cloud | Contact detection | Zigbee door/window sensor |
| **ZWATER** | Water Leak Sensor | 🌐 Cloud | Water leak detection | Zigbee water/flood sensor |
| **ZSWITCH1** | Single Channel Switch | 🌐 Cloud | Single relay | Zigbee single-channel switch |
| **ZSWITCH2** | Dual Channel Switch | 🌐 Cloud | Dual relay | Zigbee dual-channel switch |
| **ZSWITCH3** | Triple Channel Switch | 🌐 Cloud | Triple relay | Zigbee triple-channel switch |
| **ZSWITCH4** | Quad Channel Switch | 🌐 Cloud | Quad relay | Zigbee quad-channel switch |
| **ZLIGHT** | Dimmable Light | 🌐 Cloud | Dimmable white light | Zigbee dimmable bulb |
| **RF1** | Single Button Remote | 🔄 Mixed | 1 button control | 433MHz single-button remote |
| **RF2** | Dual Button Remote | 🔄 Mixed | 2 button control | 433MHz dual-button remote |
| **RF3** | Triple Button Remote | 🔄 Mixed | 3 button control | 433MHz triple-button remote |
| **RF4** | Quad Button Remote | 🔄 Mixed | 4 button control | 433MHz quad-button remote |
| **RF6** | RF Sensor | 🔄 Mixed | Motion/contact detection | 433MHz PIR or door/window sensor |
