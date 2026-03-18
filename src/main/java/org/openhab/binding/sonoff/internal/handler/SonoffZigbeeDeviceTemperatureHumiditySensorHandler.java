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
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;

/**
 * The {@link SonoffZigbeeDeviceTemperatureHumiditySensorHandler} is responsible for updates and handling commands
 * to/from Zigbee Devices
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class SonoffZigbeeDeviceTemperatureHumiditySensorHandler extends SonoffBaseZigbeeHandler {

    public SonoffZigbeeDeviceTemperatureHumiditySensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void updateDevice(SonoffDeviceState newDevice) {
        // Battery and trigger time
        updateState("battery", newDevice.getParameters().getBatteryLevel());
        updateState("trigTime", newDevice.getParameters().getTrigTime());
        // Current temperature and humidity
        updateState("temperature", newDevice.getParameters().getTemperature());
        updateState("humidity", newDevice.getParameters().getHumidity());
        // Temperature statistics (UUID 7038)
        updateState("temperatureMax", newDevice.getParameters().getTemperatureMax());
        updateState("temperatureMin", newDevice.getParameters().getTemperatureMin());
        updateState("temperatureAvg", newDevice.getParameters().getTemperatureAvg());
        // Humidity statistics (UUID 7038)
        updateState("humidityMax", newDevice.getParameters().getHumidityMax());
        updateState("humidityMin", newDevice.getParameters().getHumidityMin());
        updateState("humidityAvg", newDevice.getParameters().getHumidityAvg());
        // Connections
        this.cloud = newDevice.getCloud();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }

    @Override
    public void startTasks() {
    }

    @Override
    public void cancelTasks() {
    }
}
