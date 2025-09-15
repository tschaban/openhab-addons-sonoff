# SonoffBaseBridgeHandler Test Coverage

Test coverage for the `SonoffBaseBridgeHandler` abstract class.

## Test Classes

### 1. SonoffBaseBridgeHandlerTest
**Unit tests covering core functionality**

#### Initialization Tests
- ✅ **Valid Configuration** - Proper setup with device ID and bridge handler
- ✅ **No Bridge** - Handles missing bridge scenario with appropriate offline status
- ✅ **Null Device State** - Handles missing device state with configuration error
- ✅ **Local Mode Unsupported** - Validates local mode compatibility checking
- ✅ **Local In Device** - Sets `isLocalIn` flag for supported devices (UIID 1)
- ✅ **Local Out Device** - Sets `isLocalOut` flag for supported devices (UIID 2)

#### Bridge Status Management Tests
- ✅ **Bridge Online** - Starts tasks and queues messages when bridge comes online
- ✅ **Bridge Offline** - Cancels tasks and sets offline status when bridge goes offline
- ✅ **Local Device Management** - Manages LAN service for local-capable devices
- ✅ **Task Already Started** - Prevents duplicate task initialization

#### Command Handling Tests
- ✅ **Refresh Command** - Properly ignores refresh commands (no message queuing)
- ✅ **SLED Command** - Creates and queues SLED online/offline commands
- ✅ **Unknown Channel** - Handles unknown channels gracefully without queuing
- ✅ **Null Message Handling** - Logs debug messages for null command scenarios

#### Message Queuing Tests
- ✅ **Valid Account** - Successfully queues messages through account handler
- ✅ **Null Account** - Handles null account scenario without exceptions

#### Status Update Tests
- ✅ **Local Mode with Support** - Online status when device supports local mode
- ✅ **Local Mode without Support** - Offline status with appropriate error message
- ✅ **Cloud Mode with Connection** - Online status when cloud connection available
- ✅ **Cloud Mode without Connection** - Offline status when cloud unavailable
- ✅ **Mixed Mode Scenarios** - Complex logic for cloud/local combinations
- ✅ **Null Account Status** - Default online status when no account handler

#### Lifecycle Management Tests
- ✅ **Dispose** - Proper cleanup of resources, listeners, and task cancellation
- ✅ **Device ID Retrieval** - Correct device ID access after initialization
- ✅ **Properties Update** - Property updates through framework integration

### 2. SonoffBaseBridgeHandlerIntegrationTest
**Integration tests covering component interactions**

#### Full Lifecycle Testing
- ✅ **Initialize to Dispose** - Complete lifecycle from initialization through cleanup
  - Device listener registration
  - Task management
  - Message queuing
  - Resource cleanup verification

#### Bridge Status Transitions
- ✅ **Online → Offline → Online** - Complex status transition scenarios
  - Task start/stop management
  - Status propagation
  - Connection state handling

#### Multi-Mode Operations
- ✅ **Cloud and Local Transitions** - Dynamic mode switching
  - Cloud-only operation with LAN offline status
  - Local-only operation with cloud offline status
  - Both connections online scenario

#### Error Recovery
- ✅ **Configuration Error Recovery** - Recovery from missing device state
  - Initial failure with appropriate error status
  - Successful recovery after configuration fix

#### Concurrent Operations
- ✅ **Status Updates and Commands** - Thread safety validation
  - Concurrent status update calls
  - Concurrent command handling
  - Deadlock prevention verification

## Test Architecture

### Mock Strategy
- **Lenient Mocking** - Uses `lenient()` to avoid stubbing warnings
- **Real Configuration** - Uses actual DeviceConfig objects
- **Direct Field Access** - Sets config fields directly

### Key Testing Patterns
- **Initialization Side Effects** - Accounts for call chains
- **Static Constants Usage** - Uses actual SonoffBindingConstants values
- **Null-Safe Testing** - Proper handling of null scenarios

## Test Execution

```bash
# Run bridge handler tests
mvn test -Dtest="SonoffBaseBridgeHandler*"

# Run unit tests only
mvn test -Dtest="SonoffBaseBridgeHandlerTest"

# Run integration tests only
mvn test -Dtest="SonoffBaseBridgeHandlerIntegrationTest"
```

## Key Features

- **Abstract Class Testing** - Uses concrete test implementation
- **Real Configuration** - Uses actual DeviceConfig objects
- **Comprehensive Coverage** - Unit and integration approaches
- **Mock Management** - Lenient mocking to avoid warnings