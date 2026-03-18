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
import org.openhab.binding.sonoff.internal.dto.commands.UiActive;
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
    private @Nullable ScheduledFuture<?> sledOnlineTask;
    private @Nullable ScheduledFuture<?> uiActiveTask;
    private String currentSledOnline = "off";

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
            Boolean enableElectricityPolling = config.local;

            // Start sledOnline task in cloud/mixed mode (electrical data only available via cloud)
            if (!mode.equals("local") && enableElectricityPolling) {
                // UUID 276 devices need sledOnline command to trigger electrical measurements
                // Electrical data monitoring only works via cloud connection
                // Sending sledOnline command forces the device to report fresh electrical data
                Runnable sledOnlineData = () -> {
                    if (this.cloud) {
                        logger.debug(
                                "Sending sledOnline command to {} with current state '{}' to trigger electrical data update",
                                this.deviceid, currentSledOnline);
                        SLed sled = new SLed();
                        sled.setSledOnline(currentSledOnline); // Send current state to avoid toggling LED
                        queueMessage(new SonoffCommandMessage("sledOnline", this.deviceid, true, sled));
                    }
                };

                logger.debug("Starting sledOnline polling task for {} every {} seconds (cloud mode)", this.deviceid,
                        localPoll);
                sledOnlineTask = scheduler.scheduleWithFixedDelay(sledOnlineData, 10, localPoll, TimeUnit.SECONDS);

                // Send uiActive command every 50 seconds to trigger UI being active (workaround)
                // This simulates opening the device in the eWeLink app
                Runnable uiActiveData = () -> {
                    if (this.local) {
                        logger.debug("Sending uiActive command to {} via LAN to trigger electrical data update",
                                this.deviceid);
                        UiActive uiActive = new UiActive();
                        uiActive.setUiActive(60);
                        queueMessage(new SonoffCommandMessage("uiActive", this.deviceid, true, uiActive));
                    }
                };

                logger.debug("Starting uiActive polling task for {} every 50 seconds (LAN mode)", this.deviceid);
                uiActiveTask = scheduler.scheduleWithFixedDelay(uiActiveData, 15, 50, TimeUnit.SECONDS);
            } else if (mode.equals("local")) {
                logger.info("UUID 276 device {} in local-only mode - electrical data monitoring not available",
                        this.deviceid);
            } else {
                logger.info("UUID 276 device {} - electrical data polling disabled by configuration", this.deviceid);
            }
        }
    }

    @Override
    public void cancelTasks() {
        logger.debug("Stopping tasks for {}", this.deviceid);
        final ScheduledFuture<?> sledOnlineTask = this.sledOnlineTask;
        if (sledOnlineTask != null) {
            sledOnlineTask.cancel(true);
            this.sledOnlineTask = null;
        }
        final ScheduledFuture<?> uiActiveTask = this.uiActiveTask;
        if (uiActiveTask != null) {
            uiActiveTask.cancel(true);
            this.uiActiveTask = null;
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
        logger.debug("updateDevice called for {}, device online: cloud={}, local={}", this.deviceid,
                newDevice.getCloud(), newDevice.getLocal());

        // Switch
        updateState("switch", newDevice.getParameters().getSwitch0());

        // Power monitoring
        updateState("power", newDevice.getParameters().getPower());
        updateState("voltage", newDevice.getParameters().getVoltage());
        updateState("current", newDevice.getParameters().getCurrent());

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
        // Store current sledOnline state for polling
        this.currentSledOnline = newDevice.getParameters().getNetworkLED().toString().toLowerCase();
        updateState("ipaddress", newDevice.getIpAddress());

        // Connections
        this.cloud = newDevice.getCloud();
        this.local = newDevice.getLocal();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", this.local ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }
}
