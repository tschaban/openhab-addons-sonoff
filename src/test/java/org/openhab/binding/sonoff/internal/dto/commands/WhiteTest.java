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
package org.openhab.binding.sonoff.internal.dto.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link White} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class WhiteTest {

    @Test
    @DisplayName("White: Should set and get switch state")
    void testWhiteSwitchState() {
        White cmd = new White();
        cmd.setSwitch("on");

        assertEquals("on", cmd.getSwitch());
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("White: WhiteObject should set and get brightness")
    void testWhiteObjectBrightness() {
        White cmd = new White();
        cmd.getWhite().setBrightness(75);

        assertEquals(75, cmd.getWhite().getBrightness());
    }

    @Test
    @DisplayName("White: WhiteObject should set and get color temperature")
    void testWhiteObjectColorTemperature() {
        White cmd = new White();
        cmd.getWhite().setColorTemperature(4500);

        assertEquals(4500, cmd.getWhite().getColorTemperature());
    }

    @Test
    @DisplayName("White: Should handle both WhiteObject properties")
    void testWhiteObjectBothProperties() {
        White cmd = new White();
        White.WhiteObject whiteObj = cmd.getWhite();
        whiteObj.setBrightness(90);
        whiteObj.setColorTemperature(6000);

        assertEquals(90, whiteObj.getBrightness());
        assertEquals(6000, whiteObj.getColorTemperature());
    }
}
