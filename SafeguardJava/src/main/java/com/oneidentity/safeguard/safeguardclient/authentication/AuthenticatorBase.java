package com.oneidentity.safeguard.safeguardclient.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneidentity.safeguard.safeguardclient.data.AccessTokenBody;
import com.oneidentity.safeguard.safeguardclient.data.OauthBody;
import com.oneidentity.safeguard.safeguardclient.exceptions.ObjectDisposedException;
import com.oneidentity.safeguard.safeguardclient.exceptions.SafeguardForJavaException;
import com.oneidentity.safeguard.safeguardclient.restclient.RestClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;

abstract class AuthenticatorBase implements IAuthenticationMechanism
{
    private boolean _disposed;

    private final String NetworkAddress; 
    private final int ApiVersion;
    private boolean IgnoreSsl;
    
    protected char[] AccessToken;

    protected final String SafeguardRstsUrl;
    protected final String SafeguardCoreUrl;

    protected RestClient RstsClient;
    protected RestClient CoreClient;

    protected AuthenticatorBase(String networkAddress, int apiVersion, boolean ignoreSsl)
    {
        NetworkAddress = networkAddress;
        ApiVersion = apiVersion;

        SafeguardRstsUrl = String.format("https://%s/RSTS", NetworkAddress);
        RstsClient = new RestClient(SafeguardRstsUrl);

        SafeguardCoreUrl = String.format("https://%s/service/core/v%d", NetworkAddress, ApiVersion);
        CoreClient = new RestClient(SafeguardCoreUrl);

//        if (ignoreSsl)
//        {
//            IgnoreSsl = true;
//            RstsClient.RemoteCertificateValidationCallback += (sender, certificate, chain, errors) => true;
//            CoreClient.RemoteCertificateValidationCallback += (sender, certificate, chain, errors) => true;
//        }
    }

    public String getNetworkAddress() {
        return NetworkAddress;
    }

    public int getApiVersion() {
        return ApiVersion;
    }

    public boolean isIgnoreSsl() {
        return IgnoreSsl;
    }

    public boolean HasAccessToken() {
        return AccessToken != null;
    }

    public char[] GetAccessToken() throws ObjectDisposedException {
        if (_disposed)
            throw new ObjectDisposedException("AuthenticatorBase");
        return AccessToken;
    }

    public int GetAccessTokenLifetimeRemaining() throws ObjectDisposedException, SafeguardForJavaException {
        if (_disposed)
            throw new ObjectDisposedException("AuthenticatorBase");
        if (!HasAccessToken())
            return 0;
        
        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", String.format("Bearer %s", new String(AccessToken)));
        headers.put("X-TokenLifetimeRemaining", "");
        
        Response response = CoreClient.execGET("LoginMessage", null, headers);
        
        if (response.getStatus() != 200)
            throw new SafeguardForJavaException(String.format("Unable to connect to web service %s, Error: ", CoreClient.getBaseURL()) + response.readEntity(String.class));
//        if (!response.IsSuccessful)
//            return 0;

        String remainingStr = response.getHeaderString("X-TokenLifetimeRemaining");

        int remaining = 10; // Random magic value... the access token was good, but for some reason it didn't return the remaining lifetime
        if (remainingStr != null) {
            try {
                remaining = Integer.parseInt(remainingStr);
            }
            catch (Exception e) {
            }
        }
            
        return remaining;
    }

    public void RefreshAccessToken() throws ObjectDisposedException, SafeguardForJavaException {
        
        if (_disposed)
            throw new ObjectDisposedException("AuthenticatorBase");
        
        char[] rStsToken = GetRstsTokenInternal();
        AccessTokenBody body = new AccessTokenBody(rStsToken);
        Response response = CoreClient.execPOST("Token/LoginResponse", null, body);

        if (response.getStatus() != 200)
            throw new SafeguardForJavaException(String.format("Unable to connect to web service %s, Error: ", CoreClient.getBaseURL()) + response.readEntity(String.class));
//        if (!response.IsSuccessful)
//            throw new SafeguardForJavaException("Error exchanging RSTS token for Safeguard API access token, Error: " +
//                                               String.format("%d %s", response.StatusCode, response.Content), response.Content);

        Map<String,String> map = ParseResponse(response);

//        var jObject = JObject.Parse(response.Content);
//        AccessToken = jObject.GetValue("UserToken").ToString().ToSecureString();
        
        AccessToken =  map.get("UserToken").toCharArray();
    }

    protected abstract char[] GetRstsTokenInternal() throws ObjectDisposedException, SafeguardForJavaException;
    
    protected Map<String,String> ParseResponse(Response response) {
        String resp = response.readEntity(String.class);
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String,String> map = new HashMap<String,String>();
        try {
            map = mapper.readValue(resp, new TypeReference<Map<String,String>>(){});
        } catch (IOException ex) {
            Logger.getLogger(PasswordAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return map;
    }

    public void Dispose()
    {
        Arrays.fill(AccessToken, '0');
        _disposed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            Arrays.fill(AccessToken, '0');
        } finally {
            _disposed = true;
            super.finalize();
        }
    }
    
}
