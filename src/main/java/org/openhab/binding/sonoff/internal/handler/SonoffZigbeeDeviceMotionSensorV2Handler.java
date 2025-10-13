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
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffZigbeeDeviceMotionSensorV2Handler} handles SNZB-03P motion sensor
 * which uses key-based motion detection (key:0) similar to button devices
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
public class SonoffZigbeeDeviceMotionSensorV2Handler extends SonoffBaseZigbeeHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffZigbeeDeviceMotionSensorV2Handler.class);

    private @Nullable ScheduledFuture<?> motionResetTask;

    private int motionResetTimeout = 60000; // Default 60 seconds

    public SonoffZigbeeDeviceMotionSensorV2Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // Read configuration
        DeviceConfig config = getConfigAs(DeviceConfig.class);
        this.motionResetTimeout = config.motionResetTimeout;

        logger.debug("Motion reset timeout configured to: {} ms", motionResetTimeout);

        // Call parent initialization
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void updateDevice(SonoffDeviceState newDevice) {
        // Motion detection via key press (key:0 indicates motion)
        if (newDevice.getParameters().getButton(0) == OpenClosedType.OPEN) {
            updateState("motion", OpenClosedType.OPEN);
            updateState("trigTime", newDevice.getParameters().getButtonTrigTime(0));
            scheduleMotionReset();
            // Reset the button state to prevent repeated triggers
            newDevice.getParameters().setButton(0, OpenClosedType.CLOSED);
        }

        // Other parameters
        updateState("battery", newDevice.getParameters().getBatteryLevel());
        updateState("rssi", newDevice.getParameters().getRssi());

        // Connections
        this.cloud = newDevice.getCloud();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();
    }

    private void scheduleMotionReset() {
        ScheduledFuture<?> task = motionResetTask;
        if (task != null) {
            task.cancel(false);
        }
        motionResetTask = scheduler.schedule(() -> {
            updateState("motion", OpenClosedType.CLOSED);
        }, motionResetTimeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void startTasks() {
    }

    @Override
    public void cancelTasks() {
        ScheduledFuture<?> task = motionResetTask;
        if (task != null) {
            task.cancel(true);
            motionResetTask = null;
        }
    }
}
