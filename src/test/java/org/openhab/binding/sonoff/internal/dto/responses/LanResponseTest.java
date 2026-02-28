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
package org.openhab.binding.sonoff.internal.dto.responses;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author David Murton - Initial contribution
 */
@DisplayName("LanResponse DTO Tests")
class LanResponseTest {

    @Test
    @DisplayName("LanResponse: Should set and get error")
    void testErrorGetterSetter() {
        LanResponse response = new LanResponse();

        response.setError(0);

        assertEquals(0, response.getError());
    }

    @Test
    @DisplayName("LanResponse: Should set and get sequence string")
    void testSequenceGetterSetter() {
        LanResponse response = new LanResponse();
        String sequence = "seq-123";

        response.setSequence(sequence);

        assertEquals(sequence, response.getSequence());
    }

    @Test
    @DisplayName("LanResponse: Should set and get seq long")
    void testSeqGetterSetter() {
        LanResponse response = new LanResponse();
        Long seq = 123456789L;

        response.setSeq(seq);

        assertEquals(seq, response.getSeq());
    }

    @Test
    @DisplayName("LanResponse: Should handle null error")
    void testNullError() {
        LanResponse response = new LanResponse();

        response.setError(null);

        assertNull(response.getError());
    }

    @Test
    @DisplayName("LanResponse: Should handle null sequence")
    void testNullSequence() {
        LanResponse response = new LanResponse();

        response.setSequence(null);

        assertNull(response.getSequence());
    }

    @Test
    @DisplayName("LanResponse: Should handle null seq")
    void testNullSeq() {
        LanResponse response = new LanResponse();

        response.setSeq(null);

        assertNull(response.getSeq());
    }

    @Test
    @DisplayName("LanResponse: Should set all properties together")
    void testAllPropertiesTogether() {
        LanResponse response = new LanResponse();

        response.setError(404);
        response.setSequence("sequence-abc");
        response.setSeq(999888777L);

        assertEquals(404, response.getError());
        assertEquals("sequence-abc", response.getSequence());
        assertEquals(999888777L, response.getSeq());
    }

    @Test
    @DisplayName("LanResponse: Should handle different error codes")
    void testDifferentErrorCodes() {
        LanResponse response = new LanResponse();

        response.setError(0);
        assertEquals(0, response.getError());

        response.setError(400);
        assertEquals(400, response.getError());

        response.setError(500);
        assertEquals(500, response.getError());

        response.setError(-1);
        assertEquals(-1, response.getError());
    }

    @Test
    @DisplayName("LanResponse: Should handle empty sequence string")
    void testEmptySequenceString() {
        LanResponse response = new LanResponse();

        response.setSequence("");

        assertEquals("", response.getSequence());
    }

    @Test
    @DisplayName("LanResponse: Should handle zero and negative seq values")
    void testZeroAndNegativeSeq() {
        LanResponse response = new LanResponse();

        response.setSeq(0L);
        assertEquals(0L, response.getSeq());

        response.setSeq(-1L);
        assertEquals(-1L, response.getSeq());

        response.setSeq(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, response.getSeq());

        response.setSeq(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, response.getSeq());
    }
}
