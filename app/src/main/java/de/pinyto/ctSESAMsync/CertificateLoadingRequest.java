package de.pinyto.ctSESAMsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Messenger;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This task loads certificate information from a SSL host.
 */
public class CertificateLoadingRequest extends AsyncTask<String, Void, JSONObject> {
    private Context context;
    private View popupAnchor;
    private Messenger replyMessenger;
    private String path = "";
    private String requestPayload = "";
    private int responseType = SyncServerRequest.SYNC_RESPONSE;
    private View certExists;

    public CertificateLoadingRequest(Context context, View popupAnchor,
                                     Messenger replyMessenger, View certExists) {
        this.context = context;
        this.popupAnchor = popupAnchor;
        this.replyMessenger = replyMessenger;
        this.certExists = certExists;
    }

    private JSONObject getErrorResponse(String error) {
        JSONObject errorResponse = new JSONObject();
        try {
            errorResponse.put("Error", error);
        } catch (JSONException jsonError) {
            jsonError.printStackTrace();
        }
        return errorResponse;
    }

    private SSLContext getSSLContext() throws
            NoSuchAlgorithmException,
            KeyManagementException,
            KeyStoreException,
            CertificateException,
            IOException {
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
        sslcontext.init(null, trustAllCerts, null);
        return sslcontext;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            this.path = params[0];
            this.requestPayload = params[1];
            this.responseType = Integer.parseInt(params[2]);
            SharedPreferences settings = context.getSharedPreferences(
                    "settings", Context.MODE_PRIVATE);
            String host = DomainExtractor.extractFullDomain(settings.getString("serverDomain", ""));
            host = host.replaceAll("/+$", "");
            String directory = settings.getString("path", "");
            directory = directory.replaceAll("^/+|/+$", "");
            String path = this.path;
            if (directory.length() <= 0) {
                path = path.replaceFirst("^/+", "");
            }
            this.requestPayload = params[1];
            this.responseType = Integer.parseInt(params[2]);
            URL url = new URL("https://" + host + "/" + directory + path);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(getSSLContext().getSocketFactory());
            connection.connect();
            Certificate[] chain = connection.getServerCertificates();
            JSONObject result = new JSONObject();
            JSONArray jsonChain = new JSONArray();
            for (Certificate cert : chain) {
                JSONObject jsonCert = new JSONObject();
                Pattern subjectPattern = Pattern.compile("CN=([^,]+)");
                Matcher m = subjectPattern.matcher(
                        ((X509Certificate) cert).getSubjectDN().getName());
                if (m.find()) {
                    jsonCert.put("subject", m.group(1));
                } else {
                    jsonCert.put("subject", ((X509Certificate) cert).getSubjectDN().getName());
                }
                byte[] binaryCertificate = cert.getEncoded();
                jsonCert.put("pem", "-----BEGIN CERTIFICATE-----\n" +
                        Base64.encodeToString(binaryCertificate, Base64.DEFAULT) +
                        "\n-----END CERTIFICATE-----");
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(binaryCertificate);
                jsonCert.put("md5", Hextools.formatWithColons(
                        Hextools.bytesToHex(digest.digest())));
                digest = java.security.MessageDigest.getInstance("SHA1");
                digest.update(binaryCertificate);
                jsonCert.put("sha1", Hextools.formatWithColons(
                        Hextools.bytesToHex(digest.digest())));
                digest = java.security.MessageDigest.getInstance("SHA256");
                digest.update(binaryCertificate);
                jsonCert.put("sha256", Hextools.formatWithColons(
                        Hextools.bytesToHex(digest.digest())));
                jsonChain.put(jsonCert);
            }
            result.put("chain", jsonChain);
            return result;
        } catch (MalformedURLException urlError) {
            urlError.printStackTrace();
            return this.getErrorResponse("malformatted url");
        } catch (IOException ioError) {
            ioError.printStackTrace();
            return this.getErrorResponse("io error while opening connection");
        } catch (NoSuchAlgorithmException algorithmError) {
            algorithmError.printStackTrace();
            return this.getErrorResponse("no tls support");
        } catch (KeyManagementException keyManagementError) {
            keyManagementError.printStackTrace();
            return this.getErrorResponse("broken key management");
        } catch (KeyStoreException keyStoreError) {
            keyStoreError.printStackTrace();
            return this.getErrorResponse("broken key store");
        } catch (CertificateException certificateError) {
            certificateError.printStackTrace();
            return this.getErrorResponse("invalid certificate");
        } catch (JSONException jsonError) {
            jsonError.printStackTrace();
            return this.getErrorResponse("json problem");
        }
    }

    private void showAcceptCertificatePopup(final JSONArray chain) {
        LayoutInflater layoutInflater = (LayoutInflater)
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View certPopupView = layoutInflater.inflate(R.layout.cert_popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                certPopupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        try {
            String certificateInfo = "";
            for (int i=0; i < chain.length(); i++) {
                JSONObject singleCertInfo = chain.getJSONObject(i);
                if (i > 0) {
                    certificateInfo += "\n---------------------------\n";
                }
                certificateInfo += ":::: " + singleCertInfo.getString("subject") + " ::::\n" +
                    String.format("MD5: %s", singleCertInfo.getString("md5")) + "\n" +
                    String.format("SHA-1: %s", singleCertInfo.getString("sha1")) + "\n" +
                    String.format("SHA-256: %s", singleCertInfo.getString("sha256"));
            }

            TextView certInfoView = (TextView) certPopupView.findViewById(R.id.certInfoTextView);
            certInfoView.setText(certificateInfo);
        } catch (JSONException jsonError) {
            jsonError.printStackTrace();
        }
        Button btnDismiss = (Button) certPopupView.findViewById(R.id.dismissButton);
        btnDismiss.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        Button btnAccept = (Button) certPopupView.findViewById(R.id.acceptButton);
        btnAccept.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    String pem = chain.getJSONObject(chain.length() - 1).getString("pem");
                    SharedPreferences settings = context.getSharedPreferences(
                            "settings", Context.MODE_PRIVATE);
                    SharedPreferences.Editor settingsEditor = settings.edit();
                    settingsEditor.putString("certificate", pem);
                    settingsEditor.apply();
                    if (certExists != null) {
                        certExists.setVisibility(View.VISIBLE);
                    }
                    new SyncServerRequest(
                        context,
                        replyMessenger,
                        popupAnchor,
                        certExists).execute(
                            path,
                            requestPayload,
                            Integer.toString(responseType)
                    );
                } catch (JSONException jsonError) {
                    jsonError.printStackTrace();
                }
                popupWindow.dismiss();
            }
        });
        popupWindow.showAsDropDown(this.popupAnchor);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        if (result.has("error")) {
            try {
                String error = result.getString("error");
                Log.e("error", error);
            } catch (JSONException jsonError) {
                jsonError.printStackTrace();
            }
        } else if (result.has("chain")) {
            try {
                JSONArray chain = result.getJSONArray("chain");
                showAcceptCertificatePopup(chain);
            } catch (JSONException jsonError) {
                jsonError.printStackTrace();
            }
        }
    }
}
