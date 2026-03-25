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
 * Unit tests for {@link Mode} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class ModeTest {

    @Test
    @DisplayName("Mode: Should set and get mode value")
    void testModeSetGet() {
        Mode cmd = new Mode();
        cmd.setMode(3);

        assertEquals(3, cmd.getMode(), "Mode should be 3");
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("Mode: Should handle different mode values")
    void testModeDifferentValues() {
        Mode cmd = new Mode();

        cmd.setMode(1);
        assertEquals(1, cmd.getMode());

        cmd.setMode(5);
        assertEquals(5, cmd.getMode());
    }
}
