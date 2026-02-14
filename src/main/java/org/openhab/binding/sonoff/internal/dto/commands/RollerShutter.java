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
 * DTO for Roller Shutter (UUID 258) commands
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
public class RollerShutter extends AbstractCommand<RollerShutter> implements Serializable {

    @SerializedName("switch")
    @Expose
    private String switch0;

    @SerializedName("setclose")
    @Expose
    private Integer setclose;

    private static final long serialVersionUID = 1205249120703729258L;

    public String getSwitch() {
        return switch0;
    }

    public void setSwitch(String switch0) {
        this.switch0 = switch0;
    }

    public Integer getSetclose() {
        return setclose;
    }

    public void setSetclose(Integer setclose) {
        this.setclose = setclose;
    }

    @Override
    public RollerShutter getCommand() {
        return this;
    }
}
