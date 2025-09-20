package com.example;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.util.Map;

public class ApacheXmlRpcClient implements RpcClient {

    private final String endpointUrl;

    public ApacheXmlRpcClient(String endpointUrl) {
        this.endpointUrl = endpointUrl.endsWith("/RPC2") ? endpointUrl : endpointUrl + "/RPC2";
    }

    @Override
    public Result sendValues(Map<String, Object> payload) throws Exception {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(endpointUrl));
        config.setEnabledForExtensions(false);
        config.setConnectionTimeout(5_000);
        config.setReplyTimeout(5_000);

        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        // Call the Python function 'receive' with one parameter: the struct (map)
        Object response = client.execute("receive", new Object[]{payload});
        String message = response != null ? response.toString() : "OK";
        return Result.ok(message);
    }
}