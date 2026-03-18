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

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Murton - Initial contribution
 */
public class RhythmSensitivity extends AbstractCommand<RhythmSensitivity> implements Serializable {

    @SerializedName("rhythmSensitive")
    @Expose
    private Integer rhythmSensitive;

    private static final long serialVersionUID = 926026952352435370L;

    public Integer getRhythmSensitive() {
        return rhythmSensitive;
    }

    public void setRhythmSensitive(Integer rhythmSensitive) {
        this.rhythmSensitive = rhythmSensitive;
    }

    @Override
    public RhythmSensitivity getCommand() {
        return this;
    }
}
