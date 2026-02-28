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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MultiSwitch} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class MultiSwitchTest {

    @Test
    @DisplayName("MultiSwitch: Should initialize with empty list")
    void testMultiSwitchEmptyList() {
        MultiSwitch cmd = new MultiSwitch();

        assertNotNull(cmd.getSwitches(), "Switches list should not be null");
        assertTrue(cmd.getSwitches().isEmpty(), "Switches list should be empty");
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("MultiSwitch: Should add and get switches")
    void testMultiSwitchAddSwitch() {
        MultiSwitch cmd = new MultiSwitch();
        MultiSwitch.Switch switch1 = cmd.new Switch();
        switch1.setSwitch("on");
        switch1.setOutlet(0);

        List<MultiSwitch.Switch> switches = new ArrayList<>();
        switches.add(switch1);
        cmd.setSwitches(switches);

        assertEquals(1, cmd.getSwitches().size(), "Should have 1 switch");
        assertEquals("on", cmd.getSwitches().get(0).getSwitch());
        assertEquals(0, cmd.getSwitches().get(0).getOutlet());
    }

    @Test
    @DisplayName("MultiSwitch: Should handle multiple switches")
    void testMultiSwitchMultipleSwitches() {
        MultiSwitch cmd = new MultiSwitch();
        List<MultiSwitch.Switch> switches = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            MultiSwitch.Switch sw = cmd.new Switch();
            sw.setSwitch(i % 2 == 0 ? "on" : "off");
            sw.setOutlet(i);
            switches.add(sw);
        }
        cmd.setSwitches(switches);

        assertEquals(4, cmd.getSwitches().size());
        assertEquals("on", cmd.getSwitches().get(0).getSwitch());
        assertEquals("off", cmd.getSwitches().get(1).getSwitch());
        assertEquals(2, cmd.getSwitches().get(2).getOutlet());
    }
}
