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
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffZigbeeButtonHandler} allows the handling of commands and updates to Button Type
 * Devices
 *
 * @author Tschaban-A - Initial contribution
 */
@NonNullByDefault
public class SonoffZigbeeButtonHandler extends SonoffBaseZigbeeHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffZigbeeButtonHandler.class);

    private @Nullable ScheduledFuture<?> button0ResetTask;
    private @Nullable ScheduledFuture<?> button1ResetTask;
    private @Nullable ScheduledFuture<?> button2ResetTask;

    public SonoffZigbeeButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void updateDevice(SonoffDeviceState newDevice) {
        // Button press events - only update the button that was actually pressed

        if (newDevice.getParameters().getButton0() == OpenClosedType.OPEN) {
            updateState("button0", OpenClosedType.OPEN);
            updateState("button0TrigTime", newDevice.getParameters().getButton0TrigTime());
            schedulePressReset(0);
            newDevice.getParameters().setButton0(OpenClosedType.CLOSED);
        }
        if (newDevice.getParameters().getButton1() == OpenClosedType.OPEN) {
            updateState("button1", OpenClosedType.OPEN);
            updateState("button1TrigTime", newDevice.getParameters().getButton1TrigTime());
            schedulePressReset(1);
            newDevice.getParameters().setButton1(OpenClosedType.CLOSED);
        }
        if (newDevice.getParameters().getButton2() == OpenClosedType.OPEN) {
            updateState("button2", OpenClosedType.OPEN);
            updateState("button2TrigTime", newDevice.getParameters().getButton2TrigTime());
            schedulePressReset(2);
            newDevice.getParameters().setButton2(OpenClosedType.CLOSED);
        }

        // Other parameters
        updateState("rssi", newDevice.getParameters().getRssi());
        updateState("battery", newDevice.getParameters().getBatteryLevel());

        // Connections
        this.cloud = newDevice.getCloud();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
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
                }, 500, TimeUnit.MILLISECONDS);
                break;
            case 1:
                task = button1ResetTask;
                if (task != null) {
                    task.cancel(false);
                }
                button1ResetTask = scheduler.schedule(() -> {
                    updateState("button1", OpenClosedType.CLOSED);
                }, 500, TimeUnit.MILLISECONDS);
                break;
            case 2:
                task = button2ResetTask;
                if (task != null) {
                    task.cancel(false);
                }
                button2ResetTask = scheduler.schedule(() -> {
                    updateState("button2", OpenClosedType.CLOSED);
                }, 500, TimeUnit.MILLISECONDS);
                break;
        }
    }

    @Override
    public void startTasks() {
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
