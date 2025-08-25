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
import org.openhab.binding.sonoff.internal.dto.commands.Consumption;
import org.openhab.binding.sonoff.internal.dto.commands.MultiSwitch;
import org.openhab.binding.sonoff.internal.dto.commands.SLed;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffSwitchPOWUgradedHandler} allows the handling of commands and updates to Devices with uuid 190
 *
 * @author added by by tschaban based on the David Murton code
 */
@NonNullByDefault
public class SonoffSwitchPOWUgradedHandler extends SonoffBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffSwitchPOWUgradedHandler.class);
    private @Nullable ScheduledFuture<?> localTask;
    private @Nullable ScheduledFuture<?> consumptionTask;

    public SonoffSwitchPOWUgradedHandler(Thing thing) {
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
            Integer localPoll = config.localPoll;
            Boolean consumption = config.consumption;
            Boolean local = config.local;

            // Task to poll for Consumption Data if we are using the cloud (POW / POWR2)
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

            // Task to poll the lan if we are in local only mode or internet access is blocked (POW / POWR2)
            Runnable localPollData = () -> {
                queueMessage(new SonoffCommandMessage("switches", this.deviceid, isLocalOut ? true : false,
                        new MultiSwitch()));
            };
            if ((mode.equals("local") || (this.cloud.equals(false) && mode.equals("mixed")))) {
                if (local.equals(true)) {
                    logger.debug("Starting local task for {}", config.deviceid);
                    localTask = scheduler.scheduleWithFixedDelay(localPollData, 10, localPoll, TimeUnit.SECONDS);
                }
            }
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
                    MultiSwitch multiSwitch = new MultiSwitch();
                    MultiSwitch.Switch newSwitch = multiSwitch.new Switch();
                    Integer outlet = 0;
                    newSwitch.setOutlet(outlet);
                    newSwitch.setSwitch(command.toString().toLowerCase());
                    multiSwitch.getSwitches().add(newSwitch);
                    message = new SonoffCommandMessage("switches", this.deviceid, isLocalOut ? true : false,
                            multiSwitch);
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
                logger.debug("Unable to send command as was null for device {}", this.deviceid);
            }
        }
    }

    @Override
    public void updateDevice(SonoffDeviceState newDevice) {
        // Switches
        updateState("switch", newDevice.getParameters().getSwitch0());
        updateState("power", newDevice.getParameters().getPower());
        updateState("voltage", newDevice.getParameters().getVoltage());
        updateState("current", newDevice.getParameters().getCurrent());
        updateState("rssi", newDevice.getParameters().getRssi());
        updateState("sled", newDevice.getParameters().getNetworkLED());
        updateState("todayKwh", newDevice.getParameters().getTodayKwh());
        // updateState("yesterdayKwh", newDevice.getParameters().getYesterdayKwh());
        updateState("monthKwh", newDevice.getParameters().getMonthKwh());
        // updateState("sevenKwh", newDevice.getParameters().getSevenKwh());
        // updateState("thirtyKwh", newDevice.getParameters().getThirtyKwh());
        // ("hundredKwh", newDevice.getParameters().getHundredKwh());
        updateState("ipaddress", newDevice.getIpAddress());
        // Connections
        this.cloud = newDevice.getCloud();
        this.local = newDevice.getLocal();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", this.local ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }
}
