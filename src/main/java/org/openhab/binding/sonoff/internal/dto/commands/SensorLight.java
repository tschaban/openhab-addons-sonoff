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
public class SensorLight extends AbstractCommand<SensorLight> implements Serializable {

    @SerializedName("sensorLight")
    @Expose
    private String sensorLight;
    private static final long serialVersionUID = 1205249120703729171L;

    public String getSensorLight() {
        return sensorLight;
    }

    public void setSensorLight(String sensorLight) {
        this.sensorLight = sensorLight;
    }

    @Override
    public SensorLight getCommand() {
        return this;
    }
}
