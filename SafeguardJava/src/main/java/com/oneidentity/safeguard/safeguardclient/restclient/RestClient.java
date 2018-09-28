package com.oneidentity.safeguard.safeguardclient.restclient;

import com.oneidentity.safeguard.safeguardclient.exceptions.RestClientException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;

import org.glassfish.jersey.logging.LoggingFeature;

public class RestClient {

    private ClientConfig config = null;
    private Client client = null;
    private String SERVERURL = null;
//    private ClientFilter cf = null;
    private String sessionId = null;
    
    Logger logger = Logger.getLogger(getClass().getName());

    public RestClient(String connectionAddr) {

        TrustManager[] trustAllCerts = null;
        SSLContext sslContext = null;
        
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() 
            {
                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException{}
                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException{}
                    @Override
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return new X509Certificate[0];
                    }

            }}, new java.security.SecureRandom());            
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            Logger.getLogger(RestClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        HostnameVerifier allowAll = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        
        SERVERURL = connectionAddr;
//        config = new DefaultClientConfig();
//        
//        if (trustAllCerts != null &&  sc != null) {
//            config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(
//                    new HostnameVerifier() {
//                @Override
//                public boolean verify(String s, SSLSession sslSession) {
//                    return true;
//                }
//            }, sc));
//        }
        
//        config.getClasses().add(contextResolver);
//        if (chunkedEncoding) {
//            config.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, 32*1024);
//        }
        
        client = ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .hostnameVerifier(allowAll)
//                .register(new SimpleLoggingFilter());
                .register(new LoggingFeature(logger, Level.FINE, null, null)).build();
        
//        client = ClientBuilder.newClient(new ClientConfig().register(new LoggingFeature(logger, Level.ALL, null, null)));

    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }



    /**************************************/
    /*    REST Helper functions           */
    /**************************************/

    public String encodeValue(String value) {
        try {
            value = URLEncoder.encode(value, "UTF-8");
            value = value.replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {}
        return value;
    }
    
//    public MultivaluedMap<String,String> createMVMap(String key, String value) {
//        
//        MultivaluedMap<String,String> mvm = new MultivaluedMapImpl();
//        if (key != null) {
//            mvm.add(key, encodeValue(value));
//        }
//        return mvm;
//    }

    private URI getBaseURI() {
        return UriBuilder.fromUri(SERVERURL).build();
    }

    public String getBaseURL() {
        return SERVERURL;
    }
    
    public void checkForUnexpectedExceptions(Response resp) 
            throws RestClientException {

        Integer status = resp.getStatus();
    	if (status >= 400) {
            try {
                    resp.readEntity(InputStream.class).reset();
                }
                catch (IOException e) {
            }

            String msg = resp.readEntity(String.class);
            throw new RestClientException(msg, status);
        }
    }

    private Long getLongFromResponse(Response response) {
        String str = response.readEntity(String.class);
        return Long.parseLong(str);
    }

    private Integer getIntegerFromResponse(Response response) {
        String str = response.readEntity(String.class);
        return Integer.parseInt(str);
    }

    public Response execGET(String path, Map<String,String> queryParams, Map<String,String> headers) {

        WebTarget service = client.target(getBaseURI()).path(path);
        
        if (queryParams != null) {
            for (Map.Entry<String,String> entry : queryParams.entrySet()) {
                service = service.queryParam(entry.getKey(), entry.getValue());   
            }
        }

        Builder requestBuilder = service.request(MediaType.APPLICATION_JSON);
        
        if (headers != null) {
            for (Map.Entry<String,String> entry : headers.entrySet()) {
                requestBuilder = requestBuilder.header(entry.getKey(), entry.getValue());   
            }
        }
        
        Response r = requestBuilder.get(Response.class);
        return r;
    }
    
    public Response execGET(String path) {

        WebTarget service = client.target(path);
        Response r = service.request(MediaType.APPLICATION_JSON).get(Response.class);

        return r;
    }
    
    public Response execGET(String path,
            Map<String,String> queryParams, String mediaType) {

        WebTarget service = client.target(getBaseURI()).path(path);
        
        if (queryParams != null) {
            for (Map.Entry<String,String> entry : queryParams.entrySet()) {
                service = service.queryParam(entry.getKey(), entry.getValue());   
            }
        }
        
        Response r = service.request(mediaType).get(Response.class);

        return r;
    }

    public Response execPUT(String path, Map<String,String> queryParams, Object requestEntity) {

        WebTarget service = client.target(getBaseURI()).path(path);
        
        if (queryParams != null) {
            for (Map.Entry<String,String> entry : queryParams.entrySet()) {
                service = service.queryParam(entry.getKey(), entry.getValue());   
            }
        }

        Response r = service.request(MediaType.APPLICATION_JSON).put(Entity.entity(requestEntity, MediaType.APPLICATION_JSON), Response.class);

        return r;
    }

    public Response execPOST(String path, Map<String,String> queryParams, Object requestEntity) {

        WebTarget service = client.target(getBaseURI()).path(path);
        
        if (queryParams != null) {
            for (Map.Entry<String,String> entry : queryParams.entrySet()) {
                service = service.queryParam(entry.getKey(), entry.getValue());   
            }
        }

        String e = requestEntity.toString();
        Response r = service.request(MediaType.APPLICATION_JSON).post(Entity.json(requestEntity.toString()), Response.class);
        
        return r;
    }
    
    public Response execPOSTFile(String path, String fileName, Map<String,String> queryParams, Object requestEntity) {

        String contentDisp = "attachment; filename=\"" + fileName + "\"";
        WebTarget service = client.target(getBaseURI()).path(path);
        
        if (queryParams == null) {
            for (Map.Entry<String,String> entry : queryParams.entrySet()) {
                service = service.queryParam(entry.getKey(), entry.getValue());   
            }
        }
        
        Response r = service.request(MediaType.APPLICATION_JSON).header("Content-Disposition", contentDisp).post(Entity.entity(requestEntity, MediaType.APPLICATION_OCTET_STREAM), Response.class);

        return r;
    }

    public Response execDELETE(String path, Map<String,String> queryParams) {

        WebTarget service = client.target(getBaseURI()).path(path);
        
        if (queryParams == null) {
            for (Map.Entry<String,String> entry : queryParams.entrySet()) {
                service = service.queryParam(entry.getKey(), entry.getValue());   
            }
        }
        
        Response r = service.request(MediaType.APPLICATION_JSON).delete(Response.class);

        return r;
    }
    
}
