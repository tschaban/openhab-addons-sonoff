/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sonoff.internal.communication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonoff.internal.dto.commands.AbstractCommand;
import org.openhab.binding.sonoff.internal.dto.commands.SingleSwitch;
import org.openhab.binding.sonoff.internal.dto.commands.UiActive;

/**
 * Unit tests for {@link SonoffCommandMessage}
 * Tests all constructors, getters, and sequence management functionality
 *
 * @author OpenHAB Sonoff Binding - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SonoffCommandMessageTest {

    private AbstractCommand<?> mockCommand;
    private SingleSwitch singleSwitchCommand;
    private UiActive uiActiveCommand;

    @BeforeEach
    void setUp() {
        mockCommand = mock(AbstractCommand.class);
        
        // Create real command objects for testing
        singleSwitchCommand = new SingleSwitch();
        singleSwitchCommand.setSwitch("on");
        
        uiActiveCommand = new UiActive();
        uiActiveCommand.setUiActive(60);
    }

    @Test
    void testApiDeviceMessageConstructor() {
        // Arrange
        String deviceId = "test-device-123";
        
        // Act
        SonoffCommandMessage message = new SonoffCommandMessage(deviceId);
        
        // Assert
        assertEquals("device", message.getCommand());
        assertEquals(deviceId, message.getDeviceid());
        assertEquals(Long.valueOf(0L), message.getSequence());
        assertFalse(message.getLanSupported());
        assertNull(message.getParams());
    }

    @Test
    void testApiDevicesMessageConstructor() {
        // Act
        SonoffCommandMessage message = new SonoffCommandMessage();
        
        // Assert
        assertEquals("devices", message.getCommand());
        assertEquals("", message.getDeviceid());
        assertEquals(Long.valueOf(0L), message.getSequence());
        assertFalse(message.getLanSupported());
        assertNull(message.getParams());
    }

    @Test
    void testDeviceMessageConstructor() {
        // Arrange
        String command = "update";
        String deviceId = "device-456";
        Boolean lanSupported = true;
        
        // Act
        SonoffCommandMessage message = new SonoffCommandMessage(command, deviceId, lanSupported, singleSwitchCommand);
        
        // Assert
        assertEquals(command, message.getCommand());
        assertEquals(deviceId, message.getDeviceid());
        assertEquals(Long.valueOf(0L), message.getSequence());
        assertTrue(message.getLanSupported());
        assertEquals(singleSwitchCommand, message.getParams());
    }

    @Test
    void testDeviceMessageConstructor_WithNullParams() {
        // Arrange
        String command = "status";
        String deviceId = "device-789";
        Boolean lanSupported = false;
        
        // Act
        SonoffCommandMessage message = new SonoffCommandMessage(command, deviceId, lanSupported, null);
        
        // Assert
        assertEquals(command, message.getCommand());
        assertEquals(deviceId, message.getDeviceid());
        assertEquals(Long.valueOf(0L), message.getSequence());
        assertFalse(message.getLanSupported());
        assertNull(message.getParams());
    }

    @Test
    void testWebSocketLoginMessageConstructor() {
        // Arrange
        String command = "userOnline";
        
        // Act
        SonoffCommandMessage message = new SonoffCommandMessage(command, uiActiveCommand);
        
        // Assert
        assertEquals(command, message.getCommand());
        assertEquals("", message.getDeviceid());
        assertEquals(Long.valueOf(0L), message.getSequence());
        assertFalse(message.getLanSupported());
        assertEquals(uiActiveCommand, message.getParams());
    }

    @Test
    void testWebSocketLoginMessageConstructor_WithNullParams() {
        // Arrange
        String command = "ping";
        
        // Act
        SonoffCommandMessage message = new SonoffCommandMessage(command, (AbstractCommand<?>) null);
        
        // Assert
        assertEquals(command, message.getCommand());
        assertEquals("", message.getDeviceid());
        assertEquals(Long.valueOf(0L), message.getSequence());
        assertFalse(message.getLanSupported());
        assertNull(message.getParams());
    }

    @Test
    void testSetSequence() {
        // Arrange
        SonoffCommandMessage message = new SonoffCommandMessage();
        Long expectedSequence = 1234567890L;
        
        try (MockedStatic<SonoffCommandMessageUtilities> mockedUtilities = mockStatic(SonoffCommandMessageUtilities.class)) {
            mockedUtilities.when(SonoffCommandMessageUtilities::getSequence).thenReturn(expectedSequence);
            
            // Act
            message.setSequence();
            
            // Assert
            assertEquals(expectedSequence, message.getSequence());
            mockedUtilities.verify(SonoffCommandMessageUtilities::getSequence);
        }
    }

    @Test
    void testSetSequence_MultipleCallsUpdateSequence() {
        // Arrange
        SonoffCommandMessage message = new SonoffCommandMessage();
        Long firstSequence = 1000L;
        Long secondSequence = 2000L;
        
        try (MockedStatic<SonoffCommandMessageUtilities> mockedUtilities = mockStatic(SonoffCommandMessageUtilities.class)) {
            mockedUtilities.when(SonoffCommandMessageUtilities::getSequence)
                    .thenReturn(firstSequence)
                    .thenReturn(secondSequence);
            
            // Act
            message.setSequence();
            Long firstResult = message.getSequence();
            
            message.setSequence();
            Long secondResult = message.getSequence();
            
            // Assert
            assertEquals(firstSequence, firstResult);
            assertEquals(secondSequence, secondResult);
            mockedUtilities.verify(SonoffCommandMessageUtilities::getSequence, times(2));
        }
    }

    @Test
    void testGetCommand_ImmutableAfterConstruction() {
        // Arrange
        String originalCommand = "testCommand";
        SonoffCommandMessage message = new SonoffCommandMessage(originalCommand, mockCommand);
        
        // Act
        String retrievedCommand = message.getCommand();
        
        // Assert
        assertEquals(originalCommand, retrievedCommand);
        
        // Verify command cannot be changed (no setter exists)
        // This is tested by the fact that there's no setCommand method
        assertEquals(originalCommand, message.getCommand());
    }

    @Test
    void testGetDeviceid_DefaultEmpty() {
        // Arrange
        SonoffCommandMessage message = new SonoffCommandMessage();
        
        // Act
        String deviceId = message.getDeviceid();
        
        // Assert
        assertEquals("", deviceId);
    }

    @Test
    void testGetDeviceid_ImmutableAfterConstruction() {
        // Arrange
        String originalDeviceId = "immutable-device-id";
        SonoffCommandMessage message = new SonoffCommandMessage(originalDeviceId);
        
        // Act
        String retrievedDeviceId = message.getDeviceid();
        
        // Assert
        assertEquals(originalDeviceId, retrievedDeviceId);
        
        // Verify deviceid cannot be changed (no setter exists)
        assertEquals(originalDeviceId, message.getDeviceid());
    }

    @Test
    void testGetSequence_DefaultZero() {
        // Arrange
        SonoffCommandMessage message = new SonoffCommandMessage();
        
        // Act
        Long sequence = message.getSequence();
        
        // Assert
        assertEquals(Long.valueOf(0L), sequence);
    }

    @Test
    void testGetLanSupported_DefaultFalse() {
        // Arrange
        SonoffCommandMessage message = new SonoffCommandMessage();
        
        // Act
        Boolean lanSupported = message.getLanSupported();
        
        // Assert
        assertFalse(lanSupported);
    }

    @Test
    void testGetLanSupported_TrueWhenSet() {
        // Arrange
        SonoffCommandMessage message = new SonoffCommandMessage("test", "device", true, mockCommand);
        
        // Act
        Boolean lanSupported = message.getLanSupported();
        
        // Assert
        assertTrue(lanSupported);
    }

    @Test
    void testGetParams_NullByDefault() {
        // Arrange
        SonoffCommandMessage message = new SonoffCommandMessage();
        
        // Act
        AbstractCommand<?> params = message.getParams();
        
        // Assert
        assertNull(params);
    }

    @Test
    void testGetParams_ReturnsCorrectCommand() {
        // Arrange
        SonoffCommandMessage message = new SonoffCommandMessage("test", singleSwitchCommand);
        
        // Act
        AbstractCommand<?> params = message.getParams();
        
        // Assert
        assertEquals(singleSwitchCommand, params);
        assertSame(singleSwitchCommand, params);
    }

    @Test
    void testMessageImmutability() {
        // Arrange
        String command = "immutableTest";
        String deviceId = "immutable-device";
        Boolean lanSupported = true;
        
        // Act
        SonoffCommandMessage message = new SonoffCommandMessage(command, deviceId, lanSupported, singleSwitchCommand);
        
        // Assert - All getters should return the same values
        assertEquals(command, message.getCommand());
        assertEquals(deviceId, message.getDeviceid());
        assertTrue(message.getLanSupported());
        assertEquals(singleSwitchCommand, message.getParams());
        
        // Verify values don't change on subsequent calls
        assertEquals(command, message.getCommand());
        assertEquals(deviceId, message.getDeviceid());
        assertTrue(message.getLanSupported());
        assertEquals(singleSwitchCommand, message.getParams());
    }

    @Test
    void testDifferentCommandTypes() {
        // Test with SingleSwitch command
        SonoffCommandMessage switchMessage = new SonoffCommandMessage("switch", "device1", true, singleSwitchCommand);
        assertEquals(singleSwitchCommand, switchMessage.getParams());
        
        // Test with UiActive command
        SonoffCommandMessage uiActiveMessage = new SonoffCommandMessage("uiActive", uiActiveCommand);
        assertEquals(uiActiveCommand, uiActiveMessage.getParams());
        
        // Test with mock command
        SonoffCommandMessage mockMessage = new SonoffCommandMessage("mock", "device2", false, mockCommand);
        assertEquals(mockCommand, mockMessage.getParams());
    }

    @Test
    void testConstructorParameterValidation() {
        // Test that constructors accept null parameters gracefully
        assertDoesNotThrow(() -> new SonoffCommandMessage("test", "device", true, null));
        assertDoesNotThrow(() -> new SonoffCommandMessage("test", (AbstractCommand<?>) null));
        
        // Test that constructors accept empty strings
        assertDoesNotThrow(() -> new SonoffCommandMessage(""));
        assertDoesNotThrow(() -> new SonoffCommandMessage("", "", false, mockCommand));
        assertDoesNotThrow(() -> new SonoffCommandMessage("", mockCommand));
    }

    @Test
    void testSequenceIndependenceAcrossInstances() {
        // Arrange
        SonoffCommandMessage message1 = new SonoffCommandMessage();
        SonoffCommandMessage message2 = new SonoffCommandMessage("device1");
        
        Long sequence1 = 1000L;
        Long sequence2 = 2000L;
        
        try (MockedStatic<SonoffCommandMessageUtilities> mockedUtilities = mockStatic(SonoffCommandMessageUtilities.class)) {
            mockedUtilities.when(SonoffCommandMessageUtilities::getSequence)
                    .thenReturn(sequence1)
                    .thenReturn(sequence2);
            
            // Act
            message1.setSequence();
            message2.setSequence();
            
            // Assert
            assertEquals(sequence1, message1.getSequence());
            assertEquals(sequence2, message2.getSequence());
            assertNotEquals(message1.getSequence(), message2.getSequence());
        }
    }

    @Test
    void testToStringDoesNotThrowException() {
        // Arrange
        SonoffCommandMessage message = new SonoffCommandMessage("test", "device", true, singleSwitchCommand);
        
        // Act & Assert
        assertDoesNotThrow(() -> message.toString());
    }

    @Test
    void testEqualsAndHashCodeConsistency() {
        // Arrange
        SonoffCommandMessage message1 = new SonoffCommandMessage("test", "device", true, singleSwitchCommand);
        SonoffCommandMessage message2 = new SonoffCommandMessage("test", "device", true, singleSwitchCommand);
        
        // Act & Assert
        // Note: SonoffCommandMessage doesn't override equals/hashCode, so this tests Object.equals behavior
        assertNotEquals(message1, message2); // Different object instances
        assertEquals(message1, message1); // Same object reference
        
        // HashCode should be consistent for the same object
        assertEquals(message1.hashCode(), message1.hashCode());
    }
}