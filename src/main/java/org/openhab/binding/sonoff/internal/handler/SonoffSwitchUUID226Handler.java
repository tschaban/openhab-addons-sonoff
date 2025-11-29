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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.communication.SonoffCommandMessage;
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.binding.sonoff.internal.dto.commands.CircuitBreakerSwitch;
import org.openhab.binding.sonoff.internal.dto.commands.Consumption;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffSwitchUUID226Handler} allows the handling of commands and updates to Devices with uuid 226
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class SonoffSwitchUUID226Handler extends SonoffBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffSwitchUUID226Handler.class);
    private @Nullable ScheduledFuture<?> localTask;
    private @Nullable ScheduledFuture<?> consumptionTask;

    public SonoffSwitchUUID226Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void startTasks() {
        logger.debug("Starting tasks for {}", this.deviceid);
        DeviceConfig config = this.getConfigAs(DeviceConfig.class);
        SonoffAccountHandler account = this.account;
        if (account != null) {
            String mode = account.getMode();
            Integer consumptionPoll = config.consumptionPoll;
            Boolean consumption = config.consumption;

            // Task to poll for Consumption Data if we are using the cloud
            Runnable consumptionData = () -> {
                if (this.cloud) {
                    queueMessage(new SonoffCommandMessage("consumption", this.deviceid, false, new Consumption()));
                }
            };
            if (!mode.equals("local") && consumption) {
                logger.debug("Starting consumption task for {}", this.deviceid);
                consumptionTask = scheduler.scheduleWithFixedDelay(consumptionData, 10, consumptionPoll,
                        TimeUnit.SECONDS);
            }

            // UUID 226 does not support LAN connection - cloud only device
            logger.debug("UUID 226 is cloud-only, LAN polling disabled for {}", this.deviceid);
        }
    }

    @Override
    public void cancelTasks() {
        logger.debug("Stopping tasks for {}", this.deviceid);
        final ScheduledFuture<?> localTask = this.localTask;
        if (localTask != null) {
            localTask.cancel(true);
            this.localTask = null;
        }
        final ScheduledFuture<?> consumptionTask = this.consumptionTask;
        if (consumptionTask != null) {
            consumptionTask.cancel(true);
            this.consumptionTask = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SonoffCommandMessage message = null;
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case "switch":
                    CircuitBreakerSwitch circuitBreakerSwitch = new CircuitBreakerSwitch();
                    circuitBreakerSwitch.setSwitch(command.toString().equalsIgnoreCase("ON"));
                    // UUID 226 is cloud-only, does not support LAN
                    message = new SonoffCommandMessage("switch", this.deviceid, false, circuitBreakerSwitch);
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
        // Switches
        updateState("switch", newDevice.getParameters().getSwitch0());
        // Electrical readings
        updateState("power", newDevice.getParameters().getPower());
        updateState("totalPower", newDevice.getParameters().getTotalPower());
        updateState("availablePower", newDevice.getParameters().getAvailablePower());
        updateState("voltage", newDevice.getParameters().getVoltage());
        updateState("current", newDevice.getParameters().getCurrent());
        // Status
        updateState("rssi", newDevice.getParameters().getRssi());
        updateState("ipaddress", newDevice.getIpAddress());
        // Connections
        this.cloud = newDevice.getCloud();
        this.local = newDevice.getLocal();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", this.local ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }
}
