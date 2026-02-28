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
 * Unit tests for {@link SingleSwitch} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class SingleSwitchTest {

    @Test
    @DisplayName("SingleSwitch: Should set and get switch state")
    void testSingleSwitchSwitch() {
        SingleSwitch cmd = new SingleSwitch();
        cmd.setSwitch("on");

        assertEquals("on", cmd.getSwitch());
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("SingleSwitch: Should set and get voltage, current, power")
    void testSingleSwitchElectricalData() {
        SingleSwitch cmd = new SingleSwitch();
        cmd.setVoltage("230.5");
        cmd.setCurrent("1.25");
        cmd.setPower("288");

        assertEquals("230.5", cmd.getVoltage());
        assertEquals("1.25", cmd.getCurrent());
        assertEquals("288", cmd.getPower());
    }

    @Test
    @DisplayName("SingleSwitch: Should set and get uiActive")
    void testSingleSwitchUiActive() {
        SingleSwitch cmd = new SingleSwitch();
        cmd.setUiActive(60);

        assertEquals(60, cmd.getUiActive());
    }

    @Test
    @DisplayName("SingleSwitch: Should set and get sledOnline")
    void testSingleSwitchSledOnline() {
        SingleSwitch cmd = new SingleSwitch();
        cmd.setSledOnline("on");

        assertEquals("on", cmd.getSledOnline());
    }

    @Test
    @DisplayName("SingleSwitch: Should set and get startup")
    void testSingleSwitchStartup() {
        SingleSwitch cmd = new SingleSwitch();
        cmd.setStartup("off");

        assertEquals("off", cmd.getStartup());
    }

    @Test
    @DisplayName("SingleSwitch: Should handle all properties together")
    void testSingleSwitchAllProperties() {
        SingleSwitch cmd = new SingleSwitch();
        cmd.setSwitch("on");
        cmd.setVoltage("220");
        cmd.setCurrent("2.5");
        cmd.setPower("550");
        cmd.setUiActive(120);
        cmd.setSledOnline("off");
        cmd.setStartup("on");

        assertEquals("on", cmd.getSwitch());
        assertEquals("220", cmd.getVoltage());
        assertEquals("2.5", cmd.getCurrent());
        assertEquals("550", cmd.getPower());
        assertEquals(120, cmd.getUiActive());
        assertEquals("off", cmd.getSledOnline());
        assertEquals("on", cmd.getStartup());
    }
}
