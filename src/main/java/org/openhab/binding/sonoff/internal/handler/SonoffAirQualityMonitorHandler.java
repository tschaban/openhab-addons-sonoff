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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonoff.internal.communication.SonoffCommandMessage;
import org.openhab.binding.sonoff.internal.dto.commands.SensorLight;
import org.openhab.binding.sonoff.internal.dto.commands.SensorLightBr;
import org.openhab.binding.sonoff.internal.dto.commands.VoiceAlarm;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffAirQualityMonitorHandler} allows the handling of commands and updates to Devices with uuid 266
 * Supports two models: SAWF-08P (CO2 sensor) and SAWF-07P (PM2.5/PM10 sensor)
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
public class SonoffAirQualityMonitorHandler extends SonoffBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffAirQualityMonitorHandler.class);

    // Product model detection - SAWF-08P (CO2) or SAWF-07P (PM2.5/PM10)
    private String productModel = "";
    private boolean isCO2Model = false;
    private boolean isPMModel = false;

    // Track pending commands to prevent stale cached state from overwriting commanded values
    // Device doesn't echo display control values (sensorLight/sensorLightBr/voiceAlarm) in regular telemetry
    // so we maintain the commanded value and never accept device state updates for these channels
    private Boolean pendingSensorLight = null;
    private Integer pendingSensorLightBr = null;
    private Boolean pendingVoiceAlarm = null;

    public SonoffAirQualityMonitorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SonoffCommandMessage message = null;
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case "sensorLight":
                    if (command instanceof OnOffType) {
                        SensorLight sensorLight = new SensorLight();
                        boolean commandValue = command == OnOffType.ON;
                        // Device expects "true" or "false" string values (not "on"/"off")
                        sensorLight.setSensorLight(commandValue ? "true" : "false");
                        message = new SonoffCommandMessage("sensorLight", this.deviceid, isLocalOut ? true : false,
                                sensorLight);
                        // Store commanded value - device won't echo this back
                        pendingSensorLight = commandValue;
                        updateState("sensorLight", commandValue ? OnOffType.ON : OnOffType.OFF);
                        logger.debug("Sent sensorLight command: {}, updated channel immediately", commandValue);
                    }
                    break;
                case "sensorLightBr":
                    if (command instanceof DecimalType) {
                        SensorLightBr sensorLightBr = new SensorLightBr();
                        int uiValue = ((DecimalType) command).intValue();
                        // Device requires 10-100, but PercentType is always 0-100
                        // Map 0-9 to device minimum of 10
                        int deviceValue = uiValue < 10 ? 10 : (uiValue > 100 ? 100 : uiValue);
                        if (uiValue < 10) {
                            logger.debug("Mapping UI value {} to device minimum: 10", uiValue);
                        }
                        sensorLightBr.setSensorLightBr(deviceValue);
                        message = new SonoffCommandMessage("sensorLightBr", this.deviceid, isLocalOut ? true : false,
                                sensorLightBr);
                        // Store UI value (0-100) for optimistic update - device won't echo this back
                        pendingSensorLightBr = uiValue;
                        updateState("sensorLightBr", new PercentType(uiValue));
                        logger.debug("Sent sensorLightBr command: device={}, UI={}, updated channel immediately",
                                deviceValue, uiValue);
                    }
                    break;
                case "voiceAlarm":
                    if (command instanceof OnOffType) {
                        VoiceAlarm voiceAlarm = new VoiceAlarm();
                        boolean commandValue = command == OnOffType.ON;
                        // Device expects "true" or "false" string values (not "on"/"off")
                        voiceAlarm.setVoiceAlarm(commandValue ? "true" : "false");
                        message = new SonoffCommandMessage("voiceAlarm", this.deviceid, isLocalOut ? true : false,
                                voiceAlarm);
                        // Store commanded value - device won't echo this back
                        pendingVoiceAlarm = commandValue;
                        updateState("voiceAlarm", commandValue ? OnOffType.ON : OnOffType.OFF);
                        logger.debug("Sent voiceAlarm command: {}, updated channel immediately", commandValue);
                    }
                    break;
            }
            if (message != null) {
                queueMessage(message);
            } else {
                logger.debug("Unable to send command as was null for device {}", this.deviceid);
            }
        }
    }

    @Override
    public void updateDevice(SonoffDeviceState newDevice) {
        // Detect product model on first update
        if (productModel.isEmpty()) {
            productModel = newDevice.getModel();
            isCO2Model = "SAWF-08P".equals(productModel);
            isPMModel = "SAWF-07P".equals(productModel);
            logger.info("Detected Air Quality Monitor model: {} (CO2={}, PM={})", productModel, isCO2Model, isPMModel);

            // Update thing label to reflect detected model
            String modelLabel = isCO2Model ? "SAWF-08P (CO2 Sensor)"
                    : (isPMModel ? "SAWF-07P (PM2.5/PM10 Sensor)" : productModel);
            getThing().setLabel("Air Quality Monitor - " + modelLabel);

            // Remove channels that don't apply to this model
            removeInapplicableChannels();
        }

        // Common sensor channels - both models support temperature and humidity
        updateState("temperature", newDevice.getParameters().getTemperature());
        updateState("temperatureF", newDevice.getParameters().getTemperatureF());
        updateState("humidity", newDevice.getParameters().getHumidity());

        // Model-specific sensor channels
        if (isCO2Model) {
            // SAWF-08P: CO2 sensor
            updateState("co2", newDevice.getParameters().getCo2());
        } else if (isPMModel) {
            // SAWF-07P: PM2.5/PM10 sensors
            updateState("pm10", newDevice.getParameters().getPm10());
            updateState("pm2_5", newDevice.getParameters().getPm2_5());
        }

        // Display light controls - NEVER update from device state
        // Device doesn't echo these values in regular telemetry, only on explicit state changes from eWeLink app
        // If we have a pending command, keep it. Otherwise, use device state (initialization value)
        if (pendingSensorLight == null) {
            updateState("sensorLight", newDevice.getParameters().getSensorLight());
            logger.trace("Initialized sensorLight from device state: {}", newDevice.getParameters().getSensorLight());
        } else {
            logger.trace("Keeping commanded sensorLight value: {}, ignoring device state", pendingSensorLight);
        }

        if (pendingSensorLightBr == null) {
            updateState("sensorLightBr", newDevice.getParameters().getSensorLightBr());
            logger.trace("Initialized sensorLightBr from device state: {}",
                    newDevice.getParameters().getSensorLightBr());
        } else {
            logger.trace("Keeping commanded sensorLightBr value: {}, ignoring device state", pendingSensorLightBr);
        }

        if (pendingVoiceAlarm == null) {
            updateState("voiceAlarm", newDevice.getParameters().getVoiceAlarm());
            logger.trace("Initialized voiceAlarm from device state: {}", newDevice.getParameters().getVoiceAlarm());
        } else {
            logger.trace("Keeping commanded voiceAlarm value: {}, ignoring device state", pendingVoiceAlarm);
        }

        // Connection status
        updateState("rssi", newDevice.getParameters().getRssi());
        updateState("ipaddress", newDevice.getIpAddress());
        // Connections
        this.cloud = newDevice.getCloud();
        this.local = newDevice.getLocal();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", this.local ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }

    /**
     * Remove channels that don't apply to the detected model
     */
    private void removeInapplicableChannels() {
        if (isCO2Model) {
            // SAWF-08P: Remove PM2.5/PM10 channels
            removeChannelIfExists("pm10");
            removeChannelIfExists("pm2_5");
            logger.debug("Removed PM2.5/PM10 channels for CO2 model (SAWF-08P)");
        } else if (isPMModel) {
            // SAWF-07P: Remove CO2 channel
            removeChannelIfExists("co2");
            logger.debug("Removed CO2 channel for PM model (SAWF-07P)");
        }
    }

    /**
     * Remove a channel if it exists on the thing
     */
    private void removeChannelIfExists(String channelId) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
        if (getThing().getChannel(channelUID) != null) {
            updateThing(editThing().withoutChannel(channelUID).build());
            logger.trace("Removed channel: {}", channelId);
        }
    }

    @Override
    public void startTasks() {
    }

    @Override
    public void cancelTasks() {
    }
}
