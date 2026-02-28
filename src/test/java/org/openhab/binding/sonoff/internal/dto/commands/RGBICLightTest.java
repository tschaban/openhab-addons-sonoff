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
 * Unit tests for {@link RGBICLight} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class RGBICLightTest {

    @Test
    @DisplayName("RGBICLight: Should set and get all properties")
    void testRGBICLightAllProperties() {
        RGBICLight cmd = new RGBICLight();
        cmd.setSwitchState("on");
        cmd.setColorR(255);
        cmd.setColorG(128);
        cmd.setColorB(64);
        cmd.setMode(2);
        cmd.setBright(90);
        cmd.setLightType(1);

        assertEquals("on", cmd.getSwitchState());
        assertEquals(255, cmd.getColorR());
        assertEquals(128, cmd.getColorG());
        assertEquals(64, cmd.getColorB());
        assertEquals(2, cmd.getMode());
        assertEquals(90, cmd.getBright());
        assertEquals(1, cmd.getLightType());
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }
}
