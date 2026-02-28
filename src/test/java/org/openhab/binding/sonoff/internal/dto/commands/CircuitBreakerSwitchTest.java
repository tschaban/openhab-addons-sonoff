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
 * Unit tests for {@link CircuitBreakerSwitch} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class CircuitBreakerSwitchTest {

    @Test
    @DisplayName("CircuitBreakerSwitch: Should set and get switch state")
    void testCircuitBreakerSwitchSetGet() {
        CircuitBreakerSwitch cmd = new CircuitBreakerSwitch();
        cmd.setSwitch(true);

        assertEquals(true, cmd.getSwitch(), "Switch state should be true");
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("CircuitBreakerSwitch: Should handle switch states")
    void testCircuitBreakerSwitchStates() {
        CircuitBreakerSwitch cmd = new CircuitBreakerSwitch();

        cmd.setSwitch(false);
        assertEquals(false, cmd.getSwitch());

        cmd.setSwitch(true);
        assertEquals(true, cmd.getSwitch());
    }
}
