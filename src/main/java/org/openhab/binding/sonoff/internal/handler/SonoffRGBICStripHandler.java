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
import org.openhab.binding.sonoff.internal.communication.SonoffCommandMessage;
import org.openhab.binding.sonoff.internal.dto.commands.Brightness;
import org.openhab.binding.sonoff.internal.dto.commands.Mode;
import org.openhab.binding.sonoff.internal.dto.commands.RGBLight;
import org.openhab.binding.sonoff.internal.dto.commands.RhythmMode;
import org.openhab.binding.sonoff.internal.dto.commands.RhythmSensitivity;
import org.openhab.binding.sonoff.internal.dto.commands.SLed;
import org.openhab.binding.sonoff.internal.dto.commands.SingleSwitch;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffRGBICStripHandler} handles RGBIC Light Strips (UUID 173)
 * These devices have music sync functionality and per-mode speed/brightness settings
 *
 * @author Tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
public class SonoffRGBICStripHandler extends SonoffBaseDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(SonoffRGBICStripHandler.class);
    private Integer previousMode = 1;

    public SonoffRGBICStripHandler(Thing thing) {
        super(thing);
    }

    public void startTasks() {
    }

    public void cancelTasks() {
    }

    private void changeSwitch(String onOff) {
        SingleSwitch singleSwitch = new SingleSwitch();
        singleSwitch.setSwitch(onOff);
        SonoffCommandMessage message = new SonoffCommandMessage("switch", this.deviceid, isLocalOut ? true : false,
                singleSwitch);
        queueMessage(message);
    }

    private void changeBrightness(Integer value) {
        Brightness brightness = new Brightness();
        brightness.setBrightness(value);
        SonoffCommandMessage message = new SonoffCommandMessage("brightness", this.deviceid, false, brightness);
        queueMessage(message);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SonoffCommandMessage message = null;
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case "switch":
                    if (command instanceof OnOffType) {
                        changeSwitch(command.toString().toLowerCase());
                    }
                    return;
                case "sled":
                    SLed sled = new SLed();
                    sled.setSledOnline(command.toString().toLowerCase());
                    message = new SonoffCommandMessage("sledOnline", this.deviceid, false, sled);
                    break;
                case "brightness":
                    if (command instanceof PercentType) {
                        PercentType br = (PercentType) command;
                        Integer brightness = br.intValue();
                        changeBrightness(brightness);
                    }
                    return;
                case "speed":
                    // Note: UUID 173 uses per-mode speed values (speed35, speed37, etc.)
                    // This sets the speed for the current mode
                    logger.warn(
                            "Speed channel is not fully supported for UUID 173 - device uses per-mode speed settings");
                    break;
                case "mode":
                    Mode mode = new Mode();
                    mode.setMode(Integer.parseInt(command.toString().toLowerCase()));
                    message = new SonoffCommandMessage("mode", this.deviceid, false, mode);
                    break;
                case "sensitivity":
                    // UUID 173 uses rhythmSensitive instead of sensitivity
                    RhythmSensitivity sensitivity = new RhythmSensitivity();
                    sensitivity.setRhythmSensitive(Integer.parseInt(command.toString().toLowerCase()));
                    message = new SonoffCommandMessage("rhythmSensitive", this.deviceid, false, sensitivity);
                    break;
                case "color":
                    if (command.toString().contains(",")) {
                        int[] rgbColor = ColorUtil.hsbToRgb((HSBType) command);
                        RGBLight rgb = new RGBLight();
                        rgb.setColorR(rgbColor[0]);
                        rgb.setColorG(rgbColor[1]);
                        rgb.setColorB(rgbColor[2]);
                        message = new SonoffCommandMessage("color", this.deviceid, false, rgb);
                        break;
                    }
                    if (command instanceof OnOffType) {
                        logger.error("Please use the switch channel instead");
                        break;
                    }

                    if (command instanceof PercentType) {
                        logger.error("Please use the dimmer channel instead");
                        break;
                    }
                case "musicSwitch":
                    RhythmMode rhythmMode = new RhythmMode();
                    String musicSwitch = command.toString().toLowerCase();
                    if (musicSwitch.equals("on")) {
                        // Enable music mode (rhythmMode)
                        rhythmMode.setRhythmMode(1);
                    } else {
                        // Disable music mode
                        rhythmMode.setRhythmMode(0);
                    }
                    message = new SonoffCommandMessage("rhythmMode", this.deviceid, false, rhythmMode);
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
        logger.debug("UUID 173 updateDevice called for device {}", this.deviceid);

        // Log the raw parameters for debugging
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "UUID 173 device {} parameters: mode={}, bright={}, colorR={}, colorG={}, colorB={}, "
                            + "rhythmMode={}, rhythmSensitive={}, switch={}",
                    this.deviceid, newDevice.getParameters().getMode(), newDevice.getParameters().getColorBrightness(),
                    newDevice.getParameters().getColor(), newDevice.getParameters().getSwitch0());
        }

        // Update basic channels
        updateState("switch", newDevice.getParameters().getSwitch0());
        updateState("color", newDevice.getParameters().getColor());
        updateState("brightness", newDevice.getParameters().getColorBrightness());
        updateState("rssi", newDevice.getParameters().getRssi());
        updateState("sled", newDevice.getParameters().getNetworkLED());
        updateState("mode", newDevice.getParameters().getMode());

        // Store previous mode (for music switch functionality)
        if (newDevice.getParameters().getMode() != null
                && Integer.parseInt(newDevice.getParameters().getMode().toString()) != 12) {
            previousMode = Integer.parseInt(newDevice.getParameters().getMode().toString());
        }

        // Note: UUID 173 uses rhythmMode and rhythmSensitive instead of speed/sensitivity
        // These are device-specific parameters that need special handling
        if (newDevice.getParameters().getRhythmMode() != null) {
            updateState("musicSwitch", newDevice.getParameters().getRhythmMode());
        }

        if (newDevice.getParameters().getRhythmSensitivity() != null) {
            updateState("sensitivity", newDevice.getParameters().getRhythmSensitivity());
        } else {
            logger.debug("UUID 173 device {} has no rhythmSensitive parameter in state update", this.deviceid);
        }

        // Speed is per-mode for UUID 173 (speed35, speed37, etc.)
        // We don't update the speed channel as it's not directly available
        logger.trace("UUID 173 uses per-mode speed values (speed35, speed37, etc.) - not updating speed channel");

        // Connections
        this.cloud = newDevice.getCloud();
        updateState("cloudOnline", this.cloud ? new StringType("Connected") : new StringType("Disconnected"));
        updateStatus();

        logger.debug("UUID 173 device {} channels updated successfully", this.deviceid);
    }
}
