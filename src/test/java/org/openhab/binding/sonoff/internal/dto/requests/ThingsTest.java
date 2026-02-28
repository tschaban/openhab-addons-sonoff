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
package org.openhab.binding.sonoff.internal.dto.requests;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Things}.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class ThingsTest {

    @Test
    @DisplayName("Things: Should set and get itemType")
    void testItemType() {
        Things thing = new Things();
        thing.setItemType(1);

        assertEquals(1, thing.getItemType());
    }

    @Test
    @DisplayName("Things: Should set and get id")
    void testId() {
        Things thing = new Things();
        thing.setId("device-abc-123");

        assertEquals("device-abc-123", thing.getId());
    }

    @Test
    @DisplayName("Things: Should handle both properties")
    void testBothProperties() {
        Things thing = new Things();
        thing.setItemType(5);
        thing.setId("my-device");

        assertEquals(5, thing.getItemType());
        assertEquals("my-device", thing.getId());
    }

    @Test
    @DisplayName("Things: Should handle null values")
    void testNullValues() {
        Things thing = new Things();

        assertNull(thing.getItemType());
        assertNull(thing.getId());
    }

    @Test
    @DisplayName("Things: Should handle different item types")
    void testDifferentItemTypes() {
        Things thing1 = new Things();
        thing1.setItemType(1);

        Things thing2 = new Things();
        thing2.setItemType(10);

        assertEquals(1, thing1.getItemType());
        assertEquals(10, thing2.getItemType());
    }
}
