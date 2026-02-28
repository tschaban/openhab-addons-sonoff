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
 * Unit tests for {@link Brightness} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class BrightnessTest {

    @Test
    @DisplayName("Brightness: Should set and get brightness value")
    void testBrightnessSetGet() {
        Brightness cmd = new Brightness();
        cmd.setBrightness(75);

        assertEquals(75, cmd.getBrightness(), "Brightness value should be 75");
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("Brightness: Should handle null brightness")
    void testBrightnessNull() {
        Brightness cmd = new Brightness();
        cmd.setBrightness(null);

        assertNull(cmd.getBrightness(), "Brightness should be null");
    }

    @Test
    @DisplayName("Brightness: Should handle min/max values")
    void testBrightnessMinMax() {
        Brightness cmd = new Brightness();

        cmd.setBrightness(0);
        assertEquals(0, cmd.getBrightness(), "Brightness should be 0");

        cmd.setBrightness(100);
        assertEquals(100, cmd.getBrightness(), "Brightness should be 100");
    }
}
