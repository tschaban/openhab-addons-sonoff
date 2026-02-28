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
 * Unit tests for {@link Info} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class InfoTest {

    @Test
    @DisplayName("Info: Should set and get cmd")
    void testInfoSetGet() {
        Info cmd = new Info();
        cmd.setCmd("test-command");

        assertEquals("test-command", cmd.getCmd());
    }

    @Test
    @DisplayName("Info: Should handle null cmd")
    void testInfoNullCmd() {
        Info cmd = new Info();
        cmd.setCmd(null);

        assertNull(cmd.getCmd());
    }
}
