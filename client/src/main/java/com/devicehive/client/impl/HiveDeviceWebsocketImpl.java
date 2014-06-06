package com.devicehive.client.impl;


import com.devicehive.client.HiveMessageHandler;
import com.devicehive.client.impl.context.RestAgent;
import com.devicehive.client.impl.context.WebsocketAgent;
import com.devicehive.client.impl.json.GsonFactory;
import com.devicehive.client.impl.util.Messages;
import com.devicehive.client.model.Device;
import com.devicehive.client.model.DeviceCommand;
import com.devicehive.client.model.DeviceNotification;
import com.devicehive.client.model.SubscriptionFilter;
import com.devicehive.client.model.exceptions.HiveClientException;
import com.devicehive.client.model.exceptions.HiveException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

import javax.ws.rs.HttpMethod;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.devicehive.client.impl.json.strategies.JsonPolicyDef.Policy.COMMAND_UPDATE_FROM_DEVICE;
import static com.devicehive.client.impl.json.strategies.JsonPolicyDef.Policy.DEVICE_PUBLISHED_DEVICE_AUTH;
import static com.devicehive.client.impl.json.strategies.JsonPolicyDef.Policy.NOTIFICATION_FROM_DEVICE;
import static com.devicehive.client.impl.json.strategies.JsonPolicyDef.Policy.NOTIFICATION_TO_DEVICE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class HiveDeviceWebsocketImpl extends HiveDeviceRestImpl {

    private WebsocketAgent websocketAgent;

    public HiveDeviceWebsocketImpl(WebsocketAgent websocketAgent) {
        super(websocketAgent);
        this.websocketAgent = websocketAgent;
    }

    @Override
    public Device getDevice() throws HiveException {
        JsonObject request = new JsonObject();
        request.addProperty("action", "device/get");
        return websocketAgent.getWebsocketConnector().sendMessage(request, "device", Device.class,
                DEVICE_PUBLISHED_DEVICE_AUTH);
    }

    @Override
    public void registerDevice(Device device) throws HiveException {
        if (device == null) {
            throw new HiveClientException(
                    String.format(Messages.PARAMETER_IS_NULL, "device"), BAD_REQUEST.getStatusCode());
        }
        JsonObject request = new JsonObject();
        request.addProperty("action", "device/save");
        Gson gson = GsonFactory.createGson();
        request.add("device", gson.toJsonTree(device));
        if (websocketAgent.getHivePrincipal() != null) {
            Pair<String, String> authenticated = websocketAgent.getHivePrincipal().getDevice();
            request.addProperty("deviceId", authenticated.getLeft());
            request.addProperty("deviceKey", authenticated.getRight());
        } else {
            request.addProperty("deviceId", device.getId());
            request.addProperty("deviceKey", device.getKey());
        }
        websocketAgent.getWebsocketConnector().sendMessage(request);
    }

    @Override
    public DeviceCommand getCommand(long commandId) throws HiveException {
        Pair<String, String> authenticated = websocketAgent.getHivePrincipal().getDevice();
        String path = "/device/" + authenticated.getKey() + "/command/" + commandId;
        return websocketAgent.getRestConnector().executeWithConnectionCheck(path, HttpMethod.GET, null,
                DeviceCommand.class, null);
    }

    @Override
    public void updateCommand(DeviceCommand deviceCommand) throws HiveException {
        if (deviceCommand == null){
            throw new HiveClientException(String.format(Messages.PARAMETER_IS_NULL, "deviceCommand"),
                    BAD_REQUEST.getStatusCode());
        }
        JsonObject request = new JsonObject();
        request.addProperty("action", "command/update");
        String requestId = UUID.randomUUID().toString();
        request.addProperty("requestId", requestId);
        request.addProperty("commandId", deviceCommand.getId());
        Gson gson = GsonFactory.createGson(COMMAND_UPDATE_FROM_DEVICE);
        request.add("command", gson.toJsonTree(deviceCommand));
        websocketAgent.getWebsocketConnector().sendMessage(request);
    }

    @Override
    public void subscribeForCommands(final Timestamp timestamp, HiveMessageHandler<DeviceCommand> commandHandler)
            throws
            HiveException {
        if (commandHandler == null){
            throw new HiveClientException(String.format(Messages.PARAMETER_IS_NULL, "commandsHandler"),
                    BAD_REQUEST.getStatusCode()) ;
        }
        Set<String> uuids = new HashSet<>();
        uuids.add(websocketAgent.getHivePrincipal().getDevice().getLeft());
        SubscriptionFilter filter =
                new SubscriptionFilter(uuids, null, timestamp);
        websocketAgent.addCommandsSubscription(filter, commandHandler);
    }

    /**
     * Unsubscribes the device from commands.
     */
    @Override
    public void unsubscribeFromCommands() throws HiveException {
        websocketAgent.removeCommandsSubscription();
    }

    @Override
    public DeviceNotification insertNotification(DeviceNotification deviceNotification) throws HiveException {
        if (deviceNotification == null){
            throw new HiveClientException(String.format(Messages.PARAMETER_IS_NULL, "deviceNotification"),
                    BAD_REQUEST.getStatusCode()) ;
        }
        JsonObject request = new JsonObject();
        request.addProperty("action", "notification/insert");
        Gson gson = GsonFactory.createGson(NOTIFICATION_FROM_DEVICE);
        request.add("notification", gson.toJsonTree(deviceNotification));
        return websocketAgent.getWebsocketConnector().sendMessage(request, "notification", DeviceNotification.class,
                NOTIFICATION_TO_DEVICE);
    }


}
