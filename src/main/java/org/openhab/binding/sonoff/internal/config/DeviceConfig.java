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
package org.openhab.binding.sonoff.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceConfig} class defines the configuration for all Device Things
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class DeviceConfig {

    public String deviceid = "";
    public Integer consumptionPoll = 86400;
    public Integer localPoll = 60;
    public Boolean consumption = false;
    public Boolean local = false;
    public Integer buttonResetTimeout = 500;
    public Integer motionResetTimeout = 60000;

    @Override
    public String toString() {
        return "[deviceid=" + deviceid + ", localPoll=" + localPoll + ", consumptionPoll=" + consumptionPoll
                + ", local=" + local + ", consumption=" + consumption + ", buttonResetTimeout=" + buttonResetTimeout
                + ", motionResetTimeout=" + motionResetTimeout + "]";
    }
}
