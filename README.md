> [!NOTE]
> Info about that particular branch
> * It's maintained by SmartnyDom based on the code by [delid4ve](https://github.com/delid4ve/openhab-3.x-sonoff) and all colleagues down the road (. ❛ ᴗ ❛.)👍
> * All enhancements, changes and fixes to the original code you can find logged [here](https://github.com/tschaban/openhab-addons-sonoff/milestones?state=closed)
> * Feel free to report issues for my best effort fixes [here](https://github.com/tschaban/openhab-addons-sonoff/issues)
> * Newest release [here](https://github.com/tschaban/openhab-addons-sonoff/releases)
> 
> **Added: SONOFF Supported Devices**
> * SONOFF WiFi iPlug Smart Plug | S60 Series: Models: S60TPF, S60TPG
> * SONOFF WiFi POW Smart Power Meter Switch | Models: POWR316, POWR316D, POWR320D
> * SONOFF WiFi POW Ring Smart Power Meter | Models: POWCT
> * SONOFF WiFi MINI Dry Smart Switch | Model: MINI-D
> * SONOFF WiFi MINI Smart Switch | Models: MINI-R4, MINI-R4M
> * SONOFF WiFi Smart Gate Controller (Prototype device)
> * SONOFF WiFi SwitchMan 1-3 Channel Switch | Model: M5-1C, M5-2C, M5-3C [Not tested, let me know if it works]
> * SONOFF WiFi TX ULTIMATE 1-4 Channel Wall Switch | Model: T5-1C, T5-2C, T5-3C, T5-4C [Partially tested, let me know if it works] 
> * SONOFF Zigbee Bridge | Model: ZB-Bridge-Pro
> * SONOFF Zigbee Bridge | Model: ZB-Bridge-Ultra
> * SONOFF Zigbee Sensor: Temp and Humidity | Models: SNZB-02P, SNZB-02D
> * SONOFF Zigbee Sensor: Door and Window: | Model: SNZB-04P (1st version) [Not tested, let me know if it works]


> **Added: GENERIC Devices**
> * 3 Way Wall Switch | Model: Bouffalo Lab BL602 (CK-BL602-4SW-HS)



# sonoff Binding

Allows Control/Updates of Ewelink based devices using the cloud and/or LAN.

## Supported Things

Currently known to support (non exhaustive):
* Mixed Mode - UUID1: S20, S26, Basic, Mini, Mini PciE Card
* Mixed Mode - UUID2: Unknown Models
* Mixed Mode - UUID3: Unknown Models
* Mixed Mode - UUID4: Unknown Models
* Mixed Mode - UUID5: POW
* Mixed Mode - UUID6: T11C, TX1C, G1
* Mixed Mode - UUID7: T12C, TX2C
* Mixed Mode - UUID8: T13C, TX3C
* Mixed Mode - UUID9: Unknown Models
* Mixed Mode - UUID15: TH10, TH16
* Cloud Only - UUID24: 1 Channel GSM Socket
* Cloud Only - UUID27: 1 Channel GSM Socket
* Mixed Mode - UUID28: RFBRIDGE (Only sensors currently supported, awaiting remote logs)
* Cloud Only - UUID29: 2 Channel GSM Socket
* Cloud Only - UUID30: 3 Channel GSM Socket
* Cloud Only - UUID31: 4 Channel GSM Socket
* Mixed Mode - UUID32: POWR2
* Mixed Mode - UUID77: MICRO USB
* Cloud Only - UUID81: 1 Channel GSM Socket
* Cloud Only - UUID82: 2 Channel GSM Socket
* Cloud Only - UUID83: 3 Channel GSM Socket
* Cloud Only - UUID84: 4 Channel GSM Socket
* Cloud Only - UUID66: Zigbee Bridge
* Cloud Only - UUID107: 1 Channel GSM Socket
* Cloud Only - UUID2026: Zigbee Motion Sensor

## Setup
* Add an 'Account' thing and configure.
* email: your ewelink email address
* password: your ewelink password
* accessmode: your choice of mode for the binding (local,cloud,mixed)

The account should now come online.  Run discovery to create the cache required for all devices, you can manually add as text files once this is complete.
Should any devices not be supported please send @delid4ve the file that is generated for the deviceid you want added.

* Please note there is a known bug within openhab if you are using text files.  If on changing a config parameter your devices do not come online then please remove the file file and re-add.  If this does not resaolve the issue you may have to remove and re-add the binding.

## Discovery

* Once you have initialized the account, run discovery as normal.
* All devices support automatic discovery and this must be run even if using text based files in order to create a cache.
* For Sub devices, i.e sensors connected to an RF Bridge or Zigbee bridge, add the main device and then run discovery again to find any connected devices.

## Local vs Cloud

* Not all devices support local mode such as the zigbee bridge.
* If local mode is supported and once initialized, the device can be blocked by your firewall to prevent external access.
* If you are in mixed mode, locally supported devices can be blocked at your firewall and will use local only mode

POW/POWR2 in local mode: 
* In order to retreive energy data when operating in local only mode there are 2 seperate configuration parameters: (Not required when in LAN Development mode)
* Enable Local Polling: enable local polling of energy data
* Polling Interval for Local Only mode: interval in seconds betwen polls

POW/POWR2 Consumption:

In order to retreive consumption data (cloud only) there are 2 seperate configuration parameters:
* Enable consumption polling: on/off
* Polling interval for consumption data: interval in seconds to retreive the data
* Please bear in mind that polling for data is a burden on your system resources.  Data such as consumption realy only needs to be fetched every 24 hours (86400 seconds).

## Bugs

Please report any bugs on my github:

https://github.com/delid4ve/openhab-sonoff/issues

Please ensure you include the version you are using and any debug log information that is applicable.  Please also include the file that is created for the device under userdata/sonoff/deviceid.txt

## Thing Configuration

* POW / POWR2 Devices support consumption polling
* Devices listed as Local or Mixed Mode support local polling

```
Bridge sonoff:account:uniqueName "Sonoff Account" @ "myLocation" 
[ email="account@example.com", password="myPassword",accessmode="mixed"] {
32      PowR2                               "PowR2"         @   "thingLocation"     [ deviceid="1000bd9fe9",local=false,localPoll=10,consumption=false,consumptionPoll=10] ]
77      USBSwitch                           "USB Switch"    @   "thingLocation"     [ deviceid="1000dc155b",local=false,localPoll=10 ]	

Bridge  sonoff:28:uniqueName:RFBridge       "RFBridge"      @   "thingLocation"     [ deviceid="1000e72cb8" ] { 

    rfsensor	DoorContact       "Door Contact"		    @ "contactLocation"	[ deviceid="0" ]
    rfsensor	WindowContact     "Window Contact"	        @ "contactLocation"	[ deviceid="1" ]
    rfsensor	PIRSensor         "PIR Sensor"		        @ "sensorLocation"	[ deviceid="2" ]
    rfremote2	Remote1           "2 Button Remote"		    @ "wherever"	    [ deviceid="3" ]
    rfremote4	Remote2           "4 Button Remote"		    @ "wherever"	    [ deviceid="5" ]
}

Bridge  sonoff:66:benfleet:ZigbeeBridge     "Zigbee Bridge"	@ "bridgeLocation"	    [ deviceid="1000f60f3d"]	{

	zmotion		MotionSensor	  "Motion Sensor"			@ "sensorLocation"	[ deviceid="a48000a933"]
}

}
```

## Item Configuration

# Main Devices

```

Switch			Switch				        "Switch"				                {channel="sonoff:32:uniqueName:PowR2:switch"}
Number			Current				        "Current"						        {channel="sonoff:32:uniqueName:PowR2:current"}
Number			Voltage				        "Voltage"						        {channel="sonoff:32:uniqueName:PowR2:voltage"}
Number			Power				        "Power"							        {channel="sonoff:32:uniqueName:PowR2:power"}
Number			Today				        "Energy Usage Today"			        {channel="sonoff:32:uniqueName:PowR2:todayKwh"}
Number			Yesterday			        "Energy Usage Yesterday"		        {channel="sonoff:32:uniqueName:PowR2:yesterdayKwh"}
Number			Seven				        "Energy Usage Last Week"		        {channel="sonoff:32:uniqueName:PowR2:sevenKwh"}
Number			Thirty				        "Energy Usage Last Month"		        {channel="sonoff:32:uniqueName:PowR2:thirtyKwh"}
Number			Hundred				        "Energy Usage Last Hundred"		        {channel="sonoff:32:uniqueName:PowR2:hundredKwh"}
String			CloudConnected		        "Cloud Connected"				        {channel="sonoff:32:uniqueName:PowR2:cloudOnline"}
String			LocalConnected		        "LAN Connected"				            {channel="sonoff:32:uniqueName:PowR2:localOnline"}
Number			Rssi				        "Signal Stength"				        {channel="sonoff:32:uniqueName:PowR2:rssi"}

String			RFBridgeCloudConnected		"Cloud Connected"						{channel="sonoff:28:uniqueName:RFBridge:cloudOnline"}
String			RFBridgeLANConnected		"LAN Connected"				    		{channel="sonoff:28:uniqueName:RFBridge:localOnline"}
Number			RFBridgeRssi				"Signal Stength"						{channel="sonoff:28:uniqueName:RFBridge:rssi"}
```

# RF Sensors

```
DateTime		DoorOpened					"Door Opened"					        {channel="sonoff:rfsensor:uniqueName:RFBridge:DoorContact:rf0External"}
DateTime		WindowOpened				"Front Door Opened"				        {channel="sonoff:rfsensor:uniqueName:RFBridge:WindowContact:rf0External"}
DateTime		MotionDetected				"Motion Detected"				        {channel="sonoff:rfsensor:uniqueName:RFBridge:PIRSensor:rf0External"}
```

# RF Remotes

```
Switch  		Remote1Arm					"Arm alarm"					            {channel="sonoff:rfremote2:uniqueName:RFBridge:Remote1:button0"}
Switch  		Remote1Disarm				"Disarm alarm"					        {channel="sonoff:rfremote2:uniqueName:RFBridge:Remote1:button1"}
Switch  		Remote2Arm					"Arm alarm"					            {channel="sonoff:rfremote4:uniqueName:RFBridge:Remote2:button0"}
Switch  		Remote2Disarm				"Disarm alarm"					        {channel="sonoff:rfremote4:uniqueName:RFBridge:Remote2:button1"}
Switch  		Remote2PartArm				"Part Arm alarm"					    {channel="sonoff:rfremote4:uniqueName:RFBridge:Remote2:button2"}
Switch  		Remote2SOS				    "SOS"					                {channel="sonoff:rfremote4:uniqueName:RFBridge:Remote2:button3"}

DateTime		Remote1Button1External		"Arm Alarm Triggered By Remote"			{channel="sonoff:rfremote2:uniqueName:RFBridge:Remote1:rf0External"}
DateTime		Remote1Button2External		"Disarm Alarm Triggered By Remote"      {channel="sonoff:rfremote2:uniqueName:RFBridge:Remote1:rf1External"}

DateTime		Remote1Button1Internal		"Arm Alarm Triggered By Openhab"		{channel="sonoff:rfremote2:uniqueName:RFBridge:Remote1:rf0Internal"}
DateTime		Remote1Button2Internal		"Disarm Alarm Triggered By Openhab"		{channel="sonoff:rfremote2:uniqueName:RFBridge:Remote1:rf1Internal"}
```

# Zigbee Sensor

```
Switch			MotionDetected		        "Motion Detected"						{channel="sonoff:zmotion:uniqueName:ZigbeeBridge:MotionSensor:motion"}
Number			MotionSensorBattery	        "PIR Battery Level"						{channel="sonoff:zmotion:uniqueName:ZigbeeBridge:MotionSensor:battery"}
DateTime		MotionActivated		        "PIR Activated"				 	        {channel="sonoff:zmotion:uniqueName:ZigbeeBridge:MotionSensor:trigTime"}
```

## Credits

## Development Documentation

For developers working on the Sonoff binding, comprehensive technical documentation is available in the `docs/` directory:

### 🔧 Development Framework
- **[Testing Framework](docs/development/testing-framework.md)** - Complete JUnit 5 testing setup with Mockito, automated scripts, and CI/CD integration

### 🧪 Testing Documentation
- **[Cache Provider Tests](docs/testing/cache-provider-tests.md)** - Unit, integration, and error handling tests for SonoffCacheProvider
- **[Discovery Service Tests](docs/testing/discovery-service-tests.md)** - Comprehensive testing for device discovery functionality
- **[Handler Factory Tests](docs/testing/handler-factory-tests.md)** - Unit and integration tests for SonoffHandlerFactory

### 📋 Quick Development Setup
```bash
# Run all tests with detailed progress
.\task-run-unit-tests.ps1

# Full CI/CD pipeline with testing, compilation, and deployment
.\task-run-deploy.ps1
```

### 🎯 Key Features
- **100+ test methods** across 6 comprehensive test classes
- **Quality gates** that prevent deployment of failing code
- **Mock-based testing** for reliable unit tests
- **Integration testing** with real file system operations
- **Automated scripts** with detailed progress tracking and error handling

---

## Acknowledgments

Huge thanks to the following, this would not be possible without you:

https://github.com/skydiver

https://github.com/bwp91

https://github.com/AlexxIT

https://github.com/RealZimboGuy
