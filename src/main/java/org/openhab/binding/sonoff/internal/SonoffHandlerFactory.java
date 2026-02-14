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

// Purpose:Setting a handlers for specific devices

package org.openhab.binding.sonoff.internal;

import static org.openhab.binding.sonoff.internal.SonoffBindingConstants.*;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.sonoff.internal.handler.*;
import org.openhab.binding.sonoff.internal.handler.SonoffAccountHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffRfBridgeHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffRfDeviceHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffZigbeeBridgeHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffZigbeeButtonHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffZigbeeDeviceMotionSensorHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffZigbeeDeviceTemperatureHumiditySensorHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The {@link sonoffHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.sonoff", service = ThingHandlerFactory.class)

public class SonoffHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(SonoffHandlerFactory.class);
    private final WebSocketClient websocketClient;
    private final HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Activate
    public SonoffHandlerFactory(final @Reference WebSocketFactory webSocketFactory,
            final @Reference HttpClientFactory httpClientFactory, BundleContext bundleContext) {
        this.websocketClient = webSocketFactory.getCommonWebSocketClient();
        this.httpClient = httpClientFactory.getCommonHttpClient();

        // Log binding version on activation
        String customVersion = readVersionFromXml();
        String bundleVersion = bundleContext.getBundle().getVersion().toString();
        logger.info("Sonoff (eWeLink) Binding: version {} (Maven: {})", customVersion, bundleVersion);
    }

    /**
     * Reads the custom version from version.xml resource file
     *
     * @return the version string, or "unknown" if unable to read
     */
    private String readVersionFromXml() {
        try (InputStream input = getClass().getResourceAsStream("/version.xml")) {
            if (input == null) {
                logger.warn("version.xml not found in resources");
                return "unknown";
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(input);

            NodeList customNodes = doc.getElementsByTagName("custom");
            if (customNodes.getLength() > 0) {
                Element customElement = (Element) customNodes.item(0);
                return customElement.getTextContent().trim();
            }

            logger.warn("No <custom> element found in version.xml");
            return "unknown";
        } catch (Exception e) {
            logger.warn("Failed to read version from version.xml: {}", e.getMessage());
            return "unknown";
        }
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        String id = thing.getThingTypeUID().getId();
        switch (id) {
            case "account":
                return new SonoffAccountHandler((Bridge) thing, websocketClient, httpClient);
            case "1":
            case "6":
            case "14":
            case "27":
            case "81":
            case "107":
            case "160":
            case "209":
            case "256": // CAM not supported yet
            case "260": // CAM not supported yet
                return new SonoffSwitchSingleHandler(thing);
            case "2":
            case "3":
            case "4":
            case "7":
            case "8":
            case "9":
            case "29":
            case "30":
            case "31":
            case "77":
            case "78":
            case "82":
            case "83":
            case "84":
            case "126":
            case "161":
            case "162":
            case "210":
            case "211":
            case "212":
            case "264":
            case "268":
            case "275":
                return new SonoffSwitchMultiHandler(thing);
            case "5":
                return new SonoffSwitchPOWHandler(thing);
            case "15":
            case "181":
                return new SonoffSwitchTHHandler(thing);
            case "24":
                return new SonoffGSMSocketHandler(thing);
            case "28":
                return new SonoffRfBridgeHandler((Bridge) thing);
            case "32":
                return new SonoffSwitchPOWR2Handler(thing);
            case "226":
                return new SonoffSwitchUUID226Handler(thing);
            case "59":
                return new SonoffRGBStripHandler(thing);
            case "173":
                return new SonoffRGBICStripHandler(thing);
            case "66":
            case "168":
            case "243":
                return new SonoffZigbeeBridgeHandler((Bridge) thing);
            case "102":
                return new SonoffMagneticSwitchHandler(thing);
            case "104":
                return new SonoffRGBCCTHandler(thing);
            case "138":
                return new SonoffSwitchSingleMiniHandler(thing);
            case "190":
                return new SonoffSwitchPOWUgradedHandler(thing);
            case "237":
                return new SonoffGateHandler(thing);
            case "258":
                return new SonoffRollerShutterHandler(thing);
            case "265":
                return new SonoffButtonHandler(thing);
            case "1770":
            case "7014": // SNZB-02P
            case "7038": // SNZB-02DR2
                return new SonoffZigbeeDeviceTemperatureHumiditySensorHandler(thing);
            case "2026":
                return new SonoffZigbeeDeviceMotionSensorHandler(thing);
            case "rfremote1":
            case "rfremote2":
            case "rfremote3":
            case "rfremote4":
            case "rfsensor":
                return new SonoffRfDeviceHandler(thing);
            case "7000":
                return new SonoffZigbeeButtonHandler(thing);
            case "7002":
                return new SonoffZigbeeDeviceMotionSensorHandler(thing);
            case "7003":
                return new SonoffZigbeeContactSensorHandler(thing);
            case "7010":
                return new SonoffZigbeeSwitchSingleHandler(thing);
            case "7029":
            case "7040":
                return new SonoffZigbeeSwitchMultiHandler(thing);
            default:
                return null;
        }
    }
}
