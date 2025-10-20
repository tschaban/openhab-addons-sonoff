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
package org.openhab.binding.sonoff.internal.handler;

import static org.openhab.core.library.unit.Units.*;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;

/**
 * The {@link SonoffDeviceStateParameters} contains the base state of a device
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class SonoffDeviceStateParameters {

    private final Logger logger = LoggerFactory.getLogger(SonoffDeviceStateParameters.class);

    // Parameters
    // Camera
    private OnOffType camPower = OnOffType.OFF;
    // Switches
    private OnOffType switch0 = OnOffType.OFF;
    private OnOffType switch1 = OnOffType.OFF;
    private OnOffType switch2 = OnOffType.OFF;
    private OnOffType switch3 = OnOffType.OFF;
    // Electric
    private QuantityType<Power> power = new QuantityType<Power>(0.0, WATT);
    private QuantityType<ElectricPotential> voltage = new QuantityType<ElectricPotential>(0.0, (VOLT));
    private QuantityType<ElectricCurrent> current = new QuantityType<ElectricCurrent>(0.0, (AMPERE));
    private QuantityType<ElectricPotential> battery = new QuantityType<ElectricPotential>(0.0, (VOLT));
    private QuantityType<Dimensionless> batteryLevel = new QuantityType<Dimensionless>(0.0, PERCENT);
    // Energy
    private QuantityType<Energy> todayKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);
    private QuantityType<Energy> yesterdayKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);
    private QuantityType<Energy> sevenKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);
    private QuantityType<Energy> monthKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);
    private QuantityType<Energy> thirtyKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);
    private QuantityType<Energy> hundredKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);
    // Sensors
    private StringType sensorType = new StringType();
    private QuantityType<Temperature> temperature = new QuantityType<Temperature>(0.0, SIUnits.CELSIUS);
    private QuantityType<Dimensionless> humidity = new QuantityType<Dimensionless>(0.0, PERCENT);
    // Actions
    private DateTimeType lastUpdate = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType actionTime = new DateTimeType(System.currentTimeMillis() + "");
    // RGB
    private PercentType speed = new PercentType(0);
    private DecimalType sensitivity = new DecimalType(0);
    private DecimalType mode = new DecimalType(0);
    private HSBType color = new HSBType();
    private OnOffType musicMode = OnOffType.OFF;
    private PercentType colourTemperature = new PercentType(0);
    private StringType ltype = new StringType();
    private PercentType whiteBrightness = new PercentType(0);
    private PercentType colorBrightness = new PercentType(0);
    // DoorSensor
    private OnOffType door0 = OnOffType.OFF;
    private OnOffType door1 = OnOffType.OFF;
    private OnOffType door2 = OnOffType.OFF;
    private OpenClosedType doorSensor0 = OpenClosedType.CLOSED;
    private OpenClosedType doorSensor1 = OpenClosedType.CLOSED;
    private OpenClosedType doorSensor2 = OpenClosedType.CLOSED;
    // Contact
    private OpenClosedType contact0 = OpenClosedType.CLOSED;

    // Other
    private OnOffType networkLED = OnOffType.OFF;
    private QuantityType<Power> rssi = new QuantityType<Power>(0.0, DECIBEL_MILLIWATTS);
    private OnOffType zigbeeLED = OnOffType.OFF;
    // RF
    private DateTimeType rf0 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf1 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf2 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf3 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf4 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf5 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf6 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf7 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf8 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf9 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf10 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf11 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf12 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf13 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf14 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf15 = new DateTimeType(System.currentTimeMillis() + "");
    private JsonArray rfCodeList = new JsonArray();
    // Zigbee
    private DateTimeType trigTime = new DateTimeType(System.currentTimeMillis() + "");
    private OnOffType motion = OnOffType.OFF;
    private StringType brightnessState = new StringType("");
    // Button
    private OpenClosedType button0 = OpenClosedType.CLOSED;
    private OpenClosedType button1 = OpenClosedType.CLOSED;
    private OpenClosedType button2 = OpenClosedType.CLOSED;
    private DateTimeType button0TrigTime = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType button1TrigTime = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType button2TrigTime = new DateTimeType(System.currentTimeMillis() + "");

    private double round(double value, int decimalPlaces) {
        double multiplier = Math.pow(10, decimalPlaces);
        return Math.round(value * multiplier) / multiplier;
    }

    public OnOffType getCamPower() {
        return this.camPower;
    }

    public void setCamPower(String power) {
        this.camPower = power.equals("on") ? OnOffType.ON : OnOffType.OFF;
    }

    public OnOffType getDoor0() {
        return this.door0;
    }

    public OnOffType getDoor1() {
        return this.door1;
    }

    public OnOffType getDoor2() {
        return this.door2;
    }

    public OpenClosedType getDoorSensor0() {
        return this.doorSensor0;
    }

    public OpenClosedType getDoorSensor1() {
        return this.doorSensor1;
    }

    public OpenClosedType getDoorSensor2() {
        return this.doorSensor2;
    }

    public void setDoor0(String door0) {
        this.door0 = door0.equals("on") ? OnOffType.ON : OnOffType.OFF;
        this.doorSensor0 = door0.equals("on") ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    public void setDoor1(String door1) {
        this.door1 = door1.equals("on") ? OnOffType.ON : OnOffType.OFF;
        this.doorSensor1 = door1.equals("on") ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    public void setDoor2(String door2) {
        this.door2 = door2.equals("on") ? OnOffType.ON : OnOffType.OFF;
        this.doorSensor2 = door2.equals("on") ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    public OpenClosedType getContact0() {
        return this.contact0;
    }

    public void setContact0(Integer contact0) {
        this.contact0 = contact0 == 1 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    public OnOffType getSwitch0() {
        return this.switch0;
    }

    public void setSwitch0(String switch0) {
        this.switch0 = switch0.equals("on") ? OnOffType.ON : OnOffType.OFF;
    }

    public OnOffType getSwitch1() {
        return this.switch1;
    }

    public void setSwitch1(String switch1) {
        this.switch1 = switch1.equals("on") ? OnOffType.ON : OnOffType.OFF;
    }

    public OnOffType getSwitch2() {
        return this.switch2;
    }

    public void setSwitch2(String switch2) {
        this.switch2 = switch2.equals("on") ? OnOffType.ON : OnOffType.OFF;
    }

    public OnOffType getSwitch3() {
        return this.switch3;
    }

    public void setSwitch3(String switch3) {
        this.switch3 = switch3.equals("on") ? OnOffType.ON : OnOffType.OFF;
    }

    public QuantityType<Power> getPower() {
        return this.power;
    }

    public void setPower(String power, Float multiplier, int decimalPlaces) {
        this.power = new QuantityType<Power>(round(Float.parseFloat(power) * multiplier, decimalPlaces), WATT);
    }

    public QuantityType<ElectricPotential> getVoltage() {
        return this.voltage;
    }

    public void setVoltage(String voltage, Float multiplier, int decimalPlaces) {
        this.voltage = new QuantityType<ElectricPotential>(round(Float.parseFloat(voltage) * multiplier, decimalPlaces),
                VOLT);
    }

    public QuantityType<ElectricCurrent> getCurrent() {
        return this.current;
    }

    public void setCurrent(String current, Float multiplier, int decimalPlaces) {
        this.current = new QuantityType<ElectricCurrent>(round(Float.parseFloat(current) * multiplier, decimalPlaces),
                AMPERE);
    }

    public QuantityType<ElectricPotential> getBattery() {
        return this.battery;
    }

    public void setBattery(Double battery) {
        this.battery = new QuantityType<ElectricPotential>(battery, VOLT);
    }

    public QuantityType<Dimensionless> getBatteryLevel() {
        return this.batteryLevel;
    }

    public void setBatteryLevel(Double batteryLevel) {
        this.batteryLevel = new QuantityType<Dimensionless>(batteryLevel, PERCENT);
    }

    public QuantityType<Energy> getTodayKwh() {
        return this.todayKwh;
    }

    public void setTodayKwh(Double total, float multiplier, int decimalPlaces) {
        this.todayKwh = new QuantityType<Energy>(round(total * multiplier, decimalPlaces), KILOWATT_HOUR);
    }

    public QuantityType<Energy> getYesterdayKwh() {
        return this.yesterdayKwh;
    }

    public void setYesterdayKwh(Double total, float multiplier, int decimalPlaces) {
        this.yesterdayKwh = new QuantityType<Energy>(round(total * multiplier, decimalPlaces), KILOWATT_HOUR);
    }

    public QuantityType<Energy> getMonthKwh() {
        return this.monthKwh;
    }

    public void setMonthKwh(Double total, float multiplier, int decimalPlaces) {
        this.monthKwh = new QuantityType<Energy>(round(total * multiplier, decimalPlaces), KILOWATT_HOUR);
    }

    public QuantityType<Energy> getSevenKwh() {
        return this.sevenKwh;
    }

    public void setSevenKwh(Double total, float multiplier, int decimalPlaces) {
        this.sevenKwh = new QuantityType<Energy>(round(total * multiplier, decimalPlaces), KILOWATT_HOUR);
    }

    public QuantityType<Energy> getThirtyKwh() {
        return this.thirtyKwh;
    }

    public void setThirtyKwh(Double total, float multiplier, int decimalPlaces) {
        this.thirtyKwh = new QuantityType<Energy>(round(total * multiplier, decimalPlaces), KILOWATT_HOUR);
    }

    public QuantityType<Energy> getHundredKwh() {
        return this.hundredKwh;
    }

    public void setHundredKwh(Double total, float multiplier, int decimalPlaces) {
        this.hundredKwh = new QuantityType<Energy>(round(total * multiplier, decimalPlaces), KILOWATT_HOUR);
    }

    public StringType getSensorType() {
        return this.sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = new StringType(sensorType);
    }

    public QuantityType<Temperature> getTemperature() {
        return this.temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = new QuantityType<Temperature>(temperature, SIUnits.CELSIUS);
    }

    public QuantityType<Dimensionless> getHumidity() {
        return this.humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = new QuantityType<Dimensionless>(humidity, PERCENT);
    }

    public DateTimeType getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = new DateTimeType(lastUpdate);
    }

    public DateTimeType getActionTime() {
        return this.actionTime;
    }

    public void setActionTime(String actionTime) {
        this.actionTime = new DateTimeType(actionTime);
    }

    public PercentType getWhiteBrightness() {
        return this.whiteBrightness;
    }

    public void setWhiteBrightness(Integer whiteBrightness) {
        this.whiteBrightness = new PercentType(whiteBrightness);
    }

    public PercentType getColorBrightness() {
        return this.colorBrightness;
    }

    public void setColorBrightness(Integer colorBrightness) {
        this.colorBrightness = new PercentType(colorBrightness);
    }

    public HSBType getColor() {
        return this.color;
    }

    public void setColor(Integer red, Integer green, Integer blue) {
        HSBType hsb = HSBType.fromRGB(red, green, blue);
        this.color = hsb;
    }

    public PercentType getSpeed() {
        return this.speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = new PercentType(speed);
    }

    public DecimalType getSensitivity() {
        return this.sensitivity;
    }

    public void setSensitivity(Integer sensitivity) {
        this.sensitivity = new DecimalType(sensitivity);
    }

    public DecimalType getMode() {
        return this.mode;
    }

    public void setMode(Integer mode) {
        this.mode = new DecimalType(mode);
    }

    public OnOffType getMusicMode() {
        return this.musicMode;
    }

    public void setMusicMode(String musicMode) {
        this.musicMode = musicMode.equals("on") ? OnOffType.ON : OnOffType.OFF;
        ;
    }

    public PercentType getColorTemperature() {
        return this.colourTemperature;
    }

    public void setColorTemperature(Integer colourTemperature) {
        this.colourTemperature = new PercentType(colourTemperature);
    }

    public StringType getLtype() {
        return this.ltype;
    }

    public void setLtype(String ltype) {
        this.ltype = new StringType(ltype);
    }

    public OnOffType getNetworkLED() {
        return this.networkLED;
    }

    public void setNetworkLED(String networkLED) {
        this.networkLED = networkLED.equals("on") ? OnOffType.ON : OnOffType.OFF;
    }

    public QuantityType<Power> getRssi() {
        return this.rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = new QuantityType<Power>(rssi, DECIBEL_MILLIWATTS);
    }

    public OnOffType getZigbeeLED() {
        return this.zigbeeLED;
    }

    public void setZigbeeLED(String zigbeeLED) {
        this.zigbeeLED = zigbeeLED.equals("on") ? OnOffType.ON : OnOffType.OFF;
    }

    public DateTimeType getRf0() {
        return this.rf0;
    }

    public void setRf0(String rf0) {
        this.rf0 = new DateTimeType(rf0);
    }

    public DateTimeType getRf1() {
        return this.rf1;
    }

    public void setRf1(String rf1) {
        this.rf1 = new DateTimeType(rf1);
    }

    public DateTimeType getRf2() {
        return this.rf2;
    }

    public void setRf2(String rf2) {
        this.rf2 = new DateTimeType(rf2);
    }

    public DateTimeType getRf3() {
        return this.rf3;
    }

    public void setRf3(String rf3) {
        this.rf3 = new DateTimeType(rf3);
    }

    public DateTimeType getRf4() {
        return this.rf4;
    }

    public void setRf4(String rf4) {
        this.rf4 = new DateTimeType(rf4);
    }

    public DateTimeType getRf5() {
        return this.rf5;
    }

    public void setRf5(String rf5) {
        this.rf5 = new DateTimeType(rf5);
    }

    public DateTimeType getRf6() {
        return this.rf6;
    }

    public void setRf6(String rf6) {
        this.rf6 = new DateTimeType(rf6);
    }

    public DateTimeType getRf7() {
        return this.rf7;
    }

    public void setRf7(String rf7) {
        this.rf7 = new DateTimeType(rf7);
    }

    public DateTimeType getRf8() {
        return this.rf8;
    }

    public void setRf8(String rf8) {
        this.rf8 = new DateTimeType(rf8);
    }

    public DateTimeType getRf9() {
        return this.rf9;
    }

    public void setRf9(String rf9) {
        this.rf9 = new DateTimeType(rf9);
    }

    public DateTimeType getRf10() {
        return this.rf10;
    }

    public void setRf10(String rf10) {
        this.rf10 = new DateTimeType(rf10);
    }

    public DateTimeType getRf11() {
        return this.rf11;
    }

    public void setRf11(String rf11) {
        this.rf11 = new DateTimeType(rf11);
    }

    public DateTimeType getRf12() {
        return this.rf12;
    }

    public void setRf12(String rf12) {
        this.rf12 = new DateTimeType(rf12);
    }

    public DateTimeType getRf13() {
        return this.rf13;
    }

    public void setRf13(String rf13) {
        this.rf13 = new DateTimeType(rf13);
    }

    public DateTimeType getRf14() {
        return this.rf14;
    }

    public void setRf14(String rf14) {
        this.rf14 = new DateTimeType(rf14);
    }

    public DateTimeType getRf15() {
        return this.rf15;
    }

    public void setRf15(String rf15) {
        this.rf15 = new DateTimeType(rf15);
    }

    public JsonArray getRfCodeList() {
        return this.rfCodeList;
    }

    public void setRfCodeList(JsonArray rfCodeList) {
        this.rfCodeList = rfCodeList;
    }

    public DateTimeType getTrigTime() {
        return this.trigTime;
    }

    public void setTrigTime(String trigTime) {
        this.trigTime = new DateTimeType(trigTime);
    }

    public OnOffType getMotion() {
        return this.motion;
    }

    public OpenClosedType getMotionContact() {
        return this.motion == OnOffType.ON ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    public void setMotion(Integer motion) {
        this.motion = motion.equals(1) ? OnOffType.ON : OnOffType.OFF;
    }

    public StringType getBrightnessState() {
        return this.brightnessState;
    }

    public void setBrightnessState(String brightnessState) {
        this.brightnessState = new StringType(brightnessState);
    }

    public OpenClosedType getButton(Integer key) {
        switch (key) {
            case 0:
                return this.button0;
            case 1:
                return this.button1;
            case 2:
                return this.button2;
            default:
                logger.warn("Unknown button key value: {}", key);
                return OpenClosedType.CLOSED;
        }
    }

    public void setButton(Integer key, OpenClosedType state) {
        switch (key) {
            case 0:
                this.button0 = state;
                break;
            case 1:
                this.button1 = state;
                break;
            case 2:
                this.button2 = state;
                break;
            default:
                logger.warn("Unknown button key value: {}", key);
        }
    }

    public DateTimeType getButtonTrigTime(Integer key) {
        switch (key) {
            case 0:
                return this.button0TrigTime;
            case 1:
                return this.button1TrigTime;
            case 2:
                return this.button2TrigTime;
            default:
                logger.warn("Unknown button key value: {}", key);
                return new DateTimeType(System.currentTimeMillis() + "");
        }
    }

    public void setButtonTrigTime(Integer key, String trigTime) {
        switch (key) {
            case 0:
                this.button0TrigTime = new DateTimeType(trigTime);
                break;
            case 1:
                this.button1TrigTime = new DateTimeType(trigTime);
                break;
            case 2:
                this.button2TrigTime = new DateTimeType(trigTime);
                break;
            default:
                logger.warn("Unknown button key value: {}", key);
        }
    }

    public void setButtonPress(Integer key, String trigTime) {
        setButton(key, OpenClosedType.OPEN);
        setButtonTrigTime(key, trigTime);
    }

    public void resetButtonPress(Integer key) {
        setButton(key, OpenClosedType.CLOSED);
    }
}
