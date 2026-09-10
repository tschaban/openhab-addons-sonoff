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
import org.openhab.binding.sonoff.internal.dto.commands.SLed;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;

/**
 * The {@link SonoffNsPanelProHandler} allows the handling of commands and updates to Devices with uuid 195
 * (NSPanel Pro - Smart Home Control Panel with built-in ZigBee coordinator, 86-type and 120-type)
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
public class SonoffNsPanelProHandler extends SonoffBaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffNsPanelProHandler.class);

    public SonoffNsPanelProHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SonoffCommandMessage message = null;
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case "sled":
                    SLed sled = new SLed();
                    sled.setSledOnline(command.toString().toLowerCase());
                    message = new SonoffCommandMessage("sledOnline", getDeviceid(), isLocalOut ? true : false, sled);
                    break;
            }
            if (message != null) {
                queueMessage(message);
            } else {
                logger.debug("Unable to send command as was null for device {}", getDeviceid());
            }
        }
    }

    @Override
    public void updateDevice(SonoffDeviceState newDevice) {
        // Temperature
        updateState("temperature", newDevice.getParameters().getTemperature());
        // Standard WiFi channels
        updateState("rssi", newDevice.getParameters().getRssi());
        updateState("sled", newDevice.getParameters().getNetworkLED());
        updateState("ipaddress", newDevice.getIpAddress());
        // Version info
        updateState("fwVersion", newDevice.getParameters().getFwVersion());
        updateState("sysVersion", newDevice.getParameters().getSysVersion());
        updateState("zigbeeVersion", newDevice.getParameters().getZigbeeVersion());
        updateState("appVersion", newDevice.getParameters().getAppVersion());
        // Connection states
        this.cloud = newDevice.getCloud();
        this.local = newDevice.getLocal();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", this.local ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }

    // Required for ZigBee sub-device discovery
    public JsonArray getSubDevices() {
        JsonArray subDevices = new JsonArray();
        SonoffAccountHandler account = this.account;
        logger.info("Getting subdevices for NSPanel Pro bridge {}", this.deviceid);
        if (account != null) {
            SonoffDeviceState state = account.getState(this.deviceid);
            if (state != null) {
                subDevices = state.getSubDevices();
                logger.info("Got subdevices for NSPanel Pro bridge {}", this.deviceid);
            }
        }
        logger.info("Returning number of subdevices for NSPanel Pro bridge {}", subDevices.size());
        return subDevices;
    }

    @Override
    public void startTasks() {
    }

    @Override
    public void cancelTasks() {
    }
}
