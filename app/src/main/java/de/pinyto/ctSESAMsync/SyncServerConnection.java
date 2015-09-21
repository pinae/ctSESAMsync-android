package de.pinyto.ctSESAMsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Use this class to connect to sync servers. This is used by the service and by the
 * activity for testing the settings.
 */
public class SyncServerConnection {
    final Context contentContext;

    public SyncServerConnection(Context contentContext) {
        this.contentContext = contentContext;
    }

    public KeyStore buildKeystore() throws
            KeyStoreException,
            CertificateException,
            NoSuchAlgorithmException,
            IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        SharedPreferences settings = contentContext.getSharedPreferences(
                "settings", Context.MODE_PRIVATE);
        String certificate = CertificateCleaner.cleanCertificate(
                settings.getString("certificate", ""));
        InputStream caInput;
        try {
            caInput = new ByteArrayInputStream(certificate.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            caInput = new ByteArrayInputStream(certificate.getBytes());
        }
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
        } finally {
            caInput.close();
        }
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);
        return keyStore;
    }

    private TrustManager[] getTrustManagers() throws
            NoSuchAlgorithmException,
            KeyStoreException,
            CertificateException,
            IOException {
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(buildKeystore());
        return tmf.getTrustManagers();
    }

    private SSLContext getSSLContext() throws
            NoSuchAlgorithmException,
            KeyManagementException,
            KeyStoreException,
            CertificateException,
            IOException {
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, getTrustManagers(), null);
        return sslcontext;
    }

    private HttpsURLConnection establishHttpsConnection(String path) {
        try {
            SharedPreferences settings = contentContext.getSharedPreferences(
                    "settings", Context.MODE_PRIVATE);
            String host = DomainExtractor.extractFullDomain(settings.getString("serverDomain", ""));
            String directory = settings.getString("path", "");
            directory = directory.replaceAll("^/+|/+$", "");
            URL url = new URL("https://" + host + "/" + directory + path);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(getSSLContext().getSocketFactory());
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Host", host);
            connection.setRequestProperty("Connection", "keep-alive");
            //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization",
                    "Basic " + Base64.encodeToString(
                            (settings.getString("username", "") + ":" +
                                    settings.getString("password", "")).getBytes("UTF-8"),
                            Base64.DEFAULT));
            connection.setDoOutput(true);
            connection.setDoInput(true);
            return connection;
        } catch (MalformedURLException e) {
            Log.d("Authentication error",
                    "The URL is malformed. Probably host or directory is wrong.");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.d("Authentication error", "Could not establish connection.");
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            Log.d("Authentication error", "TLS or TrustManager is not implemented.");
            e.printStackTrace();
            return null;
        } catch (KeyManagementException e) {
            Log.d("Authentication error", "Key management is broken.");
            e.printStackTrace();
            return null;
        } catch (KeyStoreException e) {
            Log.d("Authentication error", "KeyStore is broken.");
            e.printStackTrace();
            return null;
        } catch (CertificateException e) {
            Log.d("Authentication error", "The local certificate is invalid.");
            e.printStackTrace();
            return null;
        }
    }

    public String makeRequest(String path, String request, int responseType) {
        HttpsURLConnection connection = establishHttpsConnection(path);
        if (connection != null) {
            try {
                DataOutputStream requestStream = new DataOutputStream(connection.getOutputStream());
                requestStream.writeBytes(request);
                requestStream.flush();
                requestStream.close();
                InputStream responseStream = connection.getInputStream();
                InputStreamReader responseReader = new InputStreamReader(responseStream);
                BufferedReader bufferedResponseReader = new BufferedReader(responseReader);
                String response = "";
                String responseLine;
                while ((responseLine = bufferedResponseReader.readLine()) != null) {
                    response += responseLine;
                }
                JSONObject resultObject = new JSONObject();
                resultObject.put("response", response);
                resultObject.put("responseType", responseType);
                return resultObject.toString();
            } catch (IOException e) {
                Log.d("Request error", "IO Error while sending request.");
                e.printStackTrace();
            } catch (JSONException e) {
                Log.d("Request error", "Could not pack response.");
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
        }
        return "";
    }
}
