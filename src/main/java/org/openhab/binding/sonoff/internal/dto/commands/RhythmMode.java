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
public class RhythmMode extends AbstractCommand<RhythmMode> implements Serializable {

    @SerializedName("rhythmMode")
    @Expose
    private Integer rhythmMode;

    private static final long serialVersionUID = 926026952352435369L;

    public Integer getRhythmMode() {
        return rhythmMode;
    }

    public void setRhythmMode(Integer rhythmMode) {
        this.rhythmMode = rhythmMode;
    }

    @Override
    public RhythmMode getCommand() {
        return this;
    }
}
