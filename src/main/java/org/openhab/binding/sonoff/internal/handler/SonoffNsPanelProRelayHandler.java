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
import org.openhab.binding.sonoff.internal.dto.commands.MultiSwitch;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffNsPanelProRelayHandler} allows the handling of commands and updates to Devices with uuid 278
 * (NSPanel Pro Gen2 with dual relay - Smart Home Control Panel with dual relay and built-in ZigBee coordinator,
 * 86-type)
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
public class SonoffNsPanelProRelayHandler extends SonoffNsPanelProHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffNsPanelProRelayHandler.class);

    public SonoffNsPanelProRelayHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        SonoffCommandMessage message = null;
        switch (channelUID.getId()) {
            case "switch0":
                MultiSwitch multiSwitch0 = new MultiSwitch();
                MultiSwitch.Switch newSwitch0 = multiSwitch0.new Switch();
                newSwitch0.setOutlet(0);
                newSwitch0.setSwitch(command.toString().toLowerCase());
                multiSwitch0.getSwitches().add(newSwitch0);
                message = new SonoffCommandMessage("switches", getDeviceid(), isLocalOut ? true : false, multiSwitch0);
                break;
            case "switch1":
                MultiSwitch multiSwitch1 = new MultiSwitch();
                MultiSwitch.Switch newSwitch1 = multiSwitch1.new Switch();
                newSwitch1.setOutlet(1);
                newSwitch1.setSwitch(command.toString().toLowerCase());
                multiSwitch1.getSwitches().add(newSwitch1);
                message = new SonoffCommandMessage("switches", getDeviceid(), isLocalOut ? true : false, multiSwitch1);
                break;
            default:
                return;
        }
        if (message != null) {
            queueMessage(message);
        } else {
            logger.debug("Unable to send command as was null for device {}", getDeviceid());
        }
    }

    @Override
    public void updateDevice(SonoffDeviceState newDevice) {
        // Relay switch channels
        updateState("switch0", newDevice.getParameters().getSwitch0());
        updateState("switch1", newDevice.getParameters().getSwitch1());
        // Weather temperature (outdoor)
        updateState("temperature", newDevice.getParameters().getTemperature());
        // CPU temperature
        updateState("cpuTemperature", newDevice.getParameters().getCpuTemperature());
        // Storage info
        updateState("storageTotal", newDevice.getParameters().getStorageTotal());
        updateState("storageFree", newDevice.getParameters().getStorageFree());
        updateState("storageUsed", newDevice.getParameters().getStorageUsed());
        // Outdoor air quality
        updateState("airQualityCo", newDevice.getParameters().getAirQualityCo());
        updateState("airQualityNo2", newDevice.getParameters().getAirQualityNo2());
        updateState("airQualityO3", newDevice.getParameters().getAirQualityO3());
        updateState("airQualitySo2", newDevice.getParameters().getAirQualitySo2());
        updateState("pm2_5", newDevice.getParameters().getPm2_5());
        updateState("pm10", newDevice.getParameters().getPm10());
        updateState("usEpaIndex", newDevice.getParameters().getUsEpaIndex());
        updateState("gbDefraIndex", newDevice.getParameters().getGbDefraIndex());
        // Standard WiFi channels
        updateState("rssi", newDevice.getParameters().getRssi());
        updateState("ipaddress", newDevice.getIpAddress());
        // Connection states
        this.cloud = newDevice.getCloud();
        this.local = newDevice.getLocal();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", this.local ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }
}
