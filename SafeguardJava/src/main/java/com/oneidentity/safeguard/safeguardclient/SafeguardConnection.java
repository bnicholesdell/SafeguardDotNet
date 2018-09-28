package com.oneidentity.safeguard.safeguardclient;

import com.oneidentity.safeguard.safeguardclient.authentication.IAuthenticationMechanism;
import com.oneidentity.safeguard.safeguardclient.data.FullResponse;
import com.oneidentity.safeguard.safeguardclient.data.Method;
import com.oneidentity.safeguard.safeguardclient.data.Service;
import com.oneidentity.safeguard.safeguardclient.exceptions.ObjectDisposedException;
import com.oneidentity.safeguard.safeguardclient.exceptions.SafeguardForJavaException;
import com.oneidentity.safeguard.safeguardclient.restclient.RestClient;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

class SafeguardConnection implements ISafeguardConnection {

    private boolean disposed;

    private final IAuthenticationMechanism authenticationMechanism;

    private final RestClient coreClient;
    private final RestClient applianceClient;
    private final RestClient notificationClient;

    public SafeguardConnection(IAuthenticationMechanism authenticationMechanism) {
        this.authenticationMechanism = authenticationMechanism;

        String safeguardCoreUrl = String.format("https://%s/service/core/v%d",
                this.authenticationMechanism.getNetworkAddress(), this.authenticationMechanism.getApiVersion());
        coreClient = new RestClient(safeguardCoreUrl);

        String safeguardApplianceUrl = String.format("https://%s/service/appliance/v%d",
                this.authenticationMechanism.getNetworkAddress(), this.authenticationMechanism.getApiVersion());
        applianceClient = new RestClient(safeguardApplianceUrl);

        String safeguardNotificationUrl = String.format("https://%s/service/notification/v%d",
                this.authenticationMechanism.getNetworkAddress(), this.authenticationMechanism.getApiVersion());
        notificationClient = new RestClient(safeguardNotificationUrl);

//        if (authenticationMechanism.isIgnoreSsl()) {
//            coreClient.RemoteCertificateValidationCallback += (sender, certificate, chain, errors) =  >   true;
//            applianceClient.RemoteCertificateValidationCallback += (sender, certificate, chain, errors) =  >   true;
//            notificationClient.RemoteCertificateValidationCallback += (sender, certificate, chain, errors) =  >   true;
//        }
    }

    @Override
    public int GetAccessTokenLifetimeRemaining() throws ObjectDisposedException, SafeguardForJavaException {
        if (disposed) {
            throw new ObjectDisposedException("SafeguardConnection");
        }
        return authenticationMechanism.GetAccessTokenLifetimeRemaining();
    }

    @Override
    public void RefreshAccessToken() throws ObjectDisposedException, SafeguardForJavaException {
        if (disposed) {
            throw new ObjectDisposedException("SafeguardConnection");
        }
        authenticationMechanism.RefreshAccessToken();
    }

    @Override
    public String InvokeMethod(Service service, Method method, String relativeUrl, String body,
            Map<String, String> parameters, Map<String, String> additionalHeaders)
            throws ObjectDisposedException, SafeguardForJavaException {
        if (disposed) {
            throw new ObjectDisposedException("SafeguardConnection");
        }
        return InvokeMethodFull(service, method, relativeUrl, body, parameters, additionalHeaders).getBody();
    }

    @Override
    public FullResponse InvokeMethodFull(Service service, Method method, String relativeUrl,
            String body, Map<String, String> parameters, Map<String, String> additionalHeaders)
            throws ObjectDisposedException, SafeguardForJavaException {

        if (disposed) {
            throw new ObjectDisposedException("SafeguardConnection");
        }
        
        RestClient client = GetClientForService(service);
        
        Map<String,String> headers = prepareHeaders(additionalHeaders, service);
                additionalHeaders.putAll(additionalHeaders);
        Response response = null;
        
        switch (method) {
            case Get:

                response = client.execGET(relativeUrl, parameters, headers);
                break;
        }
        
//        var request = new RestRequest(relativeUrl, method.ConvertToRestSharpMethod())
//                .AddHeader("Accept", "application/json");
//        if (service != Service.Notification) { // SecureString handling here basically negates the use of a secure string anyway, but when calling a Web API
//                                               // I'm not sure there is anything you can do about it.
//            request.AddHeader("Authorization", String.format("Bearer %s", new String(authenticationMechanism.GetAccessToken())));
//        }
//        if (additionalHeaders != null) {
//            for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
//                request.AddHeader(header.getKey(), header.getValue());
//            }
//        }
//        if (method == Method.Post || method == Method.Put) {
//            request.AddParameter("application/json", body, ParameterType.RequestBody);
//        }
//        if (parameters != null) {
//            for (Map.Entry<String, String> param : parameters.entrySet()) {
//                request.AddParameter(param.getKey(), param.getValue(), ParameterType.QueryString);
//            }
//        }
//
//        var client = GetClientForService(service);
//        var response = client.Execute(request);

//        if (response.ResponseStatus != ResponseStatus.Completed) {
//            throw new SafeguardForJavaException(String.format("Unable to connect to web service %s, Error: %s", client.BaseUrl, response.ErrorMessage));
//        };
        if (response.getStatus() != 200) {
            String reply = response.readEntity(String.class);
            throw new SafeguardForJavaException("Error returned from Safeguard API, Error: "
                    + String.format("%d %s", response.getStatus(), reply));
        }
            
        FullResponse fullResponse = new FullResponse(response.getStatus(), response.getHeaders(), response.readEntity(String.class));
        return fullResponse;
    }

//    public ISafeguardEventListener GetEventListener() {
//        SafeguardEventListener eventListener = new SafeguardEventListener(
//                String.format("https://%s/service/event", authenticationMechanism.NetworkAddress),
//                authenticationMechanism.GetAccessToken(), authenticationMechanism.IgnoreSsl);
//        return eventListener;
//    }

    private RestClient GetClientForService(Service service) throws SafeguardForJavaException {
        switch (service) {
            case Core:
                return coreClient;
            case Appliance:
                return applianceClient;
            case Notification:
                return notificationClient;
            case A2A:
                throw new SafeguardForJavaException(
                        "You must call the A2A service using the A2A specific method, Error: Unsupported operation");
            default:
                throw new SafeguardForJavaException("Unknown or unsupported service specified");
        }
    }
    
    private Map<String,String> prepareHeaders(Map<String,String> additionalHeaders, Service service) 
            throws ObjectDisposedException {
        
        if (additionalHeaders == null) 
            return null;
        
        Map<String,String> headers = new HashMap<String,String>();
        headers.putAll(additionalHeaders);
        if (service != Service.Notification) { // SecureString handling here basically negates the use of a secure string anyway, but when calling a Web API
                                               // I'm not sure there is anything you can do about it.
            headers.put("Authorization", String.format("Bearer %s", new String(authenticationMechanism.GetAccessToken())));
        }
        
        return headers;
    }

    public void Dispose()
    {
        if (authenticationMechanism != null)
            authenticationMechanism.Dispose();
        disposed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (authenticationMechanism != null)
                authenticationMechanism.Dispose();
        } finally {
            disposed = true;
            super.finalize();
        }
    }
    
}
