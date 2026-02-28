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
 * Unit tests for {@link Consumption} command DTO.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class ConsumptionTest {

    @Test
    @DisplayName("Consumption: Should have default value")
    void testConsumptionDefaultValue() {
        Consumption cmd = new Consumption();

        assertEquals("get", cmd.getHundredDaysKwh(), "Default value should be 'get'");
        assertSame(cmd, cmd.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("Consumption: Should set and get hundredDaysKwh")
    void testConsumptionSetGet() {
        Consumption cmd = new Consumption();
        cmd.setHundredDaysKwh("123.45");

        assertEquals("123.45", cmd.getHundredDaysKwh(), "Value should be '123.45'");
    }
}
