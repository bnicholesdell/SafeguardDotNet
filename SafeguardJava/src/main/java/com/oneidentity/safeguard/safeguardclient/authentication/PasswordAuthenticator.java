package com.oneidentity.safeguard.safeguardclient.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneidentity.safeguard.safeguardclient.StringUtils;
import com.oneidentity.safeguard.safeguardclient.data.OauthBody;
import com.oneidentity.safeguard.safeguardclient.exceptions.ObjectDisposedException;
import com.oneidentity.safeguard.safeguardclient.exceptions.SafeguardForJavaException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import jdk.net.SocketFlow;

public class PasswordAuthenticator extends AuthenticatorBase 
{
    private boolean _disposed;

    private final String provider;
    private String providerScope;
    private final String username;
    private final char[] password;

    public PasswordAuthenticator(String networkAddress, String provider, String username,
        char[] password, int apiVersion, boolean ignoreSsl)
    {
        super(networkAddress, apiVersion, ignoreSsl);
        this.provider = provider;
        
        //TODO: What is the providerScope of it isn't null?
        if (StringUtils.isNullOrEmpty(this.provider) || provider.equalsIgnoreCase("local"))
            providerScope = "rsts:sts:primaryproviderid:local";
        
        this.username = username;
        this.password = password;
    }

    private void ResolveProviderToScope() throws SafeguardForJavaException
    {
//        try
//        {
//            IRestResponse response;
//            try
//            {
//                var request = new RestRequest("UserLogin/LoginController", RestSharp.Method.POST)
//                    .AddHeader("Accept", "application/json")
//                    .AddHeader("Content-type", "application/x-www-form-urlencoded")
//                    .AddParameter("response_type", "token", ParameterType.QueryString)
//                    .AddParameter("redirect_uri", "urn:InstalledApplication", ParameterType.QueryString)
//                    .AddParameter("loginRequestStep", 1, ParameterType.QueryString)
//                    .AddBody("RelayState=");
//                response = RstsClient.Execute(request);
//            }
//            catch (WebException)
//            {
//                Log.Debug("Caught exception with POST to find identity provider scopes, trying GET");
//                var request = new RestRequest("UserLogin/LoginController", RestSharp.Method.GET)
//                    .AddHeader("Accept", "application/json")
//                    .AddHeader("Content-type", "application/x-www-form-urlencoded")
//                    .AddParameter("response_type", "token", ParameterType.QueryString)
//                    .AddParameter("redirect_uri", "urn:InstalledApplication", ParameterType.QueryString)
//                    .AddParameter("loginRequestStep", 1, ParameterType.QueryString);
//                response = RstsClient.Execute(request);
//            }
//            if (response.ResponseStatus != ResponseStatus.Completed)
//                throw new SafeguardForJavaException("Unable to connect to RSTS to find identity provider scopes, Error: " +
//                                                   response.ErrorMessage);
//            if (!response.IsSuccessful)
//                throw new SafeguardForJavaException("Error requesting identity provider scopes from RSTS, Error: " +
//                                                   String.format("%d %s", response.StatusCode, response.Content), response.Content);
//            var jObject = JObject.Parse(response.Content);
//            var jProviders = (JArray)jObject["Providers"];
//            var knownScopes = jProviders.Select(s => s["Id"]).Values<string>().ToArray();
//            var scope = knownScopes.FirstOrDefault(s => s.EqualsNoCase(provider));
//            if (scope != null)
//                providerScope = String.format("rsts:sts:primaryproviderid:%s", scope);
//            else
//            {
//                scope = knownScopes.FirstOrDefault(s => s.ContainsNoCase(provider));
//                if (providerScope != null)
//                    providerScope = String.format("rsts:sts:primaryproviderid:%s", scope);
//                else
//                    throw new SafeguardForJavaException(String.format("Unable to find scope matching '%s' in [%s]", provider, String.Join(",", knownScopes)));
//            }
//        }
//        catch (Exception ex)
//        {
//            throw new SafeguardForJavaException("Unable to connect to determine identity provider", ex);
//        }
    }

    @Override
    protected char[] GetRstsTokenInternal() throws ObjectDisposedException, SafeguardForJavaException
    {
        if (_disposed)
            throw new ObjectDisposedException("PasswordAuthenticator");
        if (providerScope == null)
            ResolveProviderToScope();

        OauthBody body = new OauthBody("password", username, password, providerScope);
        Response response = RstsClient.execPOST("oauth2/token", null, body);

        if (response.getStatus() != 200)
            throw new SafeguardForJavaException(String.format("Unable to connect to RSTS service %s, Error: %s", RstsClient.getBaseURL(), response.readEntity(String.class)));
//        if (!response.IsSuccessful)
//            throw new SafeguardForJavaException(String.format("Error using password grant_type with scope %s, Error: ", providerScope) +
//                                               String.format("%s %s", response.StatusCode, response.Content), response.Content);

        Map<String,String> map = ParseResponse(response);

//        String resp = response.readEntity(String.class);
//        
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String,String> map = new HashMap<String,String>();
//        try {
//            map = mapper.readValue(resp, new TypeReference<Map<String,String>>(){});
//        } catch (IOException ex) {
//            Logger.getLogger(PasswordAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
//        }

        String accessToken = map.get("access_token");
        return accessToken.toCharArray();
    }

    @Override
    public void Dispose()
    {
        super.Dispose();
        Arrays.fill(password, '0');
        _disposed = true;
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            Arrays.fill(password, '0');
        } finally {
            _disposed = true;
            super.finalize();
        }
    }
    
}
