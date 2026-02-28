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
 * Unit tests for {@link Login} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class LoginTest {

    @Test
    @DisplayName("Login: Should return self from getCommand")
    void testLoginGetCommand() {
        Login cmd = new Login();

        assertNotNull(cmd, "Login command should not be null");
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("Login: Should create multiple instances")
    void testLoginMultipleInstances() {
        Login cmd1 = new Login();
        Login cmd2 = new Login();

        assertNotSame(cmd1, cmd2, "Different instances should be different");
        assertSame(cmd1, cmd1.getCommand(), "Each should return itself");
        assertSame(cmd2, cmd2.getCommand());
    }
}
