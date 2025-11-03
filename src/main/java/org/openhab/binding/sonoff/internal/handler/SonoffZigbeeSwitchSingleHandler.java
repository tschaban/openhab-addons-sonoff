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
import org.openhab.binding.sonoff.internal.dto.commands.SingleSwitch;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffZigbeeSwitchSingleHandler} allows the handling of commands and updates to Zigbee single switch
 * devices.
 * This handler is specifically designed for Zigbee devices that support switch control through the cloud API.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
public class SonoffZigbeeSwitchSingleHandler extends SonoffBaseZigbeeHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffZigbeeSwitchSingleHandler.class);

    public SonoffZigbeeSwitchSingleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SonoffCommandMessage message = null;

        if (command instanceof RefreshType) {
            return;
        }

        switch (channelUID.getId()) {
            case "switch":
                SingleSwitch singleSwitch = new SingleSwitch();
                singleSwitch.setSwitch(command.toString().toLowerCase());
                // Zigbee devices only support cloud communication (isLocal = false)
                message = new SonoffCommandMessage("switch", this.deviceid, false, singleSwitch);
                logger.debug("Sending switch command for Zigbee device {}: {}", this.deviceid,
                        command.toString().toLowerCase());
                break;
            case "sled":
                SLed sled = new SLed();
                sled.setSledOnline(command.toString().toLowerCase());
                message = new SonoffCommandMessage("sledOnline", this.deviceid, false, sled);
                logger.debug("Sending sled command for Zigbee device {}: {}", this.deviceid,
                        command.toString().toLowerCase());
                break;

            default:
                logger.debug("Unknown channel for Zigbee switch device {}: {}", this.deviceid, channelUID.getId());
                break;
        }

        if (message != null) {
            queueMessage(message);
        } else {
            logger.debug("Unable to send command as message was null for device {}", this.deviceid);
        }
    }

    @Override
    public void updateDevice(SonoffDeviceState newDevice) {
        // Update switch state
        if (this.getThing().getChannel("switch") != null) {
            updateState("switch", newDevice.getParameters().getSwitch0());
        }
        updateState("sled", newDevice.getParameters().getNetworkLED());
        // Update signal strength
        updateState("rssi", newDevice.getParameters().getRssi());

        // Update cloud connection status
        this.cloud = newDevice.getCloud();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));

        updateStatus();

        logger.trace("Updated Zigbee switch device {}: switch={}, rssi={}, cloud={}", this.deviceid,
                newDevice.getParameters().getSwitch0(), newDevice.getParameters().getRssi(), this.cloud);
    }

    @Override
    public void startTasks() {
        // Zigbee devices don't require polling tasks as they are cloud-only
        // and receive updates via the cloud connection
    }

    @Override
    public void cancelTasks() {
        // No tasks to cancel for Zigbee devices
    }
}
