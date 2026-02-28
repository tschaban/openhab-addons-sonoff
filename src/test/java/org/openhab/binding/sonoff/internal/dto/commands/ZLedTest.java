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
 * Unit tests for {@link ZLed} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class ZLedTest {

    @Test
    @DisplayName("ZLed: Should set and get zled")
    void testZLedSetGet() {
        ZLed cmd = new ZLed();
        cmd.setZled("on");

        assertEquals("on", cmd.getZled());
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("ZLed: Should handle on/off states")
    void testZLedStates() {
        ZLed cmd = new ZLed();

        cmd.setZled("off");
        assertEquals("off", cmd.getZled());

        cmd.setZled("on");
        assertEquals("on", cmd.getZled());
    }
}
