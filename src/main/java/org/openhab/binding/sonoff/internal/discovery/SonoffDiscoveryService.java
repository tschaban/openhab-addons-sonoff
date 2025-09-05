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
package org.openhab.binding.sonoff.internal.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.SonoffBindingConstants;
import org.openhab.binding.sonoff.internal.SonoffCacheProvider;
import org.openhab.binding.sonoff.internal.connection.SonoffApiConnection;
import org.openhab.binding.sonoff.internal.connection.SonoffConnectionManager;
import org.openhab.binding.sonoff.internal.handler.*;
import org.openhab.binding.sonoff.internal.handler.SonoffRfBridgeHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffZigbeeBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link SonoffDiscoveryService} Allows Discovery of Ewelink devices
 *
 * @author David Murton - Initial contribution
 */

@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.sonoff")
public class SonoffDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    // , DiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(SonoffDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private @Nullable SonoffAccountHandler account;
    private @Nullable ScheduledFuture<?> scanTask;
    private final Gson gson;

    public SonoffDiscoveryService() {
        super(SonoffBindingConstants.DISCOVERABLE_THING_TYPE_UIDS, DISCOVER_TIMEOUT_SECONDS, false);
        this.gson = new Gson();
    }

    @Override
    protected void activate(@Nullable Map<String, Object> configProperties) {
    }

    @Override
    public void deactivate() {
    }

    @Override
    protected void startScan() {
        logger.debug("Start Scan");
        final ScheduledFuture<?> scanTask = this.scanTask;
        if (scanTask != null) {
            scanTask.cancel(true);
        }
        this.scanTask = scheduler.schedule(() -> {
            try {
                discover();
            } catch (Exception e) {
            }
        }, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop Scan");
        super.stopScan();
        final ScheduledFuture<?> scanTask = this.scanTask;
        if (scanTask != null) {
            scanTask.cancel(true);
            this.scanTask = null;
        }
    }

    // Used for discovery
    public List<JsonObject> createCache(List<Thing> things) {
        SonoffCacheProvider cacheProvider = new SonoffCacheProvider();
        List<JsonObject> devices = new ArrayList<JsonObject>();

        final SonoffAccountHandler account = this.account;
        if (account != null) {
            SonoffConnectionManager connectionManager = account.getConnectionManager();
            final String mode = connectionManager.getMode();
            final SonoffApiConnection api = account.getConnectionManager().getApi();
            // If we are in local mode connect to the api
            if (mode.equals("local")) {
                api.login();
            }
            String response = "";
            try {
                response = api.createCache();
            } catch (Exception e) {
                logger.debug("Creating the device cache threw an error {}", e.getMessage());
            }
            JsonObject main = gson.fromJson(response, JsonObject.class);
            if (main != null) {
                JsonElement dataElement = main.get("data");
                if (dataElement == null || !dataElement.isJsonObject()) {
                    logger.debug("No data field found in response or data is not an object");
                    return devices;
                }
                JsonObject data = dataElement.getAsJsonObject();
                JsonElement thingListElement = data.get("thingList");
                if (thingListElement == null || !thingListElement.isJsonArray()) {
                    logger.debug("No thingList field found in data or thingList is not an array");
                    return devices;
                }
                JsonArray thingList = thingListElement.getAsJsonArray();
                for (int i = 0; i < thingList.size(); i++) {
                    // Items (type 1)
                    JsonElement thingElement = thingList.get(i);
                    if (thingElement == null || !thingElement.isJsonObject()) {
                        continue;
                    }
                    JsonObject thingObject = thingElement.getAsJsonObject();
                    JsonElement type = thingObject.get("itemType");
                    if (type != null && !type.isJsonNull()) {
                        try {
                            if (type.getAsInt() == 1) {
                                JsonElement itemDataElement = thingObject.get("itemData");
                                if (itemDataElement == null || !itemDataElement.isJsonObject()) {
                                    continue;
                                }
                                JsonObject device = itemDataElement.getAsJsonObject();
                                JsonElement deviceIdElement = device.get("deviceid");
                                if (deviceIdElement == null || deviceIdElement.isJsonNull()) {
                                    logger.debug("Device missing deviceid, skipping");
                                    continue;
                                }
                                String deviceid = deviceIdElement.getAsString();
                            logger.debug("Processing device {}", deviceid);
                            if (!cacheProvider.checkFile(deviceid)) {
                                cacheProvider.newFile(deviceid, gson.toJson(device));
                                account.addState(deviceid);
                                logger.debug("Cache file and state created for device {} as it was missing", deviceid);

                                for (int m = 0; m < things.size(); m++) {
                                    Thing thing = things.get(m);
                                    if (thing.getConfiguration() != null) {
                                        Object configValue = thing.getConfiguration().get("deviceid");
                                        if (configValue != null) {
                                            String config = configValue.toString();
                                            if (config.equals(deviceid)) {
                                                logger.info("Re-Initializing {} as a thing was already present", deviceid);
                                                if (thing.getHandler() != null) {
                                                    thing.getHandler().thingUpdated(thing);
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                            devices.add(device);
                            }
                        } catch (Exception e) {
                            logger.debug("Error processing device item: {}", e.getMessage());
                        }
                    }
                }
            }
        }
        return devices;
    }

    private void discover() {
        logger.debug("Sonoff - Start Discovery");
        // Get the master Bridge and create the cache
        final SonoffAccountHandler account = this.account;
        if (account != null) {
            new ArrayList<JsonObject>();
            ThingUID bridgeUID = account.getThing().getUID();
            int i = 0;

            // Get a list of child things so we can add sub devices and reinitialise if required
            List<Thing> things = account.getThing().getThings();

            List<JsonObject> devices = createCache(things);

            // Create Top Level Devices
            for (i = 0; i < devices.size(); i++) {
                try {
                    JsonObject device = devices.get(i);
                    if (device == null) {
                        continue;
                    }
                    
                    // Safe deviceid extraction
                    JsonElement deviceIdElement = device.get("deviceid");
                    if (deviceIdElement == null || deviceIdElement.isJsonNull()) {
                        logger.debug("Device missing deviceid, skipping");
                        continue;
                    }
                    String deviceid = deviceIdElement.getAsString();
                    
                    // Safe uiid extraction
                    JsonElement extraElement = device.get("extra");
                    if (extraElement == null || !extraElement.isJsonObject()) {
                        logger.debug("Device {} missing extra object, skipping", deviceid);
                        continue;
                    }
                    JsonObject extra = extraElement.getAsJsonObject();
                    JsonElement uiidElement = extra.get("uiid");
                    if (uiidElement == null || uiidElement.isJsonNull()) {
                        logger.debug("Device {} missing uiid, skipping", deviceid);
                        continue;
                    }
                    Integer uiid = uiidElement.getAsInt();
                    
                    // Safe params extraction
                    JsonElement paramsElement = device.get("params");
                    JsonObject params = (paramsElement != null && paramsElement.isJsonObject()) 
                        ? paramsElement.getAsJsonObject() : new JsonObject();

                    logger.debug("Discovered device {}", deviceid);
                    ThingTypeUID thingTypeUid = SonoffBindingConstants.createMap().get(uiid);
                    if (thingTypeUid != null) {
                        ThingUID deviceThing = new ThingUID(thingTypeUid, account.getThing().getUID(), deviceid);
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("deviceid", deviceid);
                        
                        // Safe property extraction with defaults
                        JsonElement nameElement = device.get("name");
                        String name = (nameElement != null && !nameElement.isJsonNull()) 
                            ? nameElement.getAsString() : "Unknown Device";
                        properties.put("Name", name);
                        
                        JsonElement brandElement = device.get("brandName");
                        String brand = (brandElement != null && !brandElement.isJsonNull()) 
                            ? brandElement.getAsString() : "Unknown";
                        properties.put("Brand", brand);
                        
                        JsonElement modelElement = device.get("productModel");
                        String model = (modelElement != null && !modelElement.isJsonNull()) 
                            ? modelElement.getAsString() : "Unknown";
                        properties.put("Model", model);
                        
                        JsonElement fwVersionElement = params.get("fwVersion");
                        if (fwVersionElement != null && !fwVersionElement.isJsonNull()) {
                            properties.put("FW Version", fwVersionElement.getAsString());
                        }
                        
                        properties.put("Device ID", deviceid);
                        
                        JsonElement deviceKeyElement = device.get("devicekey");
                        String deviceKey = (deviceKeyElement != null && !deviceKeyElement.isJsonNull()) 
                            ? deviceKeyElement.getAsString() : "";
                        properties.put("Device Key", deviceKey);
                        
                        properties.put("UIID", uiid);
                        
                        JsonElement apiKeyElement = device.get("apikey");
                        String apiKey = (apiKeyElement != null && !apiKeyElement.isJsonNull()) 
                            ? apiKeyElement.getAsString() : "";
                        properties.put("API Key", apiKey);
                        
                        JsonElement ssidElement = params.get("ssid");
                        if (ssidElement != null && !ssidElement.isJsonNull()) {
                            properties.put("Connected To SSID", ssidElement.getAsString());
                        }
                        
                        String label = name;
                        thingDiscovered(
                                DiscoveryResultBuilder.create(deviceThing).withLabel(label).withProperties(properties)
                                        .withRepresentationProperty("deviceid").withBridge(bridgeUID).build());
                    } else {
                        Boolean subDevice = false;
                        subDevice = SonoffBindingConstants.createZigbeeMap().get(uiid) != null ? true : subDevice;
                        subDevice = SonoffBindingConstants.createSensorMap().get(uiid) != null ? true : subDevice;
                        if (!subDevice) {
                            logger.error(
                                    "Unable to add {} as its not supported, please forward the cache file to the developer",
                                    deviceid);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Error processing device during discovery: {}", e.getMessage());
                }
            }

            // Create Child Devices
            int j = 0;
            for (i = 0; i < things.size(); i++) {
                String uiid = things.get(i).getThingTypeUID().getId();
                switch (uiid) {
                    // RF Devices
                    case "28":
                        try {
                            SonoffRfBridgeHandler rfBridge = (SonoffRfBridgeHandler) account.getThing().getThings().get(i)
                                    .getHandler();
                            if (rfBridge != null) {
                                JsonArray subDevices = rfBridge.getSubDevices();
                                if (subDevices != null) {
                                    logger.debug("Found {} rf device/s", subDevices.size());
                                    for (j = 0; j < subDevices.size(); j++) {
                                        try {
                                            JsonElement subDeviceElement = subDevices.get(j);
                                            if (subDeviceElement == null || !subDeviceElement.isJsonObject()) {
                                                continue;
                                            }
                                            JsonObject subDevice = subDeviceElement.getAsJsonObject();
                                            
                                            JsonElement remoteTypeElement = subDevice.get("remote_type");
                                            if (remoteTypeElement == null || remoteTypeElement.isJsonNull()) {
                                                continue;
                                            }
                                            Integer type = Integer.parseInt(remoteTypeElement.getAsString());
                                            
                                            ThingTypeUID thingTypeUid = SonoffBindingConstants.createSensorMap().get(type);
                                            if (thingTypeUid != null) {
                                                ThingUID rfThing = new ThingUID(thingTypeUid, rfBridge.getThing().getUID(), j + "");
                                                Map<String, Object> properties = new HashMap<>();
                                                properties.put("deviceid", j + "");
                                                
                                                JsonElement nameElement = subDevice.get("name");
                                                String name = (nameElement != null && !nameElement.isJsonNull()) 
                                                    ? nameElement.getAsString() : "RF Device " + j;
                                                properties.put("Name", name);
                                                
                                                String rfLabel = name;
                                                thingDiscovered(DiscoveryResultBuilder.create(rfThing).withLabel(rfLabel)
                                                        .withProperties(properties).withRepresentationProperty("deviceid")
                                                        .withBridge(rfBridge.getThing().getUID()).build());
                                            }
                                        } catch (Exception e) {
                                            logger.debug("Error processing RF sub-device {}: {}", j, e.getMessage());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Error processing RF bridge: {}", e.getMessage());
                        }
                        break;
                    // Zigbee Devices
                    case "66":
                    case "168":
                    case "243":
                        try {
                            SonoffZigbeeBridgeHandler zigbeeBridge = (SonoffZigbeeBridgeHandler) account.getThing()
                                    .getThings().get(i).getHandler();
                            if (zigbeeBridge != null) {
                                JsonArray subDevices = zigbeeBridge.getSubDevices();
                                if (subDevices != null) {
                                    logger.debug("Found {} zigbee device/s", subDevices.size());
                                    for (j = 0; j < subDevices.size(); j++) {
                                        try {
                                            JsonElement subDeviceElement = subDevices.get(j);
                                            if (subDeviceElement == null || !subDeviceElement.isJsonObject()) {
                                                continue;
                                            }
                                            JsonObject subDevice = subDeviceElement.getAsJsonObject();
                                            
                                            JsonElement deviceIdElement = subDevice.get("deviceid");
                                            if (deviceIdElement == null || deviceIdElement.isJsonNull()) {
                                                continue;
                                            }
                                            String subDeviceid = deviceIdElement.getAsString();
                                            
                                            JsonElement uiidElement = subDevice.get("uiid");
                                            if (uiidElement == null || uiidElement.isJsonNull()) {
                                                continue;
                                            }
                                            Integer subDeviceuiid = uiidElement.getAsInt();
                                            
                                            // Lookup our device in the main list
                                            for (int k = 0; k < devices.size(); k++) {
                                                try {
                                                    JsonObject mainDevice = devices.get(k);
                                                    if (mainDevice == null) {
                                                        continue;
                                                    }
                                                    JsonElement mainDeviceIdElement = mainDevice.get("deviceid");
                                                    if (mainDeviceIdElement == null || mainDeviceIdElement.isJsonNull()) {
                                                        continue;
                                                    }
                                                    if (mainDeviceIdElement.getAsString().equals(subDeviceid)) {
                                                        subDevice = mainDevice;
                                                        
                                                        JsonElement paramsElement = subDevice.get("params");
                                                        JsonObject subParams = (paramsElement != null && paramsElement.isJsonObject()) 
                                                            ? paramsElement.getAsJsonObject() : new JsonObject();
                                                        
                                                        ThingTypeUID thingTypeUid = SonoffBindingConstants.createZigbeeMap()
                                                                .get(subDeviceuiid);
                                                        if (thingTypeUid != null) {
                                                            ThingUID zigbeeThing = new ThingUID(thingTypeUid,
                                                                    zigbeeBridge.getThing().getUID(), subDeviceid);
                                                            Map<String, Object> properties = new HashMap<>();
                                                            properties.put("deviceid", subDeviceid);
                                                            
                                                            JsonElement nameElement = subDevice.get("name");
                                                            String name = (nameElement != null && !nameElement.isJsonNull()) 
                                                                ? nameElement.getAsString() : "Zigbee Device";
                                                            properties.put("Name", name);
                                                            
                                                            JsonElement brandElement = subDevice.get("brandName");
                                                            String brand = (brandElement != null && !brandElement.isJsonNull()) 
                                                                ? brandElement.getAsString() : "Unknown";
                                                            properties.put("Brand", brand);
                                                            
                                                            JsonElement modelElement = subDevice.get("productModel");
                                                            String model = (modelElement != null && !modelElement.isJsonNull()) 
                                                                ? modelElement.getAsString() : "Unknown";
                                                            properties.put("Model", model);
                                                            
                                                            JsonElement fwVersionElement = subParams.get("fwVersion");
                                                            if (fwVersionElement != null && !fwVersionElement.isJsonNull()) {
                                                                properties.put("FW Version", fwVersionElement.getAsString());
                                                            }
                                                            
                                                            JsonElement deviceKeyElement = subDevice.get("devicekey");
                                                            String deviceKey = (deviceKeyElement != null && !deviceKeyElement.isJsonNull()) 
                                                                ? deviceKeyElement.getAsString() : "";
                                                            properties.put("Device Key", deviceKey);
                                                            
                                                            properties.put("UIID", subDeviceuiid);
                                                            
                                                            JsonElement apiKeyElement = subDevice.get("apikey");
                                                            String apiKey = (apiKeyElement != null && !apiKeyElement.isJsonNull()) 
                                                                ? apiKeyElement.getAsString() : "";
                                                            properties.put("API Key", apiKey);
                                                            
                                                            String label = name;
                                                            thingDiscovered(DiscoveryResultBuilder.create(zigbeeThing).withLabel(label)
                                                                    .withProperties(properties).withRepresentationProperty("deviceid")
                                                                    .withBridge(zigbeeBridge.getThing().getUID()).build());
                                                        }
                                                        break;
                                                    }
                                                } catch (Exception e) {
                                                    logger.debug("Error processing main device {}: {}", k, e.getMessage());
                                                }
                                            }
                                        } catch (Exception e) {
                                            logger.debug("Error processing Zigbee sub-device {}: {}", j, e.getMessage());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Error processing Zigbee bridge: {}", e.getMessage());
                        }
                }
            }
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SonoffAccountHandler) {
            account = (SonoffAccountHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return account;
    }
}
