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
import org.openhab.binding.sonoff.internal.dto.commands.SLed;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffSwitchWS01Handler} allows the handling of commands and updates to Devices with uuid 276
 * WS01TPF-E (ORB) - WiFi socket with Matter support and enhanced energy monitoring
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
public class SonoffSwitchWS01Handler extends SonoffBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffSwitchWS01Handler.class);
    private @Nullable ScheduledFuture<?> localTask;
    private OnOffType currentSledState = OnOffType.ON; // Track current sled state for polling

    public SonoffSwitchWS01Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void startTasks() {
        logger.debug("Starting tasks for {}", this.deviceid);
        DeviceConfig config = this.getConfigAs(DeviceConfig.class);
        SonoffAccountHandler account = this.account;
        if (account != null) {
            String mode = account.getMode();
            Integer localPoll = config.localPoll;
            Boolean local = config.local;

            // UUID 276 polling strategy: Send sled command with current state value
            // Device responds with full state including power/energy data
            // This avoids state changes while triggering device response
            Runnable localPollData = () -> {
                SLed sled = new SLed();
                String sledState = currentSledState == OnOffType.ON ? "on" : "off";
                sled.setSledOnline(sledState);
                logger.debug("Polling UUID 276 - sending sled={} to trigger state update", sledState);
                queueMessage(new SonoffCommandMessage("sledOnline", this.deviceid, true, sled));
            };

            // Start local polling when local is enabled and mode allows it
            if (local.equals(true) && !mode.equals("cloud")) {
                logger.debug("Starting local polling task for UUID 276 - querying power data every {} seconds",
                        localPoll);
                localTask = scheduler.scheduleWithFixedDelay(localPollData, 10, localPoll, TimeUnit.SECONDS);
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
                    // Update tracked state
                    if (command instanceof OnOffType) {
                        this.currentSledState = (OnOffType) command;
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
        // Switch
        updateState("switch", newDevice.getParameters().getSwitch0());

        // Power monitoring
        updateState("power", newDevice.getParameters().getPower());
        updateState("voltage", newDevice.getParameters().getVoltage());
        updateState("current", newDevice.getParameters().getCurrent());

        // Energy consumption
        updateState("dayKwh", newDevice.getParameters().getTodayKwh());
        updateState("weekKwh", newDevice.getParameters().getWeekKwh());
        updateState("monthKwh", newDevice.getParameters().getMonthKwh());
        updateState("yearKwh", newDevice.getParameters().getYearKwh());

        // Cost tracking
        updateState("costDay", newDevice.getParameters().getCostDay());
        updateState("costWeek", newDevice.getParameters().getCostWeek());
        updateState("costMonth", newDevice.getParameters().getCostMonth());
        updateState("costYear", newDevice.getParameters().getCostYear());

        // Runtime tracking
        updateState("dayRuntime", newDevice.getParameters().getDayRuntime());
        updateState("monthRuntime", newDevice.getParameters().getMonthRuntime());

        // Device status
        updateState("rssi", newDevice.getParameters().getRssi());
        updateState("sled", newDevice.getParameters().getNetworkLED());
        updateState("ipaddress", newDevice.getIpAddress());

        // Track sled state for polling
        this.currentSledState = newDevice.getParameters().getNetworkLED();

        // Connections
        this.cloud = newDevice.getCloud();
        this.local = newDevice.getLocal();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", this.local ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }
}
