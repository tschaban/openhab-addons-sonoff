# Device Addition Checklist ✅

**Quick reference for adding new UUID devices**

## Before You Start
- [ ] Have device UUID from eWeLink API
- [ ] Know device model and capabilities  
- [ ] Identified appropriate handler type

## Code Changes Required

### 1. SonoffBindingConstants.java
- [ ] Add `THING_TYPE_XXX` constant
- [ ] Add to `SUPPORTED_THING_TYPE_UIDS`
- [ ] Add to `DISCOVERABLE_THING_TYPE_UIDS` (if discoverable)
- [ ] Add to `createMap()` method
- [ ] Add to LAN protocol sets (if LAN supported)

### 2. SonoffHandlerFactory.java  
- [ ] Add case in `createHandler()` switch statement
- [ ] Choose correct handler type

### 3. XML Thing Definition
- [ ] Add thing-type in appropriate `*-things.xml` file
- [ ] Include all required channels
- [ ] Add config parameters
- [ ] Set correct label and description

### 4. Documentation
- [ ] Add entry to `SUPPORTED_DEVICES.md`

## Validation
- [ ] Run `SonoffBindingConstants.validateDeviceMappings()`
- [ ] No validation errors
- [ ] Device appears in discovery
- [ ] Thing creation works
- [ ] Channels functional

## ⚠️ Critical Checks
- [ ] UUID matches eWeLink exactly
- [ ] No typos in UUID across files
- [ ] Correct handler selected
- [ ] XML syntax valid
- [ ] All collections updated

---

**Files to modify**: 3-4 files  
**Time needed**: 15-30 minutes  
**Validation**: Automated validation available