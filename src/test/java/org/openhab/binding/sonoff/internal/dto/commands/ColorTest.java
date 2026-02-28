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
 * Unit tests for {@link Color} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class ColorTest {

    @Test
    @DisplayName("Color: Should create with default constructor")
    void testColorDefaultConstructor() {
        Color cmd = new Color();

        assertNotNull(cmd.getColor(), "ColorObject should not be null");
        assertEquals(0, cmd.getColor().getColorR(), "Red should be 0");
        assertEquals(0, cmd.getColor().getColorG(), "Green should be 0");
        assertEquals(0, cmd.getColor().getColorB(), "Blue should be 0");
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("Color: Should create with RGB constructor")
    void testColorRGBConstructor() {
        Color cmd = new Color(255, 128, 64);

        assertEquals(255, cmd.getColor().getColorR(), "Red should be 255");
        assertEquals(128, cmd.getColor().getColorG(), "Green should be 128");
        assertEquals(64, cmd.getColor().getColorB(), "Blue should be 64");
    }

    @Test
    @DisplayName("Color: Should set color using setColor method with RGB")
    void testColorSetColorRGB() {
        Color cmd = new Color();
        cmd.setColor(100, 150, 200);

        assertEquals(100, cmd.getColor().getColorR(), "Red should be 100");
        assertEquals(150, cmd.getColor().getColorG(), "Green should be 150");
        assertEquals(200, cmd.getColor().getColorB(), "Blue should be 200");
    }

    @Test
    @DisplayName("Color: Should set and get switch state")
    void testColorSwitchState() {
        Color cmd = new Color();
        cmd.setSwitch("on");

        assertEquals("on", cmd.getSwitch(), "Switch should be 'on'");
    }

    @Test
    @DisplayName("Color: ColorObject should set and get brightness")
    void testColorObjectBrightness() {
        Color cmd = new Color();
        cmd.getColor().setBrightness(80);

        assertEquals(80, cmd.getColor().getBrightness(), "Brightness should be 80");
    }

    @Test
    @DisplayName("Color: ColorObject should set individual color components")
    void testColorObjectIndividualComponents() {
        Color cmd = new Color();
        Color.ColorObject colorObj = cmd.getColor();

        colorObj.setColorR(255);
        colorObj.setColorG(128);
        colorObj.setColorB(0);

        assertEquals(255, colorObj.getColorR());
        assertEquals(128, colorObj.getColorG());
        assertEquals(0, colorObj.getColorB());
    }
}
