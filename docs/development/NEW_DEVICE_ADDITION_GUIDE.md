# New Device Addition Guide - Sonoff Binding

**Quick checklist for adding new devices with UUID to the Sonoff binding**

## ⚠️ CRITICAL WARNINGS

**READ THESE FIRST - Common mistakes that break everything:**

### 🚨 UUID Consistency is CRITICAL
- UUID must be **EXACTLY** the same across all files
- One typo = device won't work at all
- Copy-paste UUIDs, don't type them manually

### 🚨 Handler Selection Matters
- Wrong handler = device appears to work but functions are broken
- Check existing similar devices for correct handler type
- When in doubt, start with basic handler and upgrade

### 🚨 Collection Updates are MANDATORY
- Missing from `SUPPORTED_THING_TYPE_UIDS` = validation fails
- Missing from `createMap()` = discovery fails  
- Missing from factory = thing creation fails

### 🚨 XML Validation is ESSENTIAL
- Invalid XML = binding won't load
- Missing channel types = runtime errors
- Always validate XML syntax before testing

---

## Prerequisites

- **Device UUID**: Obtain the numeric UUID from eWeLink API documentation
- **Device Info**: Model name, capabilities, channels needed
- **Handler Type**: Determine which existing handler fits or if new handler needed

---

## Step-by-Step Checklist

### 1. 🚨 **CRITICAL: Add THING_TYPE Constant**
**File**: `SonoffBindingConstants.java`

```java
// Add in appropriate WiFi devices section
/** Device description: ModelName */
public static final ThingTypeUID THING_TYPE_XXX = new ThingTypeUID(BINDING_ID, "XXX");
```

**🚨 CRITICAL**: Replace `XXX` with actual UUID number. This MUST match the eWeLink UUID exactly.
**⚠️ DANGER**: Wrong UUID = device will never be discovered or work properly.

### 2. 🚨 **CRITICAL: Add to Device Collections**
**File**: `SonoffBindingConstants.java`

**⚠️ SKIP ANY OF THESE = DEVICE WON'T WORK**

#### a) Add to SUPPORTED_THING_TYPE_UIDS
```java
// Add in appropriate category section
THING_TYPE_XXX,
```

#### b) Add to DISCOVERABLE_THING_TYPE_UIDS (if discoverable)
```java
// Add in WiFi devices section
THING_TYPE_XXX,
```

#### c) Add to createMap()
```java
deviceTypes.put(XXX, THING_TYPE_XXX);
```

#### d) Add to createZigbeeMap() (Zigbee devices only)
```java
// Add in createZigbeeMap() method
zigbeeTypes.put(XXX, THING_TYPE_XXX); // e.g., zigbeeTypes.put(7000, THING_TYPE_7000);
```
**✅ AUTOMATIC**: This also enables automatic subDevRssi handling for RSSI values

#### e) 🚨 **CRITICAL: Add to createZigbeeBridgeMap() (ZigBee Bridge devices only)**
**File**: `SonoffBindingConstants.java`

**⚠️ ONLY for ZigBee Bridge devices (not regular ZigBee devices)**

If adding a **NEW ZIGBEE BRIDGE** (like a new version of ZBBridge), you MUST update the `createZigbeeBridgeMap()` method:

```java
/**
 * Creates mapping for ZigBee Bridge device types.
 * Used to identify devices that support ZigBee sub-devices.
 * When adding new ZigBee bridges, add their UUID to this map.
 * 
 * Current bridges:
 * - 66: THING_TYPE_66 (ZB Bridge original)
 * - 168: THING_TYPE_168 (ZBBridge-P enhanced version)
 * - 243: THING_TYPE_243 (ZBridge-U USB version)
 * - XXX: THING_TYPE_XXX (Your new bridge)  // ADD THIS LINE
 */
public static final Map<Integer, ThingTypeUID> createZigbeeBridgeMap() {
    Map<Integer, ThingTypeUID> zigbeeBridgeTypes = new HashMap<>();
    zigbeeBridgeTypes.put(66, THING_TYPE_66);
    zigbeeBridgeTypes.put(168, THING_TYPE_168);
    zigbeeBridgeTypes.put(243, THING_TYPE_243);
    zigbeeBridgeTypes.put(XXX, THING_TYPE_XXX);  // ADD THIS LINE
    
    return Collections.unmodifiableMap(zigbeeBridgeTypes);
}
```

**🚨 CRITICAL**: Missing this = ZigBee sub-devices won't be discovered through the new bridge  
**⚠️ NOTE**: This is ONLY for bridge devices, NOT for ZigBee end devices (sensors, switches, etc.)  
**✅ BENEFIT**: Automatically enables sub-device discovery and management for the new bridge

**📋 What This Does:**
- Enables automatic ZigBee sub-device detection for the bridge
- Allows the bridge to report connected ZigBee devices
- Used by `SonoffDeviceState.setSubDevices()` to identify bridge devices
- Used by `SonoffDiscoveryService` to discover ZigBee devices connected to the bridge
- Follows the same pattern as `createMap()`, `createZigbeeMap()`, and `createSensorMap()`

#### f) Add to LAN protocol sets (WiFi devices with LAN support)
```java
// Add XXX to LAN_IN and/or LAN_OUT sets
```

### 3. 🚨 **CRITICAL: Add Handler Mapping**
**File**: `SonoffHandlerFactory.java`

```java
// Add case in createHandler() switch statement
case "XXX":
    return new SonoffSuitableHandler(thing); // Choose appropriate handler
```

**🚨 CRITICAL**: Missing this = "No handler found for thing type" error
**⚠️ WARNING**: Wrong handler = device functions won't work properly

**📋 Handler Types Available:**

**WiFi Devices:**
- `SonoffSwitchSingleHandler` - Single channel switches
- `SonoffSwitchMultiHandler` - Multi-channel switches  
- `SonoffSwitchPOWHandler` - Power monitoring (POW)
- `SonoffSwitchPOWR2Handler` - Advanced power monitoring (POWR2)
- `SonoffSwitchTHHandler` - Temperature/humidity sensors
- `SonoffRGBStripHandler` - RGB lighting
- `SonoffRGBCCTHandler` - RGB+CCT lighting

**Zigbee Devices:**
- `SonoffZigbeeContactSensorHandler` - Door/window sensors
- `SonoffZigbeeDeviceMotionSensorHandler` - Motion sensors
- `SonoffZigbeeDeviceTemperatureHumiditySensorHandler` - Temperature/humidity sensors
- `SonoffZigbeeButtonHandler` - Button/switch devices

**Other:**
- `SonoffRfDeviceHandler` - 433MHz RF devices
- Create new handler if none fit

### 4. 🚨 **CRITICAL: Add Thing Type Definition**
**File**: `src/main/resources/OH-INF/thing/[category]-things.xml`

**🚨 CRITICAL**: Invalid XML = binding won't load at all
**⚠️ WARNING**: Missing channels = device appears broken to users

```xml
<thing-type id="XXX">
    <supported-bridge-type-refs>
        <bridge-type-ref id="account"/>
    </supported-bridge-type-refs>
    <label>SONOFF Device Name</label>
    <description>Models: Model1, Model2</description>
    <channels>
        <!-- Add appropriate channels -->
        <channel id="switch" typeId="power"/>
        <channel id="localOnline" typeId="localOnline"/>
        <channel id="cloudOnline" typeId="cloudOnline"/>
        <!-- Add device-specific channels -->
    </channels>
    
    <representation-property>deviceid</representation-property>
    
    <config-description>
        <parameter name="deviceid" type="text" required="true">
            <label>Device ID</label>
            <description>Device ID</description>
        </parameter>
        <!-- Add device-specific config parameters -->
    </config-description>
</thing-type>
```

**📁 File Selection:**
- `switch-things.xml` - WiFi switches and relays
- `sensor-things.xml` - WiFi sensors
- `light-things.xml` - WiFi lighting devices
- `cam-things.xml` - Cameras
- `zigbee-things.xml` - Zigbee devices (sensors, switches, buttons)
- `rf-things.xml` - 433MHz RF devices
- Create new file if needed

**⚠️ Zigbee Devices**: Must include supported bridge type refs:
```xml
<supported-bridge-type-refs>
    <bridge-type-ref id="66"/>   <!-- ZB Bridge -->
    <bridge-type-ref id="168"/>  <!-- ZBBridge-P -->
    <bridge-type-ref id="243"/>  <!-- ZBBridge-U -->
</supported-bridge-type-refs>
```

### 5. **Update Test Classes**
**Files**: 
- `src/test/java/.../SonoffHandlerFactoryTest.java`
- `src/test/java/.../SonoffHandlerFactoryIntegrationTest.java`

**⚠️ IMPORTANT**: Update test classes to include the new device for proper test coverage

#### a) Update SonoffHandlerFactoryTest.java
Add the new device UUID to the appropriate test method:

```java
// For single switch devices
@ParameterizedTest
@ValueSource(strings = { "1", "6", "14", ..., "XXX" })  // Add your UUID
@DisplayName("Should create SonoffSwitchSingleHandler for single switch device types")
void testCreateHandler_SingleSwitchDevices(String deviceId) {
    // Test implementation
}

// OR for multi switch devices
@ParameterizedTest
@ValueSource(strings = { "2", "3", "4", ..., "XXX" })  // Add your UUID
@DisplayName("Should create SonoffSwitchMultiHandler for multi switch device types")
void testCreateHandler_MultiSwitchDevices(String deviceId) {
    // Test implementation
}

// OR create dedicated test for unique device types
@Test
@DisplayName("Should create SonoffNewDeviceHandler for new device type")
void testCreateHandler_NewDevice() {
    ThingTypeUID thingType = new ThingTypeUID("sonoff", "XXX");
    when(mockThing.getThingTypeUID()).thenReturn(thingType);
    
    ThingHandler handler = factory.createHandler(mockThing);
    
    assertNotNull(handler);
    assertEquals("SonoffNewDeviceHandler", handler.getClass().getSimpleName());
}
```

#### b) Update SonoffHandlerFactoryIntegrationTest.java
Add the new device to the integration test:

```java
@Test
@DisplayName("Should create correct handler types for device categories")
void testHandlerCreationByCategory() {
    // Add to appropriate array
    String[] singleSwitchIds = { "1", "6", "14", ..., "XXX" };  // Add your UUID
    for (String id : singleSwitchIds) {
        testDeviceHandlerCreation(id, "SonoffSwitchSingleHandler");
    }
    
    // OR add dedicated test call
    testDeviceHandlerCreation("XXX", "SonoffNewDeviceHandler");
}
```

**📋 Test Categories:**
- Single switch devices → Add to `singleSwitchIds` array
- Multi switch devices → Add to `multiSwitchIds` array
- Sensor devices → Add to `sensorIds` array
- Button devices → Add dedicated test call
- RF devices → Add to `rfIds` array
- Unique handlers → Create dedicated test method

**✅ BENEFIT**: Ensures factory correctly creates handlers for new devices  
**⚠️ NOTE**: Tests will fail if handler mapping is incorrect

### 6. **Update Documentation**
**File**: `docs/SUPPORTED_DEVICES.md`

Add entry to supported devices table:
```markdown
| **XXX** | ModelName | 🔄 Mixed | Device features | Device description |
```

### 7. **✅ AUTOMATIC: subDevRssi Handling (Zigbee Devices)**
**File**: `SonoffDeviceState.java`

**✅ AUTOMATIC**: Zigbee devices automatically use `subDevRssi` instead of `rssi`

**No manual configuration needed!** The code automatically detects Zigbee devices:
```java
// Automatic detection - checks if UUID is in createZigbeeMap()
if (SonoffBindingConstants.createZigbeeMap().containsKey(uiid)) {
    // Uses subDevRssi for Zigbee devices
} else {
    // Uses rssi for WiFi devices
}
```

**✅ BENEFIT**: Adding device to `createZigbeeMap()` automatically enables subDevRssi  
**⚠️ NOTE**: Only applies to Zigbee devices that connect through bridges (66, 168, 243)  
**✅ SKIP**: WiFi devices and Zigbee bridges automatically use regular `rssi` parameter

**📋 How It Works:**
1. Add Zigbee device UUID to `createZigbeeMap()` (Step 2d)
2. RSSI handling is automatically configured
3. No additional code changes needed

### 8. **Create/Update Handler (if needed)**
**File**: `src/main/java/.../handler/SonoffNewDeviceHandler.java`

Only if existing handlers don't fit. Follow existing handler patterns.

---

## 🚨 MANDATORY Validation & Testing

### 9. **🚨 CRITICAL: Run Automated Validation**
```java
// Test validation passes
ValidationResult result = SonoffBindingConstants.validateDeviceMappings();
assertFalse(result.hasErrors());
```

**🚨 CRITICAL**: NEVER skip validation - catches 90% of common errors
**⚠️ WARNING**: Validation errors = device won't work in production

### 10. **🚨 CRITICAL: Test Discovery**
- Device should appear in discovery
- Thing creation should work
- Channels should be functional

**🚨 CRITICAL**: Test with REAL device when possible
**⚠️ WARNING**: Simulator testing only catches basic errors

---

## ⚠️ Critical Warnings

### **UUID Consistency**
- **MUST** use exact eWeLink UUID number
- **MUST** be consistent across all files
- **NEVER** reuse existing UUIDs

### **Handler Selection**
- **CRITICAL**: Choose correct handler type
- Wrong handler = device won't work
- Test with actual device if possible

### **Collection Updates**
- **MUST** add to SUPPORTED_THING_TYPE_UIDS
- **MUST** add to createMap()
- **SHOULD** add to DISCOVERABLE_THING_TYPE_UIDS (unless special case)

### **XML Validation**
- **MUST** use valid XML syntax
- **MUST** match channel types in channels.xml
- **MUST** include required config parameters

---

## Common Pitfalls

❌ **Forgot to add to createMap()** → Discovery fails  
❌ **Wrong handler type** → Device doesn't work  
❌ **Missing from SUPPORTED_THING_TYPE_UIDS** → Validation fails  
❌ **Typo in UUID** → Device not recognized  
❌ **Missing XML definition** → Thing creation fails  
❌ **Wrong XML file** → Thing not found  
❌ **Zigbee device not in createZigbeeMap()** → RSSI shows wrong values (uses rssi instead of subDevRssi)  
❌ **ZigBee Bridge not in createZigbeeBridgeMap()** → Sub-devices won't be discovered through the bridge  
❌ **Forgot to update test classes** → Tests fail in CI/CD pipeline  
❌ **Added to wrong test category** → Test expects wrong handler type  

---

## Quick Verification

✅ **Validation passes**: `SonoffBindingConstants.validateDeviceMappings()`  
✅ **Discovery works**: Device appears in discovery  
✅ **Thing creates**: No errors in logs  
✅ **Channels work**: Device responds to commands  
✅ **Tests pass**: Unit and integration tests succeed  
✅ **Documentation updated**: Device listed in SUPPORTED_DEVICES.md  

---

## Example 1: Adding WiFi Device (UUID 999)

```java
// 1. SonoffBindingConstants.java
public static final ThingTypeUID THING_TYPE_999 = new ThingTypeUID(BINDING_ID, "999");

// 2. Add to collections
THING_TYPE_999, // in SUPPORTED_THING_TYPE_UIDS
THING_TYPE_999, // in DISCOVERABLE_THING_TYPE_UIDS
deviceTypes.put(999, THING_TYPE_999); // in createMap()

// 3. SonoffHandlerFactory.java
case "999":
    return new SonoffSwitchSingleHandler(thing);
```

```xml
<!-- 4. switch-things.xml -->
<thing-type id="999">
    <supported-bridge-type-refs>
        <bridge-type-ref id="account"/>
    </supported-bridge-type-refs>
    <label>SONOFF New WiFi Device</label>
    <!-- ... rest of definition -->
</thing-type>
```

```java
// 5. SonoffHandlerFactoryTest.java - Add to parameterized test
@ValueSource(strings = { "1", "6", "14", ..., "999" })

// 6. SonoffHandlerFactoryIntegrationTest.java - Add to array
String[] singleSwitchIds = { "1", "6", "14", ..., "999" };
```

```markdown
<!-- 7. SUPPORTED_DEVICES.md -->
| **999** | NewDevice | 🔄 Mixed | Single switch | New device model |
```

---

## Example 2: Adding Zigbee Device (UUID 7000)

```java
// 1. SonoffBindingConstants.java
public static final ThingTypeUID THING_TYPE_7000 = new ThingTypeUID(BINDING_ID, "7000");

// 2. Add to collections
THING_TYPE_7000, // in SUPPORTED_THING_TYPE_UIDS
THING_TYPE_7000, // in DISCOVERABLE_THING_TYPE_UIDS
zigbeeTypes.put(7000, THING_TYPE_7000); // in createZigbeeMap()

// 3. SonoffHandlerFactory.java
case "7000":
    return new SonoffZigbeeButtonHandler(thing);

// 4. subDevRssi is AUTOMATIC - no code changes needed!
// Adding to createZigbeeMap() automatically enables subDevRssi handling
```

```xml
<!-- 5. zigbee-things.xml -->
<thing-type id="7000">
    <supported-bridge-type-refs>
        <bridge-type-ref id="66"/>
        <bridge-type-ref id="168"/>
        <bridge-type-ref id="243"/>
    </supported-bridge-type-refs>
    <label>SONOFF Zigbee Wireless Switch</label>
    <description>Model: SNZB-01P</description>
    <channels>
        <channel id="cloudOnline" typeId="cloudOnline"/>
        <channel id="button0" typeId="button-press">
            <label>Single Press</label>
        </channel>
        <channel id="battery" typeId="battery-level"/>
        <channel id="rssi" typeId="rssi"/>
    </channels>
    <!-- ... rest of definition -->
</thing-type>
```

```java
// 6. SonoffHandlerFactoryTest.java - Add dedicated test
@Test
@DisplayName("Should create SonoffZigbeeButtonHandler for button device type")
void testCreateHandler_ButtonDevice() {
    ThingTypeUID thingType = new ThingTypeUID("sonoff", "7000");
    when(mockThing.getThingTypeUID()).thenReturn(thingType);
    ThingHandler handler = factory.createHandler(mockThing);
    assertNotNull(handler);
    assertEquals("SonoffZigbeeButtonHandler", handler.getClass().getSimpleName());
}

// 7. SonoffHandlerFactoryIntegrationTest.java - Add dedicated test call
testDeviceHandlerCreation("7000", "SonoffZigbeeButtonHandler");
```

```markdown
<!-- 8. SUPPORTED_DEVICES.md -->
| **7000** | SNZB-01P | ☁️ Cloud | Wireless switch | Zigbee button device |
```

---

## Example 3: Adding ZigBee Bridge Device (UUID 300)

```java
// 1. SonoffBindingConstants.java - Add constant
public static final ThingTypeUID THING_TYPE_300 = new ThingTypeUID(BINDING_ID, "300");

// 2. Add to collections
THING_TYPE_300, // in SUPPORTED_THING_TYPE_UIDS
THING_TYPE_300, // in DISCOVERABLE_THING_TYPE_UIDS
deviceTypes.put(300, THING_TYPE_300); // in createMap()

// 3. 🚨 CRITICAL: Add to createZigbeeBridgeMap() (ONLY for bridge devices!)
public static final Map<Integer, ThingTypeUID> createZigbeeBridgeMap() {
    Map<Integer, ThingTypeUID> zigbeeBridgeTypes = new HashMap<>();
    zigbeeBridgeTypes.put(66, THING_TYPE_66);
    zigbeeBridgeTypes.put(168, THING_TYPE_168);
    zigbeeBridgeTypes.put(243, THING_TYPE_243);
    zigbeeBridgeTypes.put(300, THING_TYPE_300);  // ADD NEW BRIDGE
    
    return Collections.unmodifiableMap(zigbeeBridgeTypes);
}

// 4. Update documentation comment
/**
 * Creates mapping for ZigBee Bridge device types.
 * 
 * Current bridges:
 * - 66: THING_TYPE_66 (ZB Bridge original)
 * - 168: THING_TYPE_168 (ZBBridge-P enhanced version)
 * - 243: THING_TYPE_243 (ZBridge-U USB version)
 * - 300: THING_TYPE_300 (New ZigBee Bridge)  // ADD THIS
 */

// 5. SonoffHandlerFactory.java
case "300":
    return new SonoffZigbeeBridgeHandler(thing);
```

```xml
<!-- 6. switch-things.xml (or create zigbee-bridge-things.xml) -->
<thing-type id="300">
    <supported-bridge-type-refs>
        <bridge-type-ref id="account"/>
    </supported-bridge-type-refs>
    <label>SONOFF New ZigBee Bridge</label>
    <description>Model: ZBBridge-X</description>
    <channels>
        <channel id="cloudOnline" typeId="cloudOnline"/>
        <!-- Bridge-specific channels -->
    </channels>
    <!-- ... rest of definition -->
</thing-type>
```

```java
// 7. SonoffHandlerFactoryTest.java - Add to bridge test
@ParameterizedTest
@ValueSource(strings = { "66", "168", "243", "300" })
@DisplayName("Should create SonoffZigbeeBridgeHandler for ZigBee bridge types")
void testCreateHandler_ZigbeeBridges(String deviceId) {
    // Test implementation
}

// 8. SonoffHandlerFactoryIntegrationTest.java
testDeviceHandlerCreation("300", "SonoffZigbeeBridgeHandler");
```

```markdown
<!-- 9. SUPPORTED_DEVICES.md -->
| **300** | ZBBridge-X | ☁️ Cloud | ZigBee Bridge | New generation ZigBee bridge |
```

**⚠️ IMPORTANT**: ZigBee bridges require the createZigbeeBridgeMap() update to enable sub-device discovery!

---

**🎯 Total Time**: ~15-30 minutes for simple devices  
**🔧 Files Modified**: 5-6 files typically (including tests)  
**✅ Validation**: Automated validation catches most issues  
**🧪 Testing**: Unit and integration tests ensure correctness  

**Need Help?** Check existing similar devices for patterns and examples.