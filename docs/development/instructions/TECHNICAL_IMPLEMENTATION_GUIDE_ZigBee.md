# Technical Implementation Guide for Adding Zigbee Devices
## For AI Assistant Use Only - Zigbee Devices Only

This guide provides complete technical details for implementing a new Zigbee device based on user's minimal input.

**Note:** This guide is specifically for Zigbee devices that connect through Zigbee bridges (UUID 66, 168, 243). For WiFi devices, a separate implementation approach is required.

---

## ⚠️ CRITICAL WARNINGS

### 🚨 UUID Consistency is CRITICAL
- UUID must be EXACTLY the same across all files
- One typo = device won't work at all
- Copy-paste UUIDs, don't type them manually

### 🚨 Handler Selection Matters
- Wrong handler = device appears to work but functions are broken
- Check existing similar devices for correct handler type
- When in doubt, start with basic handler and upgrade

### 🚨 Collection Updates are MANDATORY
- Missing from `SUPPORTED_THING_TYPE_UIDS` = validation fails
- Missing from `createZigbeeMap()` = RSSI handling fails
- Missing from factory = thing creation fails

### 🚨 XML Validation is ESSENTIAL
- Invalid XML = binding won't load
- Missing channel types = runtime errors
- Always validate XML syntax before testing

---

## Code Standards

- **@author tag**: If creating a new Java file, set `@author` to `tschaban/SmartnyDom`
- After the code is created for a device and it doesn't work on development environemnt, update technical implementaion with subsequent fixes you will execute with a support of a human

---

## Input Processing

### From User Request Template
Extract:
- `uuid` from JSON file path (e.g., `uuid-7040.json` → `7040`)
- `productUrl` from "Product URL" field (for README.md and XML properties)
- `technicalUrl` from "Technical description URL" field (optional, for AI to read product specs if needed)
- `channels` from "Channels needed" list
- `handlerType` from "Handler Type" field
- `protocol` from "Protocol support"
- `features` from "Special features"

### From Device JSON File
Read `docs/development/jsons/uuid-{UUID}.json` and extract:
- `extra.uiid` → Device UUID (verify matches filename)
- `name` → Device model name
- `productModel` → Model ID
- `extra.ui` → Product description
- `params` → Available device parameters and channels
- `params.switches[]` → Number of switch channels
- `params.sledOnline` → Has status LED
- `params.subDevRssi` → Has RSSI monitoring
- `params.temperature` → Has temperature sensor
- `params.humidity` → Has humidity sensor
- `params.battery` → Battery powered device
- `online` → Cloud connection status

### Derive Product Information
- **Product URL**: Use full URL provided by user in request (e.g., `https://s.smartnydom.pl/r/sonoff-zb2gs?ref=openhab`)
- **Product name**: Use `extra.ui` or construct from model
- **Handler class name**: Based on device type (see Handler Selection below)

---

## Implementation Steps

### Step 1: Add Thing Type Constant

**File:** `src/main/java/org/openhab/binding/sonoff/internal/SonoffBindingConstants.java`

**Location:** In the "Zigbee devices" section, add in numerical order by UUID

**Search for:** `// Zigbee devices` comment block

**Code:**
```java
public static final ThingTypeUID THING_TYPE_{UUID} = new ThingTypeUID(BINDING_ID, "{UUID}");
```

---

### Step 2: Add to SUPPORTED_THING_TYPE_UIDS

**File:** `src/main/java/org/openhab/binding/sonoff/internal/SonoffBindingConstants.java`

**Location:** Find the `SUPPORTED_THING_TYPE_UIDS` collection initialization

**Search for:** `public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS`

**Action:** Add `THING_TYPE_{UUID}` in the Zigbee devices section, maintaining numerical order

**Critical:** This collection controls thing creation via `supportsThingType()`. Missing entry causes "No binding found" error.

---

### Step 3: Add to DISCOVERABLE_THING_TYPE_UIDS

**File:** `src/main/java/org/openhab/binding/sonoff/internal/SonoffBindingConstants.java`

**Location:** Find the `DISCOVERABLE_THING_TYPE_UIDS` collection initialization

**Search for:** `public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPE_UIDS`

**Action:** Add `THING_TYPE_{UUID}` in the Zigbee devices section, maintaining numerical order

**Critical:** This collection controls inbox discovery. Missing entry prevents device from appearing in inbox.

---

### Step 4: Add to Zigbee Device Map

**File:** `src/main/java/org/openhab/binding/sonoff/internal/SonoffBindingConstants.java`

**Location:** Inside the `createZigbeeMap()` method

**Search for:** `private static Map<Integer, ThingTypeUID> createZigbeeMap()`

**Code:**
```java
zigbeeTypes.put({UUID}, THING_TYPE_{UUID});
```

**Action:** Add in numerical order by UUID

---

### Step 5: Create Handler Class

**File:** `src/main/java/org/openhab/binding/sonoff/internal/handler/{HandlerClassName}.java`

#### Handler Selection Logic:

| Device Type | Channels | Handler Name | Base Class |
|-------------|----------|--------------|------------|
| Single/Multi Switch | switch0, switch1, ... | `SonoffZigbeeSwitchMultiHandler` | `SonoffBaseZigbeeHandler` |
| Motion Sensor | motion, battery, trigTime | `SonoffZigbeeMotionSensorHandler` | `SonoffBaseZigbeeHandler` |
| Contact Sensor | contact, battery, trigTime | `SonoffZigbeeContactSensorHandler` | `SonoffBaseZigbeeHandler` |
| Temp/Humidity | temperature, humidity, battery | `SonoffZigbeeTempHumiditySensorHandler` | `SonoffBaseZigbeeHandler` |
| Wireless Button | button0, button1, button2 | `SonoffZigbeeButtonHandler` | `SonoffBaseZigbeeHandler` |


#### Channel Update Mapping:

Map JSON `params` fields to handler update calls:

| JSON Field | Handler Method | Channel |
|------------|----------------|---------|
| `params.switches[0].switch` | `newDevice.getSwitch0()` | `CHANNEL_SWITCH0` |
| `params.switches[1].switch` | `newDevice.getSwitch1()` | `CHANNEL_SWITCH1` |
| `params.sledOnline` | `newDevice.getSledOnline()` | `CHANNEL_SLED` |
| `params.subDevRssi` | `newDevice.getRssi()` | `CHANNEL_RSSI` |
| `online` | `newDevice.isCloudOnline()` | `CHANNEL_CLOUD_ONLINE` |
| `params.temperature` | `newDevice.getTemperature()` | `CHANNEL_TEMPERATURE` |
| `params.humidity` | `newDevice.getHumidity()` | `CHANNEL_HUMIDITY` |
| `params.battery` | `newDevice.getBattery()` | `CHANNEL_BATTERY` |

---

### Step 6: Register Handler in Factory

**File:** `src/main/java/org/openhab/binding/sonoff/internal/SonoffHandlerFactory.java`

**Location:** In `createHandler()` switch statement

**Code:**
```java
case "{UUID}":
    return new {HandlerClassName}(thing);
```

---

### Step 7: Create XML Thing-Type Definition

**File:** `src/main/resources/OH-INF/thing/zigbee-things.xml`

**Template example:**
```xml
<thing-type id="{UUID}">
    <supported-bridge-type-refs>
        <bridge-type-ref id="66"/>
        <bridge-type-ref id="168"/>
        <bridge-type-ref id="243"/>
    </supported-bridge-type-refs>

    <label>SONOFF Zigbee {ProductName}</label>
    <description>Model: {ModelID}</description>

    <channels>
        {Generate based on user's "Channels needed"}
        
        <!-- Common patterns: -->
        
        <!-- For switches: -->
        <channel id="switch0" typeId="power">
            <label>Switch 1</label>
            <description>First switch channel</description>
        </channel>
        
        <!-- For sensors: -->
        <channel id="motion" typeId="motion-contact"/>
        <channel id="contact" typeId="contact"/>
        <channel id="temperature" typeId="temperature"/>
        <channel id="humidity" typeId="humidity"/>
        
        <!-- For buttons: -->
        <channel id="button0" typeId="button-press">
            <label>Single Press</label>
        </channel>
        
        <!-- Always include for Zigbee: -->
        <channel id="cloudOnline" typeId="cloudOnline"/>
        <channel id="rssi" typeId="rssi"/>
        
        <!-- Optional common channels: -->
        <channel id="sled" typeId="sled"/>
        <channel id="battery" typeId="battery-level"/>
        <channel id="trigTime" typeId="trigTime"/>
    </channels>

    <properties>
        <property name="vendor">SONOFF</property>
        <property name="modelId">{ModelID}</property>
        <property name="url">{productUrl}</property>
    </properties>

    <representation-property>deviceid</representation-property>

    <config-description>
        <parameter name="deviceid" type="text" required="true">
            <label>Device ID</label>
            <description>Device ID</description>
        </parameter>
        
        <!-- For button devices only: -->
        <parameter name="buttonResetTimeout" type="integer" min="100" max="60000" unit="ms">
            <label>How long (ms) keep button state pressed?</label>
            <description>Time in milliseconds before button state resets to CLOSED: default=500</description>
            <default>500</default>
        </parameter>
    </config-description>
</thing-type>
```

#### Channel Type Reference:

| Channel ID | typeId | Use For |
|------------|--------|---------|
| switch0, switch1 | power | ON/OFF relay control |
| motion | motion-contact | Motion detection |
| contact | contact | Door/window sensor |
| temperature | temperature | Temperature measurement |
| humidity | humidity | Humidity measurement |
| battery | battery-level | Battery percentage |
| button0, button1 | button-press | Button events |
| cloudOnline | cloudOnline | Cloud connection |
| rssi | rssi | Signal strength |
| sled | sled | Status LED |
| trigTime | trigTime | Last trigger time |

---

### Step 8: Update Unit Tests

**File:** `src/test/java/org/openhab/binding/sonoff/internal/SonoffBindingConstantsTest.java`

**Add test method:**
```java
@Test
public void testZigbeeDevice{UUID}() {
    assertNotNull(THING_TYPE_{UUID});
    assertTrue(SUPPORTED_THING_TYPE_UIDS.contains(THING_TYPE_{UUID}),
        "THING_TYPE_{UUID} must be in SUPPORTED_THING_TYPE_UIDS");
    assertTrue(DISCOVERABLE_THING_TYPE_UIDS.contains(THING_TYPE_{UUID}),
        "THING_TYPE_{UUID} must be in DISCOVERABLE_THING_TYPE_UIDS");
    assertEquals(THING_TYPE_{UUID}, createZigbeeMap().get({UUID}),
        "Zigbee map must contain entry for UUID {UUID}");
}
```

---

### Step 9: Update Integration Tests

**File:** `src/test/java/org/openhab/binding/sonoff/internal/SonoffHandlerFactoryTest.java`

**Add test method:**
```java
@Test
public void testZigbee{UUID}Handler() {
    Thing thing = ThingBuilder.create(THING_TYPE_{UUID}, "test{UUID}")
        .withBridge(bridgeUID)
        .build();
    
    ThingHandler handler = factory.createHandler(thing);
    
    assertNotNull(handler, "Handler should be created for THING_TYPE_{UUID}");
    assertTrue(handler instanceof {HandlerClassName},
        "Handler should be instance of {HandlerClassName}");
    assertTrue(factory.supportsThingType(THING_TYPE_{UUID}),
        "Factory should support THING_TYPE_{UUID}");
}
```

---

### Step 10: Update Documentation

**File:** `README.md`

**Add to device table (maintain numerical order by UUID):**

```markdown
| **{UUID}** | Sonoff [{ModelID}]({productUrl}) | 🌐 Cloud | {Description} | {TestingStatus} |
```

**Testing Status Values:**
- `⚠️ Testing needed` - Default for new devices
- (empty) - Fully tested and working

---

## 🚨 MANDATORY Validation & Testing

### Pre-Build Validation

**Before building, verify:**

1. ✅ UUID consistency across all files
2. ✅ THING_TYPE_{UUID} added to SUPPORTED_THING_TYPE_UIDS
3. ✅ THING_TYPE_{UUID} added to DISCOVERABLE_THING_TYPE_UIDS
4. ✅ UUID added to createZigbeeMap()
5. ✅ Handler case added to SonoffHandlerFactory
6. ✅ XML syntax is valid
7. ✅ All required channels defined
8. ✅ Test methods updated

### Automated Validation

**Run validation test:**
```java
// Test validation passes
ValidationResult result = SonoffBindingConstants.validateDeviceMappings();
assertFalse(result.hasErrors());
```

🚨 CRITICAL: NEVER skip validation - catches 90% of common errors

⚠️ WARNING: Validation errors = device won't work in production

### Post-Build Testing

**After deployment, verify:**

1. ✅ Device appears in OpenHAB inbox
2. ✅ Device creates without "No binding found" error
3. ✅ Thing status shows ONLINE
4. ✅ All channels functional
5. ✅ No errors in logs: `O:\configuration\logs\openhab.log`
6. ✅ Unit tests pass
7. ✅ Integration tests pass

🚨 CRITICAL: Test with REAL device when possible

⚠️ WARNING: Simulator testing only catches basic errors

---

## Build and Deployment

### Commands:
```powershell
# Development build (fast, skips tests)
.\build.ps1 fast

# Full build (required before commit)
.\build.ps1 deploy

```

### Verification:
1. Device appears in OpenHAB inbox
2. Device creates without "No binding found" error
3. Thing status shows ONLINE
4. All channels functional
5. No errors in logs: `O:\configuration\logs\openhab.log`

---

## Common Pitfalls

❌ **Forgot to add to createZigbeeMap()** → RSSI shows wrong values (uses rssi instead of subDevRssi)
❌ **Wrong handler type** → Device doesn't work
❌ **Missing from SUPPORTED_THING_TYPE_UIDS** → Validation fails, "No binding found" error
❌ **Typo in UUID** → Device not recognized at all
❌ **Missing XML definition** → Thing creation fails
❌ **Wrong XML file** → Thing not found
❌ **Forgot to update test classes** → Tests fail in CI/CD pipeline
❌ **Added to wrong test category** → Test expects wrong handler type
❌ **Invalid XML syntax** → Binding won't load
❌ **Missing channel types** → Runtime errors
❌ **Wrong bridge type refs** → Device won't connect through bridge
❌ **Forgot representation-property** → Device properties not properly identified

---

## Common Patterns

### Multi-Channel Switches (2-4 channels)
- Handler: `SonoffZigbeeSwitchMultiHandler`
- Channels: switch0, switch1, switch2, switch3, cloudOnline, sled, rssi
- JSON: `params.switches[]` array

### Motion Sensors
- Handler: `SonoffZigbeeMotionSensorHandler`
- Channels: motion, battery, trigTime, cloudOnline, rssi
- JSON: `params.motion`, `params.battery`

### Contact Sensors
- Handler: `SonoffZigbeeContactSensorHandler`
- Channels: contact, battery, trigTime, cloudOnline, rssi
- JSON: `params.contact`, `params.battery`

### Temperature/Humidity Sensors
- Handler: `SonoffZigbeeTempHumiditySensorHandler`
- Channels: temperature, humidity, battery, trigTime, cloudOnline
- JSON: `params.temperature`, `params.humidity`, `params.battery`

### Wireless Buttons
- Handler: `SonoffZigbeeButtonHandler`
- Channels: button0 (single), button1 (double), button2 (long), trigTime, cloudOnline, rssi
- JSON: `params.key` with values 0, 1, 2
- Config: `buttonResetTimeout` parameter

---

## Error Resolution

### "No binding found" error
**Cause:** Missing from `SUPPORTED_THING_TYPE_UIDS`  
**Fix:** Search for `SUPPORTED_THING_TYPE_UIDS` collection and add THING_TYPE_{UUID}

### Device not discovered
**Cause:** Missing from `DISCOVERABLE_THING_TYPE_UIDS`  
**Fix:** Search for `DISCOVERABLE_THING_TYPE_UIDS` collection and add THING_TYPE_{UUID}

### RSSI values incorrect (showing -1 or null)
**Cause:** Zigbee device not added to `createZigbeeMap()`
**Fix:** Search for `createZigbeeMap()` method and add UUID mapping

### XML validation errors
**Cause:** Invalid XML syntax or missing channel type definitions
**Fix:** Validate XML against schema, check channel typeId references exist in channels.xml

### Handler not created
**Cause:** Missing case in `SonoffHandlerFactory`  
**Fix:** Add switch case mapping UUID to handler

### Channels not updating
**Cause:** Missing channel update logic in handler  
**Fix:** Implement `updateChannels()` for all channels from JSON

---

## Files Modified Checklist

1. `SonoffBindingConstants.java` - 4 locations:
   - Thing type constant declaration
   - SUPPORTED_THING_TYPE_UIDS collection
   - DISCOVERABLE_THING_TYPE_UIDS collection
   - createZigbeeMap() method
2. `{HandlerClassName}.java` - New handler class
3. `SonoffHandlerFactory.java` - Handler registration
4. `zigbee-things.xml` - Thing-type definition
5. `SonoffBindingConstantsTest.java` - Unit test
6. `SonoffHandlerFactoryTest.java` - Integration test
7. `README.md` - Documentation

---

## Reference Implementation

**UUID 7040 (MINI-ZB2GS)** - Complete working example:
- Config: `docs/development/jsons/uuid-7040.json`
- Handler: `SonoffZigbeeSwitchMultiHandler.java`
- XML: Lines 215-250 in `zigbee-things.xml`
- Commits: Search git history for "7040" or "MINI-ZB2GS"

---

## Quick Verification Checklist

✅ **UUID consistency**: Same UUID across all files (constants, factory, XML, tests)
✅ **Collections updated**: SUPPORTED_THING_TYPE_UIDS, DISCOVERABLE_THING_TYPE_UIDS, createZigbeeMap()
✅ **Handler mapped**: Case added to SonoffHandlerFactory.createHandler()
✅ **XML valid**: Syntax correct, channels defined, bridge refs included
✅ **Tests updated**: Unit tests and integration tests include new device
✅ **Validation passes**: SonoffBindingConstants.validateDeviceMappings() succeeds
✅ **Discovery works**: Device appears in OpenHAB inbox
✅ **Thing creates**: No "No binding found" error
✅ **Channels work**: Device responds to commands, state updates correctly
✅ **Tests pass**: All unit and integration tests succeed
✅ **Documentation updated**: README.md includes device entry
✅ **No errors in logs**: Clean startup and operation

---

## Estimated Time

🎯 **Simple device** (using existing handler): 15-20 minutes
🎯 **Complex device** (new handler needed): 30-45 minutes
🔧 **Files modified**: Typically 5-7 files (including tests)
✅ **Validation**: Automated validation catches most issues immediately
🧪 **Testing**: Unit and integration tests ensure correctness

---

**This guide contains all technical details needed to implement ANY Zigbee device based on minimal user input.**
