package com.oneidentity.safeguard.safeguardclient.restclient;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneidentity.safeguard.safeguardclient.data.SafeguardClientListItem;
import com.oneidentity.safeguard.safeguardclient.data.SafeguardClientOperations;
import com.oneidentity.safeguard.safeguardclient.exceptions.RestClientException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class SafeguardClient {

    private RestClient client = null;
    private String server = null;
    private String port = null;
    private String user = null;
    private String password = null;
    private String sessionId = null;
    private Boolean suppressLogging = true;
    private Boolean secure = true;
    
    private static Properties pangaeaClientProperties = null;
    private static final String SERVER = "bnichvm0.sg.lab";
    private static final String PORT = "443";
    private static final String SUPPRESS_LOGGING = "false";
    private static final String SECURE = "true";
    private static final String SESSION_ID = "session_id=";

    private void initPangaeaClient(Properties configProps, String user, String password, String host) {
        pangaeaClientProperties = configProps;
        server = host == null ? SERVER : host;
        try {
            if (server == SERVER) {
                server = pangaeaClientProperties.getProperty("scbnav.interface.server", SERVER);
            }
            port = pangaeaClientProperties.getProperty("scbnav.interface.port", PORT);
            secure = (pangaeaClientProperties.getProperty("scbnav.interface.secure", SECURE).equalsIgnoreCase("false")) ? false : true;
            suppressLogging = (pangaeaClientProperties.getProperty("scbnav.interface.suppress.logging", SUPPRESS_LOGGING).equalsIgnoreCase("false")) ? false : true;
        } catch (NullPointerException ex) {
            port = PORT;
            secure = Boolean.parseBoolean(SECURE);
            suppressLogging = Boolean.parseBoolean(SUPPRESS_LOGGING);
        }
        
        this.user = user;
        this.password = password;
//        this.client = new RestClient(server+":"+port+"/", secure, user, password, suppressLogging, false);
    }
    
    private Map<String, Object> extractMetaData(String rawJSON) {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = new HashMap<>();
        try {
            //Convert Map to JSON
            map = mapper.readValue(rawJSON, new TypeReference<Map<String, Object>>() {
            });

            //Print JSON output
            System.out.println(map);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return (Map<String,Object>) map.get("meta");
    }
    
    public SafeguardClient(Properties configProps, String user, String password, String host) {
        initPangaeaClient(configProps, user, password, host);
    }
    
//    
//    public String encodeValue(String value) {
//        return client.encodeValue(value);
//    }
    
    public boolean PangaeaClientLogin () 
            throws RestClientException {
        
        Response resp = client.execGET("api/authentication");
        if (resp.getStatus() != 200) {
            client.checkForUnexpectedExceptions(resp);
            return false;
        }

        resp.bufferEntity();
//        SCBNavListEndpoints items = resp.getEntity(SCBNavListEndpoints.class);
//        
//        try {
//            resp.getEntityInputStream().reset();
//            String rawJSON = resp.getEntity(String.class);
//            items.getMeta().setMeta(this.extractMetaData(rawJSON));
//        } catch (IOException e) {
//        }

        MultivaluedMap<String,String> headers = resp.getStringHeaders();
        
        String cookieStr = headers.getFirst("Set-Cookie");
        if (cookieStr != null) {
            this.client.setSessionId(cookieStr);
        }
        
        return true;
    }

//    public SCBNavMetadata getSCBAPIs() 
//            throws RestClientException {
//        
//        ClientResponse resp = client.execGET("api", null);
//        if (resp.getStatus() != 200) {
//            client.checkForUnexpectedExceptions(resp);
//            return null;
//        }
//
//        resp.bufferEntity();
//        SCBNavMetadata item = resp.getEntity(SCBNavMetadata.class);
//        
//        try {
//            resp.getEntityInputStream().reset();
//            String rawJSON = resp.getEntity(String.class);
//            item.setRawJSON(rawJSON);
//            item.setMeta(this.extractMetaData(rawJSON));
//        } catch (IOException e) {
//        }
//        
//        return item;
//    }

    public SafeguardClientListItem executeCommand(SafeguardClientOperations operation, String uri, String payload) 
            throws RestClientException {
        
        Response resp = null;
        switch (operation) {
            case GET:
                resp = client.execGET(uri);
                break;
            case POST:
                if (payload == null || payload.trim().isEmpty()) {
                    payload = "{}";
                }
                resp = client.execPOST(uri, null, payload);
                break;
            case PUT:
                if (payload == null || payload.trim().isEmpty()) {
                    payload = "{}";
                }
                resp = client.execPUT(uri, null, payload);
                break;
            case DELETE:
                resp = client.execDELETE(uri, null);
                break;
            default:
                return null;
        }
        
        if (resp.getStatus() != 200 && resp.getStatus() != 201) {
            client.checkForUnexpectedExceptions(resp);
            return null;
        }

        resp.bufferEntity();
        SafeguardClientListItem items = resp.readEntity(SafeguardClientListItem.class);
        
        resp.readEntity(InputStream.class);
        String rawJSON = resp.readEntity(String.class);
//            items.getMeta().setRawJSON(rawJSON);
//            items.getMeta().setMeta(this.extractMetaData(rawJSON));
        
        return items;
    }
    
}
