package com.oneidentity.safeguard.safeguardclient.authentication;

import com.oneidentity.safeguard.safeguardclient.StringUtils;
import com.oneidentity.safeguard.safeguardclient.exceptions.ObjectDisposedException;
import com.oneidentity.safeguard.safeguardclient.exceptions.SafeguardForJavaException;
import java.util.Arrays;

public class CertificateAuthenticator extends AuthenticatorBase
{
    private boolean _disposed;

    private final String certificateThumbprint;
    private final String certificatePath;
    private final char[] certificatePassword;

    public CertificateAuthenticator(String networkAddress, String certificateThumbprint, int apiVersion,
        boolean ignoreSsl)
    {
        super(networkAddress, apiVersion, ignoreSsl);
        this.certificateThumbprint = certificateThumbprint;
        this.certificatePath = null;
        this.certificatePassword = null;
    }

    public CertificateAuthenticator(String networkAddress, String certificatePath, char[] certificatePassword,
        int apiVersion, boolean ignoreSsl)
    {
        super(networkAddress, apiVersion, ignoreSsl);
        this.certificatePath = certificatePath;
        this.certificatePassword = certificatePassword.clone();
        this.certificateThumbprint = null;
    }

    @Override
    protected char[] GetRstsTokenInternal() throws ObjectDisposedException, SafeguardForJavaException
    {
        if (_disposed)
            throw new ObjectDisposedException("CertificateAuthenticator");

//        var request = new RestRequest("oauth2/token", RestSharp.Method.POST)
//            .AddHeader("Accept", "application/json")
//            .AddHeader("Content-type", "application/json")
//            .AddJsonBody(new
//            {
//                grant_type = "client_credentials",
//                scope = "rsts:sts:primaryproviderid:certificate"
//            });
//        var userCert = !StringUtils.isNullOrEmpty(certificateThumbprint)
//            ? CertificateUtilities.GetClientCertificateFromStore(_certificateThumbprint)
//            : CertificateUtilities.GetClientCertificateFromFile(_certificatePath, _certificatePassword);
//        RstsClient.ClientCertificates = new X509Certificate2Collection() { userCert };
//        var response = RstsClient.Execute(request);
//        if (response.ResponseStatus != ResponseStatus.Completed)
//            throw new SafeguardForJavaException(String.format("Unable to connect to RSTS service %s, Error: ", RstsClient.BaseUrl) +
//                    response.ErrorMessage);
//        if (!response.IsSuccessful) {
//            String msg = StringUtils.isNullOrEmpty(certificatePath) ? String.format("thumbprint=%s", certificateThumbprint) : String.format("file=%s", certificatePath);
//            throw new SafeguardForJavaException("Error using client_credentials grant_type with " + msg +
//                    String.format(", Error: %d %s", response.StatusCode, response.Content), response.Content);
//        }
//        var jObject = JObject.Parse(response.Content);
//        return jObject.GetValue("access_token").ToString().ToSecureString();
return null;
    }

    @Override
    public void Dispose()
    {
        super.Dispose();
        Arrays.fill(certificatePassword, '0');
        _disposed = true;
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            Arrays.fill(certificatePassword, '0');
        } finally {
            _disposed = true;
            super.finalize();
        }
    }
    
}
