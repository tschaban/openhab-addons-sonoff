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
import org.openhab.binding.sonoff.internal.dto.commands.MultiSwitch;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffSwitchMultiHandler} allows the handling of commands and updates to Devices with uuid's:
 * 1
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class SonoffGateHandler extends SonoffBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffGateHandler.class);
    private @Nullable ScheduledFuture<?> localTask;

    public SonoffGateHandler(Thing thing) {
        super(thing);
    }

    public void startTasks() {
        if (isLocalIn) {
            if (account != null) {
                DeviceConfig config = this.getConfigAs(DeviceConfig.class);
                SonoffAccountHandler account = this.account;
                if (account != null) {
                    String mode = account.getMode();
                    Integer localPoll = config.localPoll;
                    Boolean local = config.local;
                    // Task to poll the lan if we are in local only mode or internet access is blocked (POW / POWR2)
                    Runnable localPollData = () -> {
                        queueMessage(new SonoffCommandMessage("switches", this.deviceid, isLocalOut ? true : false,
                                new MultiSwitch()));
                    };
                    if ((mode.equals("local") || (this.cloud.equals(false) && mode.equals("mixed")))) {
                        if (local.equals(true)) {
                            logger.debug("Starting local task for {}", config.deviceid);
                            localTask = scheduler.scheduleWithFixedDelay(localPollData, 10, localPoll,
                                    TimeUnit.SECONDS);
                        }
                    }
                }
            }
        }
    }

    public void cancelTasks() {
        final ScheduledFuture<?> localTask = this.localTask;
        if (localTask != null) {
            localTask.cancel(true);
            this.localTask = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SonoffCommandMessage message = null;
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case "door0":
                case "door1":
                case "door2":
                    MultiSwitch multiSwitch = new MultiSwitch();
                    MultiSwitch.Switch newSwitch = multiSwitch.new Switch();
                    Integer outlet = Integer.parseInt(channelUID.getId().substring(channelUID.getId().length() - 1));
                    newSwitch.setOutlet(outlet);
                    newSwitch.setSwitch(command.toString().toLowerCase());
                    multiSwitch.getSwitches().add(newSwitch);
                    message = new SonoffCommandMessage("switches", this.deviceid, isLocalOut ? true : false,
                            multiSwitch);
                    break;
                /**
                 * Not supported yet
                 * case "sled":
                 * SLed sled = new SLed();
                 * sled.setSledOnline(command.toString().toLowerCase());
                 * message = new SonoffCommandMessage("sledOnline", this.deviceid, false, sled);
                 * break;
                 */
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

        if (this.getThing().getChannel("door0") != null) {
            updateState("door0", newDevice.getParameters().getDoor0());
        }
        if (this.getThing().getChannel("door1") != null) {
            updateState("door1", newDevice.getParameters().getDoor1());
        }
        if (this.getThing().getChannel("door2") != null) {
            updateState("door2", newDevice.getParameters().getDoor2());
        }

        if (this.getThing().getChannel("doorsensor0") != null) {
            updateState("doorsensor0", newDevice.getParameters().getDoorSensor0());
        }
        if (this.getThing().getChannel("doorsensor1") != null) {
            updateState("doorsensor1", newDevice.getParameters().getDoorSensor1());
        }
        if (this.getThing().getChannel("doorsensor2") != null) {
            updateState("doorsensor2", newDevice.getParameters().getDoorSensor2());
        }
        /*
         * Not supported yet
         * 
         * updateState("rssi", newDevice.getParameters().getRssi());
         * updateState("sled", newDevice.getParameters().getNetworkLED());
         * updateState("ipaddress", newDevice.getIpAddress());
         * 
         */
        // Connections
        this.cloud = newDevice.getCloud();
        this.local = newDevice.getLocal();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", this.local ? new StringType("Connected") : new StringType("Disconnected"));

        updateStatus();
    }
}
