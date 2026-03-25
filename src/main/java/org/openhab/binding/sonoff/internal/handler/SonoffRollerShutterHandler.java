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
import org.openhab.binding.sonoff.internal.dto.commands.RollerShutter;
import org.openhab.binding.sonoff.internal.dto.commands.SLed;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffRollerShutterHandler} allows the handling of commands and updates to Devices with uuid 258
 * (MINI-RBS - Smart Roller Shutter Switch)
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
public class SonoffRollerShutterHandler extends SonoffBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffRollerShutterHandler.class);
    private @Nullable ScheduledFuture<?> localTask;

    public SonoffRollerShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void startTasks() {
        if (isLocalIn) {
            DeviceConfig config = this.getConfigAs(DeviceConfig.class);
            SonoffAccountHandler account = this.account;
            if (account != null) {
                String mode = account.getMode();
                Integer localPoll = config.localPoll;
                Boolean local = config.local;

                // Task to poll the LAN if we are in local only mode or internet access is blocked
                Runnable localPollData = () -> {
                    // Request current state from device
                    queueMessage(new SonoffCommandMessage("switch", this.deviceid, isLocalOut ? true : false,
                            new RollerShutter()));
                };

                if ((mode.equals("local") || (this.cloud.equals(false) && mode.equals("mixed")))) {
                    if (local.equals(true)) {
                        logger.debug("Starting local task for roller shutter {}", config.deviceid);
                        localTask = scheduler.scheduleWithFixedDelay(localPollData, 10, localPoll, TimeUnit.SECONDS);
                    }
                }
            }
        }
    }

    @Override
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
                case "shutter0":
                    // Control roller shutter: UP/DOWN/STOP commands and defensive PercentType
                    RollerShutter switchCmd = new RollerShutter();
                    String deviceCommand = null;

                    if (command instanceof UpDownType) {
                        // Handle UP/DOWN commands
                        UpDownType upDownCmd = (UpDownType) command;
                        if (upDownCmd == UpDownType.UP) {
                            deviceCommand = "on"; // UP = open
                        } else if (upDownCmd == UpDownType.DOWN) {
                            deviceCommand = "off"; // DOWN = close
                        }
                    } else if (command instanceof StopMoveType) {
                        // Handle STOP command
                        StopMoveType stopCmd = (StopMoveType) command;
                        if (stopCmd == StopMoveType.STOP) {
                            deviceCommand = "pause"; // STOP = pause
                        }
                    } else if (command instanceof PercentType) {
                        // Defensive handling: only 0% and 100% allowed on control channel
                        int percent = ((PercentType) command).intValue();
                        if (percent == 0) {
                            deviceCommand = "on"; // 0% = fully open = UP
                            logger.debug("Shutter control channel received 0%, treating as UP (open)");
                        } else if (percent == 100) {
                            deviceCommand = "off"; // 100% = fully closed = DOWN
                            logger.debug("Shutter control channel received 100%, treating as DOWN (close)");
                        } else {
                            // 1-99%: no true positioning on this channel
                            logger.warn(
                                    "Shutter control channel does not support positioning ({}%). Use position0 channel for positioning, or UP/DOWN/STOP commands.",
                                    percent);
                        }
                    } else if (command instanceof StringType) {
                        // Direct string commands: pause, on, off, open, close, stop
                        deviceCommand = command.toString().toLowerCase();
                    }

                    if (deviceCommand != null) {
                        switchCmd.setSwitch(deviceCommand);
                        message = new SonoffCommandMessage("switch", this.deviceid, isLocalOut ? true : false,
                                switchCmd);
                    }
                    break;
                case "position0":
                    // Set position (0-100)
                    if (command instanceof DecimalType) {
                        RollerShutter positionCmd = new RollerShutter();
                        int position = ((DecimalType) command).intValue();
                        positionCmd.setSetclose(position);
                        message = new SonoffCommandMessage("setclose", this.deviceid, isLocalOut ? true : false,
                                positionCmd);
                    }
                    break;
                case "sled":
                    SLed sled = new SLed();
                    sled.setSledOnline(command.toString().toLowerCase());
                    message = new SonoffCommandMessage("sledOnline", this.deviceid, false, sled);
                    break;
            }
            if (message != null) {
                queueMessage(message);
            } else {
                logger.debug("Unable to send command as message was null for device {}", this.deviceid);
            }
        }
    }

    @Override
    public void updateDevice(SonoffDeviceState newDevice) {
        // Update switch state (pause/open/close/stop)
        if (this.getThing().getChannel("shutter0") != null) {
            updateState("shutter0", newDevice.getParameters().getRollerSwitch());
        }

        // Update position (position0: 0-100)
        if (this.getThing().getChannel("position0") != null) {
            updateState("position0", newDevice.getParameters().getSetclose());
        }

        // Update motor direction as Thing property (not a channel)
        StringType motorDir = newDevice.getParameters().getMotorDir();
        if (motorDir != null && !motorDir.toString().isEmpty()) {
            updateProperty("motorDir", motorDir.toString());
            logger.debug("Updated motorDir property to: {}", motorDir);
        }

        // Update switch mode as Thing property (not a channel)
        DecimalType swMode = newDevice.getParameters().getSwMode();
        if (swMode != null) {
            updateProperty("swMode", swMode.toString());
            logger.debug("Updated swMode property to: {}", swMode);
        }

        // Update standard WiFi device channels
        updateState("rssi", newDevice.getParameters().getRssi());
        updateState("sled", newDevice.getParameters().getNetworkLED());
        updateState("ipaddress", newDevice.getIpAddress());

        // Update connection states
        this.cloud = newDevice.getCloud();
        this.local = newDevice.getLocal();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", this.local ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }
}
