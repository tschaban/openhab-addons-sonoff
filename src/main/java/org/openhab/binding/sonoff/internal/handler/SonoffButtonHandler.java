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
 * 
 */
package org.openhab.binding.sonoff.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffButtonHandler} allows the handling of commands and updates to WiFi Button Type
 * Devices
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
public class SonoffButtonHandler extends SonoffBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffButtonHandler.class);

    private @Nullable ScheduledFuture<?> button0ResetTask;
    private @Nullable ScheduledFuture<?> button1ResetTask;
    private @Nullable ScheduledFuture<?> button2ResetTask;

    private int buttonResetTimeout = 500; // Default value

    public SonoffButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // Read configuration
        DeviceConfig config = getConfigAs(DeviceConfig.class);
        this.buttonResetTimeout = config.buttonResetTimeout;

        logger.debug("Button reset timeout configured to: {} ms", buttonResetTimeout);

        // Call parent initialization
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // WiFi buttons are read-only - commands not supported
    }

    @Override
    public void updateDevice(SonoffDeviceState newDevice) {
        // Button press events - only update the button that was actually pressed
        for (int key = 0; key <= 2; key++) {
            if (newDevice.getParameters().getButton(key) == OpenClosedType.OPEN) {
                updateState("button" + key, OpenClosedType.OPEN);
                updateState("button" + key + "TrigTime", newDevice.getParameters().getButtonTrigTime(key));
                schedulePressReset(key);
                newDevice.getParameters().setButton(key, OpenClosedType.CLOSED);
            }
        }

        // WiFi status indicators
        updateState("rssi", newDevice.getParameters().getRssi());

        // Connections
        this.cloud = newDevice.getCloud();
        this.local = newDevice.getLocal();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", this.local ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }

    private void schedulePressReset(int key) {
        ScheduledFuture<?> task = null;
        switch (key) {
            case 0:
                task = button0ResetTask;
                if (task != null) {
                    task.cancel(false);
                }
                button0ResetTask = scheduler.schedule(() -> {
                    updateState("button0", OpenClosedType.CLOSED);
                }, buttonResetTimeout, TimeUnit.MILLISECONDS);
                break;
            case 1:
                task = button1ResetTask;
                if (task != null) {
                    task.cancel(false);
                }
                button1ResetTask = scheduler.schedule(() -> {
                    updateState("button1", OpenClosedType.CLOSED);
                }, buttonResetTimeout, TimeUnit.MILLISECONDS);
                break;
            case 2:
                task = button2ResetTask;
                if (task != null) {
                    task.cancel(false);
                }
                button2ResetTask = scheduler.schedule(() -> {
                    updateState("button2", OpenClosedType.CLOSED);
                }, buttonResetTimeout, TimeUnit.MILLISECONDS);
                break;
        }
    }

    @Override
    public void startTasks() {
        // No periodic tasks needed for button devices
    }

    @Override
    public void cancelTasks() {
        ScheduledFuture<?> task = button0ResetTask;
        if (task != null) {
            task.cancel(true);
            button0ResetTask = null;
        }
        task = button1ResetTask;
        if (task != null) {
            task.cancel(true);
            button1ResetTask = null;
        }
        task = button2ResetTask;
        if (task != null) {
            task.cancel(true);
            button2ResetTask = null;
        }
    }
}
