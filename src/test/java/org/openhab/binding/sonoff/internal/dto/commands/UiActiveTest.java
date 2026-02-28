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
 * Unit tests for {@link UiActive} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class UiActiveTest {

    @Test
    @DisplayName("UiActive: Should set and get uiActive")
    void testUiActiveSetGet() {
        UiActive cmd = new UiActive();
        cmd.setUiActive(60);

        assertEquals(60, cmd.getUiActive());
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("UiActive: Should handle different timeout values")
    void testUiActiveDifferentTimeouts() {
        UiActive cmd = new UiActive();

        cmd.setUiActive(30);
        assertEquals(30, cmd.getUiActive());

        cmd.setUiActive(600);
        assertEquals(600, cmd.getUiActive());
    }
}
