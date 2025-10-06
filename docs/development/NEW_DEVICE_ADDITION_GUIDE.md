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

#### d) Add to LAN protocol sets (if LAN supported)
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
- `SonoffSwitchSingleHandler` - Single channel switches
- `SonoffSwitchMultiHandler` - Multi-channel switches  
- `SonoffSwitchPOWHandler` - Power monitoring (POW)
- `SonoffSwitchPOWR2Handler` - Advanced power monitoring (POWR2)
- `SonoffSwitchTHHandler` - Temperature/humidity sensors
- `SonoffRGBStripHandler` - RGB lighting
- `SonoffRGBCCTHandler` - RGB+CCT lighting
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
- `switch-things.xml` - Switches and relays
- `sensor-things.xml` - Sensors
- `light-things.xml` - Lighting devices
- `cam-things.xml` - Cameras
- Create new file if needed

### 5. **Update Documentation**
**File**: `docs/SUPPORTED_DEVICES.md`

Add entry to supported devices table:
```markdown
| **XXX** | ModelName | 🔄 Mixed | Device features | Device description |
```

### 6. **Create/Update Handler (if needed)**
**File**: `src/main/java/.../handler/SonoffNewDeviceHandler.java`

Only if existing handlers don't fit. Follow existing handler patterns.

---

## 🚨 MANDATORY Validation & Testing

### 7. **🚨 CRITICAL: Run Automated Validation**
```java
// Test validation passes
ValidationResult result = SonoffBindingConstants.validateDeviceMappings();
assertFalse(result.hasErrors());
```

**🚨 CRITICAL**: NEVER skip validation - catches 90% of common errors
**⚠️ WARNING**: Validation errors = device won't work in production

### 8. **🚨 CRITICAL: Test Discovery**
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

---

## Quick Verification

✅ **Validation passes**: `SonoffBindingConstants.validateDeviceMappings()`  
✅ **Discovery works**: Device appears in discovery  
✅ **Thing creates**: No errors in logs  
✅ **Channels work**: Device responds to commands  
✅ **Documentation updated**: Device listed in SUPPORTED_DEVICES.md  

---

## Example: Adding UUID 999

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
    <label>SONOFF New Device</label>
    <!-- ... rest of definition -->
</thing-type>
```

```markdown
<!-- 5. SUPPORTED_DEVICES.md -->
| **999** | NewDevice | 🔄 Mixed | Single switch | New device model |
```

---

**🎯 Total Time**: ~15-30 minutes for simple devices  
**🔧 Files Modified**: 3-4 files typically  
**✅ Validation**: Automated validation catches most issues  

**Need Help?** Check existing similar devices for patterns and examples.