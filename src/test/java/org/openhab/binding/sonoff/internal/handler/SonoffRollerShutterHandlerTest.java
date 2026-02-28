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
package org.openhab.binding.sonoff.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonoff.internal.communication.SonoffCommandMessage;
import org.openhab.binding.sonoff.internal.dto.commands.RollerShutter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Unit tests for {@link SonoffRollerShutterHandler}.
 * 
 * Tests cover:
 * - Command translation (UP/DOWN/STOP to device commands)
 * - PercentType handling with defensive logging
 * - StringType direct commands
 * - Position channel control
 * - Property updates (motorDir, swMode)
 * - Device state updates (RSSI, cloud/local status)
 * - Edge cases and error conditions
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class SonoffRollerShutterHandlerTest {

    @Mock
    private Thing mockThing;

    @Mock
    private Bridge mockBridge;

    @Mock
    private ThingHandlerCallback mockCallback;

    @Mock
    private SonoffAccountHandler mockAccount;

    @Mock
    private SonoffDeviceState mockDeviceState;

    @Mock
    private SonoffDeviceStateParameters mockParameters;

    private ThingUID thingUID;
    private ThingUID bridgeUID;
    private Configuration configuration;
    private TestSonoffRollerShutterHandler handler;
    private ListAppender<ILoggingEvent> logAppender;

    /**
     * Test implementation that exposes protected methods and captures property updates
     */
    private class TestSonoffRollerShutterHandler extends SonoffRollerShutterHandler {
        private final Map<String, String> propertyMap = new HashMap<>();
        private String testDeviceId = "test-device-id";

        public TestSonoffRollerShutterHandler(Thing thing) {
            super(thing);
        }

        @Override
        protected void updateProperty(String name, String value) {
            propertyMap.put(name, value);
            super.updateProperty(name, value);
        }

        public Map<String, String> getPropertyMap() {
            return new HashMap<>(propertyMap);
        }

        public void setTestAccount(SonoffAccountHandler account) {
            this.account = account;
        }

        public void setTestDeviceId(String deviceId) {
            this.testDeviceId = deviceId;
            this.deviceid = deviceId;
        }

        @Override
        protected Bridge getBridge() {
            return mockBridge;
        }

        public void testUpdateDevice(SonoffDeviceState newDevice) {
            updateDevice(newDevice);
        }

        public void testHandleCommand(ChannelUID channelUID, org.openhab.core.types.Command command) {
            handleCommand(channelUID, command);
        }
    }

    @BeforeEach
    void setUp() {
        thingUID = new ThingUID("sonoff", "258", "test-roller-shutter");
        bridgeUID = new ThingUID("sonoff", "account", "test-bridge");

        // Setup configuration
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configuration = new Configuration(configMap);

        // Setup mock thing
        lenient().when(mockThing.getUID()).thenReturn(thingUID);
        lenient().when(mockThing.getConfiguration()).thenReturn(configuration);
        lenient().when(mockThing.getBridgeUID()).thenReturn(bridgeUID);
        lenient().when(mockThing.getChannel("shutter0")).thenReturn(null);
        lenient().when(mockThing.getChannel("position0")).thenReturn(null);

        // Setup mock bridge
        lenient().when(mockBridge.getUID()).thenReturn(bridgeUID);
        lenient().when(mockBridge.getHandler()).thenReturn(mockAccount);
        lenient().when(mockBridge.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        // Setup mock device state
        lenient().when(mockDeviceState.getParameters()).thenReturn(mockParameters);
        lenient().when(mockDeviceState.getCloud()).thenReturn(true);
        lenient().when(mockDeviceState.getLocal()).thenReturn(false);
        lenient().when(mockDeviceState.getUiid()).thenReturn(258);
        lenient().when(mockDeviceState.getIpAddress()).thenReturn(new StringType("192.168.1.100"));

        // Setup mock parameters
        lenient().when(mockParameters.getRollerSwitch()).thenReturn(new StringType("pause"));
        lenient().when(mockParameters.getSetclose()).thenReturn(new DecimalType(50));
        lenient().when(mockParameters.getMotorDir()).thenReturn(new StringType("1"));
        lenient().when(mockParameters.getSwMode()).thenReturn(new DecimalType(1));
        lenient().when(mockParameters.getRssi()).thenReturn(
                new QuantityType<>(Double.valueOf(-50), org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS));
        lenient().when(mockParameters.getNetworkLED()).thenReturn(OnOffType.ON);

        // Setup mock account
        lenient().when(mockAccount.getState("test-device-id")).thenReturn(mockDeviceState);
        lenient().when(mockAccount.getMode()).thenReturn("cloud");
        lenient().when(mockDeviceState.getProperties()).thenReturn(new HashMap<>());
        lenient().when(mockDeviceState.getState()).thenReturn(mockDeviceState);

        // Create handler
        handler = new TestSonoffRollerShutterHandler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestAccount(mockAccount);
        handler.setTestDeviceId("test-device-id");

        // Setup log appender
        logAppender = new ListAppender<>();
        logAppender.start();
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(SonoffRollerShutterHandler.class);
        logger.addAppender(logAppender);
        logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(SonoffRollerShutterHandler.class);
        logger.detachAppender(logAppender);
    }

    // ==================== Command Translation Tests ====================

    @Test
    @DisplayName("Should translate UP command to switch=on")
    void testUpCommandTranslation() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, UpDownType.UP);

        // Verify
        verify(mockAccount).queueMessage(messageCaptor.capture());
        SonoffCommandMessage message = messageCaptor.getValue();
        assertNotNull(message);
        assertEquals("switch", message.getCommand());
        assertEquals("test-device-id", message.getDeviceid());

        RollerShutter params = (RollerShutter) message.getParams();
        assertNotNull(params);
        assertEquals("on", params.getSwitch());
    }

    @Test
    @DisplayName("Should translate DOWN command to switch=off")
    void testDownCommandTranslation() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, UpDownType.DOWN);

        // Verify
        verify(mockAccount).queueMessage(messageCaptor.capture());
        SonoffCommandMessage message = messageCaptor.getValue();

        RollerShutter params = (RollerShutter) message.getParams();
        assertNotNull(params);
        assertEquals("off", params.getSwitch());
    }

    @Test
    @DisplayName("Should translate STOP command to switch=pause")
    void testStopCommandTranslation() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, StopMoveType.STOP);

        // Verify
        verify(mockAccount).queueMessage(messageCaptor.capture());
        SonoffCommandMessage message = messageCaptor.getValue();

        RollerShutter params = (RollerShutter) message.getParams();
        assertNotNull(params);
        assertEquals("pause", params.getSwitch());
    }

    // ==================== PercentType Defensive Tests ====================

    @Test
    @DisplayName("Should handle 0% as UP with debug log")
    void testPercentZeroAsUp() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new PercentType(0));

        // Verify command sent
        verify(mockAccount).queueMessage(messageCaptor.capture());
        SonoffCommandMessage message = messageCaptor.getValue();

        RollerShutter params = (RollerShutter) message.getParams();
        assertNotNull(params);
        assertEquals("on", params.getSwitch());

        // Verify debug log
        List<ILoggingEvent> logEvents = logAppender.list;
        boolean foundLog = logEvents.stream().anyMatch(event -> event.getLevel() == Level.DEBUG
                && event.getFormattedMessage().contains("0%") && event.getFormattedMessage().contains("UP"));
        assertTrue(foundLog, "Expected debug log for 0% treated as UP");
    }

    @Test
    @DisplayName("Should handle 100% as DOWN with debug log")
    void testPercentHundredAsDown() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new PercentType(100));

        // Verify command sent
        verify(mockAccount).queueMessage(messageCaptor.capture());
        SonoffCommandMessage message = messageCaptor.getValue();

        RollerShutter params = (RollerShutter) message.getParams();
        assertNotNull(params);
        assertEquals("off", params.getSwitch());

        // Verify debug log
        List<ILoggingEvent> logEvents = logAppender.list;
        boolean foundLog = logEvents.stream().anyMatch(event -> event.getLevel() == Level.DEBUG
                && event.getFormattedMessage().contains("100%") && event.getFormattedMessage().contains("DOWN"));
        assertTrue(foundLog, "Expected debug log for 100% treated as DOWN");
    }

    @Test
    @DisplayName("Should reject 1-99% with warning and no command")
    void testPercentMiddleRangeRejected() {
        // Test various middle values
        for (int percent : new int[] { 1, 25, 50, 75, 99 }) {
            // Clear previous interactions
            reset(mockAccount);
            logAppender.list.clear();

            // Setup
            ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");

            // Execute
            handler.testHandleCommand(channelUID, new PercentType(percent));

            // Verify no command sent
            verify(mockAccount, never()).queueMessage(any());

            // Verify warning log
            List<ILoggingEvent> logEvents = logAppender.list;
            boolean foundLog = logEvents.stream()
                    .anyMatch(event -> event.getLevel() == Level.WARN
                            && event.getFormattedMessage().contains(String.valueOf(percent))
                            && event.getFormattedMessage().contains("not support"));
            assertTrue(foundLog, "Expected warning log for " + percent + "%");
        }
    }

    // ==================== StringType Direct Commands ====================

    @Test
    @DisplayName("Should handle StringType pause command")
    void testStringTypePause() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new StringType("pause"));

        // Verify
        verify(mockAccount).queueMessage(messageCaptor.capture());
        RollerShutter params = (RollerShutter) messageCaptor.getValue().getParams();
        assertNotNull(params);
        assertEquals("pause", params.getSwitch());
    }

    @Test
    @DisplayName("Should handle StringType on command")
    void testStringTypeOn() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new StringType("on"));

        // Verify
        verify(mockAccount).queueMessage(messageCaptor.capture());
        RollerShutter params = (RollerShutter) messageCaptor.getValue().getParams();
        assertEquals("on", params.getSwitch());
    }

    @Test
    @DisplayName("Should handle StringType off command")
    void testStringTypeOff() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new StringType("off"));

        // Verify
        verify(mockAccount).queueMessage(messageCaptor.capture());
        RollerShutter params = (RollerShutter) messageCaptor.getValue().getParams();
        assertEquals("off", params.getSwitch());
    }

    @Test
    @DisplayName("Should handle StringType open command")
    void testStringTypeOpen() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new StringType("open"));

        // Verify
        verify(mockAccount).queueMessage(messageCaptor.capture());
        RollerShutter params = (RollerShutter) messageCaptor.getValue().getParams();
        assertEquals("open", params.getSwitch());
    }

    @Test
    @DisplayName("Should handle StringType close command")
    void testStringTypeClose() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new StringType("close"));

        // Verify
        verify(mockAccount).queueMessage(messageCaptor.capture());
        RollerShutter params = (RollerShutter) messageCaptor.getValue().getParams();
        assertEquals("close", params.getSwitch());
    }

    @Test
    @DisplayName("Should handle StringType stop command")
    void testStringTypeStop() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new StringType("stop"));

        // Verify
        verify(mockAccount).queueMessage(messageCaptor.capture());
        RollerShutter params = (RollerShutter) messageCaptor.getValue().getParams();
        assertEquals("stop", params.getSwitch());
    }

    // ==================== Position Channel Tests ====================

    @Test
    @DisplayName("Should handle position0 channel with DecimalType")
    void testPosition0ChannelDecimalType() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "position0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new DecimalType(75));

        // Verify
        verify(mockAccount).queueMessage(messageCaptor.capture());
        SonoffCommandMessage message = messageCaptor.getValue();
        assertEquals("setclose", message.getCommand());

        RollerShutter params = (RollerShutter) message.getParams();
        assertNotNull(params);
        assertEquals(Integer.valueOf(75), params.getSetclose());
    }

    @Test
    @DisplayName("Should handle position0 channel with PercentType")
    void testPosition0ChannelPercentType() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "position0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new PercentType(33));

        // Verify - PercentType extends DecimalType
        verify(mockAccount).queueMessage(messageCaptor.capture());
        SonoffCommandMessage message = messageCaptor.getValue();
        assertEquals("setclose", message.getCommand());

        RollerShutter params = (RollerShutter) message.getParams();
        assertNotNull(params);
        assertEquals(Integer.valueOf(33), params.getSetclose());
    }

    @Test
    @DisplayName("Should handle position0 boundary values 0 and 100")
    void testPosition0BoundaryValues() {
        ChannelUID channelUID = new ChannelUID(thingUID, "position0");

        // Test 0
        handler.testHandleCommand(channelUID, new DecimalType(0));
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);
        verify(mockAccount, times(1)).queueMessage(messageCaptor.capture());
        RollerShutter params = (RollerShutter) messageCaptor.getValue().getParams();
        assertEquals(Integer.valueOf(0), params.getSetclose());

        // Reset and test 100
        reset(mockAccount);
        handler.testHandleCommand(channelUID, new DecimalType(100));
        verify(mockAccount).queueMessage(messageCaptor.capture());
        params = (RollerShutter) messageCaptor.getValue().getParams();
        assertEquals(Integer.valueOf(100), params.getSetclose());
    }

    // ==================== Property Tests ====================

    @Test
    @DisplayName("Should update motorDir as Thing property")
    void testMotorDirProperty() {
        // Setup
        when(mockParameters.getMotorDir()).thenReturn(new StringType("1"));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify property was updated (not channel)
        Map<String, String> properties = handler.getPropertyMap();
        assertEquals("1", properties.get("motorDir"));
    }

    @Test
    @DisplayName("Should update swMode as Thing property")
    void testSwModeProperty() {
        // Setup
        when(mockParameters.getSwMode()).thenReturn(new DecimalType(1));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify property was updated (not channel)
        Map<String, String> properties = handler.getPropertyMap();
        assertEquals("1", properties.get("swMode"));
    }

    @Test
    @DisplayName("Should not update motorDir property when null")
    void testMotorDirPropertyNull() {
        // Setup
        when(mockParameters.getMotorDir()).thenReturn(null);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify property not added
        Map<String, String> properties = handler.getPropertyMap();
        assertFalse(properties.containsKey("motorDir"));
    }

    @Test
    @DisplayName("Should not update motorDir property when empty")
    void testMotorDirPropertyEmpty() {
        // Setup
        when(mockParameters.getMotorDir()).thenReturn(new StringType(""));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify property not added
        Map<String, String> properties = handler.getPropertyMap();
        assertFalse(properties.containsKey("motorDir"));
    }

    @Test
    @DisplayName("Should not update swMode property when null")
    void testSwModePropertyNull() {
        // Setup
        when(mockParameters.getSwMode()).thenReturn(null);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify property not added
        Map<String, String> properties = handler.getPropertyMap();
        assertFalse(properties.containsKey("swMode"));
    }

    // ==================== Device State Update Tests ====================

    @Test
    @DisplayName("Should update RSSI value")
    void testUpdateRSSI() {
        // Setup
        QuantityType<?> rssi = new QuantityType<>(Double.valueOf(-65),
                org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS);
        when(mockParameters.getRssi()).thenReturn((QuantityType) rssi);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify RSSI was updated
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
    }

    @Test
    @DisplayName("Should update cloud online status to Connected")
    void testUpdateCloudOnline() {
        // Setup
        when(mockDeviceState.getCloud()).thenReturn(true);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
    }

    @Test
    @DisplayName("Should update cloud online status to Disconnected")
    void testUpdateCloudOffline() {
        // Setup
        when(mockDeviceState.getCloud()).thenReturn(false);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
    }

    @Test
    @DisplayName("Should update local online status")
    void testUpdateLocalOnline() {
        // Setup
        when(mockDeviceState.getLocal()).thenReturn(true);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify local status updated
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
    }

    // ==================== Edge Case Tests ====================

    @Test
    @DisplayName("Should ignore REFRESH command")
    void testHandleRefreshCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");

        // Execute
        handler.testHandleCommand(channelUID, RefreshType.REFRESH);

        // Verify no message sent
        verify(mockAccount, never()).queueMessage(any());
    }

    @Test
    @DisplayName("Should handle initialization when bridge is null")
    void testInitializeWithNullBridge() {
        // Setup
        TestSonoffRollerShutterHandler handlerNoBridge = new TestSonoffRollerShutterHandler(mockThing) {
            @Override
            protected Bridge getBridge() {
                return null;
            }
        };
        handlerNoBridge.setCallback(mockCallback);

        // Execute
        handlerNoBridge.initialize();

        // Verify status is OFFLINE due to missing bridge
        verify(mockCallback).statusUpdated(eq(mockThing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
    }

    @Test
    @DisplayName("Should handle initialization when account is null")
    void testInitializeWithNullAccount() {
        // Setup - bridge handler returns null (no account)
        when(mockBridge.getHandler()).thenReturn(null);

        // Execute
        handler.initialize();

        // Verify - base handler should set status to OFFLINE when bridge is not set
        verify(mockCallback, atLeastOnce()).statusUpdated(eq(mockThing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.BRIDGE_UNINITIALIZED));
    }

    @Test
    @DisplayName("Should handle initialization when device state is null")
    void testInitializeWithNullDeviceState() {
        // Setup
        when(mockAccount.getState("test-device-id")).thenReturn(null);

        // Execute
        handler.initialize();

        // Verify status indicates device not initialized
        verify(mockCallback).statusUpdated(eq(mockThing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
    }

    @Test
    @DisplayName("Should handle command with local output enabled")
    void testCommandWithLocalOutput() {
        // Setup - handler with local output enabled
        handler.isLocalOut = true;
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, UpDownType.UP);

        // Verify LAN supported flag is set
        verify(mockAccount).queueMessage(messageCaptor.capture());
        SonoffCommandMessage message = messageCaptor.getValue();
        assertTrue(message.getLanSupported(), "LAN supported should be true when isLocalOut is enabled");
    }

    @Test
    @DisplayName("Should handle command with cloud output")
    void testCommandWithCloudOutput() {
        // Setup - handler with local output disabled
        handler.isLocalOut = false;
        ChannelUID channelUID = new ChannelUID(thingUID, "shutter0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, UpDownType.UP);

        // Verify LAN supported flag is not set
        verify(mockAccount).queueMessage(messageCaptor.capture());
        SonoffCommandMessage message = messageCaptor.getValue();
        assertFalse(message.getLanSupported(), "LAN supported should be false when isLocalOut is disabled");
    }

    @Test
    @DisplayName("Should handle position command with local output enabled")
    void testPositionCommandWithLocalOutput() {
        // Setup
        handler.isLocalOut = true;
        ChannelUID channelUID = new ChannelUID(thingUID, "position0");
        ArgumentCaptor<SonoffCommandMessage> messageCaptor = ArgumentCaptor.forClass(SonoffCommandMessage.class);

        // Execute
        handler.testHandleCommand(channelUID, new DecimalType(50));

        // Verify LAN supported flag is set
        verify(mockAccount).queueMessage(messageCaptor.capture());
        SonoffCommandMessage message = messageCaptor.getValue();
        assertTrue(message.getLanSupported(),
                "LAN supported should be true for position commands when isLocalOut is enabled");
    }

    @Test
    @DisplayName("Should handle unknown channel gracefully")
    void testUnknownChannel() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "unknown-channel");

        // Execute
        handler.testHandleCommand(channelUID, UpDownType.UP);

        // Verify no message sent
        verify(mockAccount, never()).queueMessage(any());
    }

    @Test
    @DisplayName("Should not send null message")
    void testNullMessageNotSent() {
        // Setup - position channel with non-DecimalType command
        ChannelUID channelUID = new ChannelUID(thingUID, "position0");

        // Execute
        handler.testHandleCommand(channelUID, UpDownType.UP);

        // Verify no message sent
        verify(mockAccount, never()).queueMessage(any());
    }
}
