# Technical Implementation Guide for Adding WiFI Devices
## For AI Assistant Use Only - WiFi Devices Only

This guide provides complete technical details for implementing a new WiFi device based on user's minimal input.


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
- After the code is created for a device and it doesn't work on development environment, update technical implementation with subsequent fixes you will execute with a support of a human

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
- `params` → Available device parameters and channels, for examoles
- `params.switches[]` → Number of switch channels
- `params.sledOnline` → Has status LED
- `params.rssi` → Has RSSI monitoring
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
1. Add THING_TYPE constant to SonoffBindingConstants.java
2. Add device to all required collections (SUPPORTED_THING_TYPE_UIDS, DISCOVERABLE_THING_TYPE_UIDS
3. Add handler mapping to SonoffHandlerFactory.java
4. Create or update thing-type XML definition in appropriate OH-INF/thing/ file
5. Update test classes:
   - Add to SonoffHandlerFactoryTest.java (parameterized test or dedicated test)
   - Add to SonoffHandlerFactoryIntegrationTest.java
6. If needed, create new handler class following existing patterns
7. Verify all changes compile without errors
8. Run validation to ensure no issues

### Common Patterns

**WiFi Devices:**
- Add to `createMap()`
- Use WiFi-specific handlers (SonoffSwitchSingleHandler, SonoffSwitchMultiHandler, etc.)
- May support LAN protocol


### Test Patterns

**Parameterized Tests** (for common device types):
```java
@ValueSource(strings = { "1", "6", "14", ..., "XXX" })
```

**Dedicated Tests** (for unique handlers):
```java
@Test
void testCreateHandler_NewDevice() {
    // Test implementation
}
```

### Mock Setup Best Practices

```java
// Use lenient() to avoid unnecessary stubbing warnings
lenient().when(mockThing.getUID()).thenReturn(thingUID);

// Mock bridge status for initialization
lenient().when(mockBridge.getStatusInfo())
    .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

// Use reflection to set protected fields
Field accountField = SonoffBaseBridgeHandler.class.getDeclaredField("account");
accountField.setAccessible(true);
accountField.set(mockZigbeeBridge, mockAccount);
```

### Type Correctness

```java
// Button trigger times
DateTimeType (not StringType)

// RSSI
QuantityType<Power> with DECIBEL_MILLIWATTS

// Battery
QuantityType<Dimensionless> with PERCENT

// Scheduler
Use @SuppressWarnings("unchecked") for raw type handling
```

---


## Files Typically Modified

### Core Files (Always):
1. `src/main/java/.../SonoffBindingConstants.java`
2. `src/main/java/.../SonoffHandlerFactory.java`
3. `src/main/resources/OH-INF/thing/[category]-things.xml`
4. `docs/SUPPORTED_DEVICES.md`

### Test Files (Always):
5. `src/test/java/.../SonoffHandlerFactoryTest.java`
6. `src/test/java/.../SonoffHandlerFactoryIntegrationTest.java`

### Optional (If New Handler Needed):
7. `src/main/java/.../handler/SonoffNewDeviceHandler.java`
8. `src/test/java/.../handler/SonoffNewDeviceHandlerTest.java`

---


## Quick Reference: Handler Types

### WiFi Device Handlers

| Handler | Use Case | Example Devices |
|---------|----------|-----------------|
| `SonoffSwitchSingleHandler` | Single channel switches | BASIC, MINI, S20 |
| `SonoffSwitchMultiHandler` | Multi-channel switches | 4CH, T1-2C, T1-3C |
| `SonoffSwitchPOWHandler` | Power monitoring (POW) | POW |
| `SonoffSwitchPOWR2Handler` | Advanced power monitoring | POWR2, POWR3 |
| `SonoffSwitchTHHandler` | Temperature/humidity | TH10, TH16 |
| `SonoffRGBStripHandler` | RGB lighting | LED Strip |
| `SonoffRGBCCTHandler` | RGB+CCT lighting | B1, B05-B |
| `SonoffGateHandler` | Gate/door controllers | GK-200MP2-B |
| `SonoffGSMSocketHandler` | GSM-enabled devices | GSM Socket |


### Bridge Handlers

| Handler | Use Case |
|---------|----------|
| `SonoffAccountHandler` | eWeLink account bridge |
| `SonoffZigbeeBridgeHandler` | Zigbee bridge (66, 168, 243) |
| `SonoffRfBridgeHandler` | 433MHz RF bridge |

### Other Handlers

| Handler | Use Case |
|---------|----------|
| `SonoffRfDeviceHandler` | 433MHz RF devices |
| `SonoffMagneticSwitchHandler` | Magnetic switches |

---

## Quick Reference: XML Files

| File | Device Types |
|------|--------------|
| `switch-things.xml` | WiFi switches, relays, power monitoring |
| `sensor-things.xml` | WiFi sensors (temperature, humidity, etc.) |
| `light-things.xml` | WiFi lighting devices (RGB, CCT, dimmers) |
| `zigbee-things.xml` | Zigbee devices (sensors, switches, buttons) |
| `rf-things.xml` | 433MHz RF devices |
| `cam-things.xml` | Camera devices |
| `bridge-things.xml` | Bridge devices (account, Zigbee, RF) |

---

## Quick Reference: Channel Types

Common channels to include based on device type:

**Basic Channels (most devices):**
- `cloudOnline` - Cloud connection status
- `localOnline` - LAN connection status (WiFi devices with LAN support)

**Switch Channels:**
- `switch` - On/off control (single channel)
- `switch1`, `switch2`, etc. - Multi-channel control

**Sensor Channels:**
- `temperature` - Temperature reading
- `humidity` - Humidity reading
- `battery` - Battery level
- `rssi` - Signal strength

**Button Channels:**
- `button0`, `button1`, `button2` - Button press detection
- `button0TrigTime`, etc. - Button press timestamp

**Power Monitoring Channels:**
- `power` - Current power consumption
- `voltage` - Voltage
- `current` - Current
- `energy` - Total energy consumption

**Lighting Channels:**
- `brightness` - Brightness level
- `color` - RGB color
- `colorTemperature` - Color temperature (CCT)

---


### Update Documentation

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
4. ✅ Handler case added to SonoffHandlerFactory
5. ✅ XML syntax is valid
6. ✅ All required channels defined
7. ✅ Test methods updated
8. ✅ Device added to createMap()
9. ✅ Tests pass without errors or warnings
10. ✅ Validation passes: `SonoffBindingConstants.validateDeviceMappings()`
11. ✅ All files compile successfully
12. ✅  @author set to "tschaban/SmartnyDom" in new files


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


## Error Resolution

### "No binding found" error
**Cause:** Missing from `SUPPORTED_THING_TYPE_UIDS`  
**Fix:** Search for `SUPPORTED_THING_TYPE_UIDS` collection and add THING_TYPE_{UUID}

### Device not discovered
**Cause:** Missing from `DISCOVERABLE_THING_TYPE_UIDS`  
**Fix:** Search for `DISCOVERABLE_THING_TYPE_UIDS` collection and add THING_TYPE_{UUID}


### XML validation errors
**Cause:** Invalid XML syntax or missing channel type definitions
**Fix:** Validate XML against schema, check channel typeId references exist in channels.xml

### Handler not created
**Cause:** Missing case in `SonoffHandlerFactory`  
**Fix:** Add switch case mapping UUID to handler

### Channels not updating
**Cause:** Missing channel update logic in handler  
**Fix:** Implement `updateChannels()` for all channels from JSON


### Issue: Tests fail with "MissingMethodInvocation"
**Solution**: Cannot mock field access. Use reflection to set fields or override methods in test helper class.

### Issue: Tests fail with "UnnecessaryStubbing"
**Solution**: Use `lenient()` for all mock setup in `@BeforeEach` method.

### Issue: NullPointerException in tests
**Solution**: Mock `bridge.getStatusInfo()` to return valid ThingStatusInfo.

### Issue: Type mismatch errors
**Solution**: Use correct types (DateTimeType for timestamps, QuantityType with proper units for measurements).

### Issue: Validation fails
**Solution**: Check that device is added to ALL required collections and handler mapping exists.

---

## Files Modified Checklist

1. `SonoffBindingConstants.java` - 4 locations:
   - Thing type constant declaration
   - SUPPORTED_THING_TYPE_UIDS collection
   - DISCOVERABLE_THING_TYPE_UIDS collection
2. `{HandlerClassName}.java` - New handler class
3. `SonoffHandlerFactory.java` - Handler registration
4. `SonoffBindingConstantsTest.java` - Unit test
5. `SonoffHandlerFactoryTest.java` - Integration test
6. `README.md` - Documentation

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
✅ **Collections updated**: SUPPORTED_THING_TYPE_UIDS, DISCOVERABLE_THING_TYPE_UIDS
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

