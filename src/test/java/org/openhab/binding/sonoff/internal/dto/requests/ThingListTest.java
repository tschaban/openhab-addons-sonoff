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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ThingList}.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class ThingListTest {

    @Test
    @DisplayName("ThingList: Should initialize with empty list")
    void testDefaultConstructor() {
        ThingList thingList = new ThingList();

        assertNotNull(thingList.getThings(), "Things list should not be null");
        assertTrue(thingList.getThings().isEmpty(), "Things list should be empty by default");
    }

    @Test
    @DisplayName("ThingList: Should set and get things list")
    void testSetAndGetThings() {
        ThingList thingList = new ThingList();
        List<Things> things = new ArrayList<>();

        Things thing1 = new Things();
        thing1.setId("device-1");
        thing1.setItemType(1);

        Things thing2 = new Things();
        thing2.setId("device-2");
        thing2.setItemType(2);

        things.add(thing1);
        things.add(thing2);

        thingList.setThings(things);

        assertEquals(2, thingList.getThings().size());
        assertEquals("device-1", thingList.getThings().get(0).getId());
        assertEquals("device-2", thingList.getThings().get(1).getId());
    }

    @Test
    @DisplayName("ThingList: Should handle empty list being set")
    void testSetEmptyList() {
        ThingList thingList = new ThingList();
        thingList.setThings(new ArrayList<>());

        assertTrue(thingList.getThings().isEmpty());
    }

    @Test
    @DisplayName("ThingList: Should handle multiple things")
    void testMultipleThings() {
        ThingList thingList = new ThingList();
        List<Things> things = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Things thing = new Things();
            thing.setId("device-" + i);
            thing.setItemType(i);
            things.add(thing);
        }

        thingList.setThings(things);

        assertEquals(5, thingList.getThings().size());
        assertEquals("device-3", thingList.getThings().get(3).getId());
        assertEquals(4, thingList.getThings().get(4).getItemType());
    }
}
