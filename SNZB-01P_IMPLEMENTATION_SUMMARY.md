# SNZB-01P (Sonoff Zigbee Wireless Switch) Implementation Summary

## Device Information
- **UUID**: 7000
- **Name**: Sonoff Zigbee Wireless Switch
- **Model**: SNZB-01P
- **Type**: Zigbee Button Device
- **Product Link**: https://itead.cc/product/sonoff-zigbee-wireless-switch-snzb-01p/

## Device Capabilities
The device reports three types of button press events:
- **Single Press** (key=0)
- **Double Press** (key=1)
- **Long Press** (key=2)

Each event includes a trigger time timestamp.

### JSON Message Format
```json
{
  "action": "update",
  "deviceid": "a4800efd21",
  "apikey": "2537db5e-d813-42c1-b351-628056d92d42",
  "userAgent": "device",
  "d_seq": 53875,
  "params": {
    "trigTime": "1760254921000",
    "key": 0  // 0=single, 1=double, 2=long
  }
}
```

### Additional Parameters
- Battery level (%)
- RSSI (signal strength)
- subDevRssi
- subDevRssiSetting

## Implementation Changes

### 1. Thing Definition (zigbee-things.xml)
Added new thing type with ID `7000`:
- Supports all three Zigbee bridge types (66, 168, 243)
- Channels use generic `button-press` and `trigTime` types with custom labels:
  - `cloudOnline` - Connection status
  - `button0` (type: `button-press`) - Custom label: "Single Press"
  - `button0TrigTime` (type: `trigTime`) - Custom label: "Single Press Trigger Time"
  - `button1` (type: `button-press`) - Custom label: "Double Press"
  - `button1TrigTime` (type: `trigTime`) - Custom label: "Double Press Trigger Time"
  - `button2` (type: `button-press`) - Custom label: "Long Press"
  - `button2TrigTime` (type: `trigTime`) - Custom label: "Long Press Trigger Time"
  - `battery` - Battery level (%)
  - `rssi` - Signal strength

This approach follows the same pattern as multi-channel switches (e.g., switch0/switch1 using generic "power" type with custom labels) and reuses the existing `trigTime` channel type used by other sensors.

### 2. Channel Definitions (channels.xml)
Added one new generic channel type (reusable for any button device):
- `button-press` - Generic Contact type for button press events

Reuses existing channel type:
- `trigTime` - Generic DateTime type for trigger timestamps (already exists for other sensors)

These generic types are customized per-channel in the thing definition with specific labels and descriptions.

### 3. Device State Parameters (SonoffDeviceStateParameters.java)
Added button state fields using generic naming:
- `button0`, `button1`, `button2` (OpenClosedType)
- `button0TrigTime`, `button1TrigTime`, `button2TrigTime` (DateTimeType)

Added generic methods with key parameter:
- `getButton(Integer key)` - Returns button state for specified key (0-2)
- `setButton(Integer key, OpenClosedType state)` - Sets button state for specified key
- `getButtonTrigTime(Integer key)` - Returns trigger time for specified key
- `setButtonTrigTime(Integer key, String trigTime)` - Sets trigger time for specified key
- `setButtonPress(Integer key, String trigTime)` - Convenience method that sets button to OPEN and updates trigger time
- `resetButtonPress(Integer key)` - Convenience method that sets button to CLOSED

All methods use switch statements to map key values (0-2) to the appropriate button fields.

### 4. Device State Parsing (SonoffDeviceState.java)
Added parsing logic for the `key` parameter:
```java
if (params.get("key") != null && params.get("trigTime") != null) {
    Integer key = params.get("key").getAsInt();
    String trigTime = params.get("trigTime").getAsString();
    parameters.setButtonPress(key, trigTime);
}
```

### 5. Button Handler (SonoffZigbeeButtonHandler.java)
Created new handler class extending `SonoffBaseZigbeeHandler`:
- Uses generic channel naming: `button0`, `button1`, `button2` and `button0TrigTime`, `button1TrigTime`, `button2TrigTime`
- Uses loop-based approach to check all buttons (0-2) for press events
- Calls generic `getButton(key)` and `getButtonTrigTime(key)` methods
- Only updates the button channel that was actually pressed (checks which button is OPEN)
- Immediately resets the button parameter to CLOSED using `setButton(key, CLOSED)` after processing
- Updates battery and RSSI channels
- Implements auto-reset mechanism (500ms delay) for button press channel states
- Properly manages scheduled tasks for state resets (button0ResetTask, button1ResetTask, button2ResetTask)
- Implements required abstract methods from base class

### 6. Binding Constants (SonoffBindingConstants.java)
- Added `THING_TYPE_7000` constant
- Added to `SUPPORTED_THING_TYPE_UIDS` collection
- Added to `DISCOVERABLE_THING_TYPE_UIDS` collection
- Added to `createZigbeeMap()` method for device discovery

### 7. Handler Factory (SonoffHandlerFactory.java)
- Added import for `SonoffZigbeeButtonHandler`
- Added case "7000" to return new `SonoffZigbeeButtonHandler(thing)`

## Design Decisions

### Generic Naming Convention
Complete generic naming pattern throughout the codebase:
- **Fields**: `button0`, `button1`, `button2` (OpenClosedType)
- **TrigTime Fields**: `button0TrigTime`, `button1TrigTime`, `button2TrigTime` (DateTimeType)
- **Methods**: Generic with key parameter - `getButton(key)`, `setButton(key, state)`, `getButtonTrigTime(key)`, `setButtonTrigTime(key, time)`
- **Channel IDs**: `button0`, `button1`, `button2`, `button0TrigTime`, `button1TrigTime`, `button2TrigTime`
- **Channel Types**: Generic `button-press` (new) and `trigTime` (existing, reused)
- **Task Names**: `button0ResetTask`, `button1ResetTask`, `button2ResetTask`
- **Mapping**: key=0 → button0, key=1 → button1, key=2 → button2
- **User Labels**: Descriptive labels ("Single Press", "Double Press", "Long Press") defined in thing definition
- **Handler Logic**: Loop-based approach iterates through keys 0-2, making it easy to extend for more buttons
- This makes the code more maintainable, extensible, and follows existing patterns (e.g., switch0/switch1 using "power" type, RF button channels)
- Reuses existing `trigTime` channel type for consistency with other sensor devices

### Contact Type for Button Presses
Button press channels use `Contact` type (OPEN/CLOSED) instead of `Switch` (ON/OFF):
- OPEN state indicates button was pressed
- CLOSED state indicates no press (default/reset state)
- This follows OpenHAB conventions for momentary events

### Auto-Reset Mechanism
Button press channel states automatically reset to CLOSED after 500ms:
- Allows automation rules to trigger on state changes
- Prevents stuck "pressed" states
- Each button type has independent reset task (button0ResetTask, button1ResetTask, button2ResetTask)
- Tasks are properly cancelled on handler disposal
- Handler immediately resets button parameters to CLOSED after processing to prevent state pollution on next update

### Separate Trigger Time Channels
Each button type has its own trigger time channel:
- `button0TrigTime`, `button1TrigTime`, `button2TrigTime`
- Allows tracking of last occurrence for each press type
- Useful for debugging and automation logic
- Follows pattern used in other sensor devices

### No Command Support
The handler's `handleCommand()` method is empty because:
- SNZB-01P is a read-only sensor device
- It only reports events, doesn't accept commands
- This is consistent with other sensor implementations

## Testing Recommendations

1. **Discovery**: Verify device is discovered with correct thing type
2. **Single Press**: Test single button press updates correct channels
3. **Double Press**: Test double press detection and timing
4. **Long Press**: Test long press detection
5. **Auto-Reset**: Verify channels reset to CLOSED after 500ms
6. **Battery**: Verify battery level reporting
7. **RSSI**: Verify signal strength reporting
8. **Multiple Presses**: Test rapid succession of different press types
9. **Connection Status**: Verify cloudOnline channel updates correctly

## Files Modified

1. `src/main/resources/OH-INF/thing/zigbee-things.xml`
2. `src/main/resources/OH-INF/thing/channels.xml`
3. `src/main/java/org/openhab/binding/sonoff/internal/handler/SonoffDeviceStateParameters.java`
4. `src/main/java/org/openhab/binding/sonoff/internal/handler/SonoffDeviceState.java`
5. `src/main/java/org/openhab/binding/sonoff/internal/SonoffBindingConstants.java`
6. `src/main/java/org/openhab/binding/sonoff/internal/SonoffHandlerFactory.java`

## Files Created

1. `src/main/java/org/openhab/binding/sonoff/internal/handler/SonoffZigbeeButtonHandler.java`

## Compatibility

- Compatible with all Sonoff Zigbee bridges (66, 168, 243)
- Follows existing patterns from other Zigbee sensor implementations
- Uses standard OpenHAB channel types and patterns
- No breaking changes to existing functionality
