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
 * @author tschaban/SmartnyDom - Initial contribution
 */
public class SensorLightBr extends AbstractCommand<SensorLightBr> implements Serializable {

    @SerializedName("sensorLightBr")
    @Expose
    private Integer sensorLightBr;
    private static final long serialVersionUID = 1205249120703729172L;

    public Integer getSensorLightBr() {
        return sensorLightBr;
    }

    public void setSensorLightBr(Integer sensorLightBr) {
        this.sensorLightBr = sensorLightBr;
    }

    @Override
    public SensorLightBr getCommand() {
        return this;
    }
}
