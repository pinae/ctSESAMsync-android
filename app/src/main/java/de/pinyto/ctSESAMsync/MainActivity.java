package de.pinyto.ctSESAMsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


public class MainActivity extends AppCompatActivity {

    class ResponseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int respCode = msg.what;

            switch (respCode) {
                case SyncServerRequest.SYNC_RESPONSE: {
                    String syncData = msg.getData().getString("respData");
                    TextView connectionStatus = (TextView) findViewById(R.id.connectionStatus);
                    if (syncData != null && syncData.length() > 0) {
                        try {
                            JSONObject responseObject = new JSONObject(syncData);
                            if (responseObject.getString("status").equals("ok")) {
                                connectionStatus.setText(R.string.connection_ok);
                                connectionStatus.setTextColor(Color.GREEN);
                            } else {
                                connectionStatus.setText(R.string.connection_JSON_ButNotOk);
                                connectionStatus.setTextColor(Color.RED);
                            }
                        } catch (JSONException e) {
                            connectionStatus.setText(R.string.connection_JSON_Exception);
                            connectionStatus.setTextColor(Color.RED);
                        }
                    } else {
                        connectionStatus.setText(R.string.connection_send_error);
                        connectionStatus.setTextColor(Color.RED);
                    }
                }
            }
        }
    }

    private void saveSettings() {
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        EditText domainInput = (EditText) findViewById(R.id.serverDomain);
        EditText pathInput = (EditText) findViewById(R.id.path);
        EditText usernameInput = (EditText) findViewById(R.id.username);
        EditText passwordInput = (EditText) findViewById(R.id.password);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString("serverDomain", domainInput.getText().toString());
        settingsEditor.putString("path", pathInput.getText().toString());
        settingsEditor.putString("username", usernameInput.getText().toString());
        settingsEditor.putString("password", passwordInput.getText().toString());
        settingsEditor.apply();
    }

    private void testCertificate() {
        SyncServerConnection connectionObject = new SyncServerConnection(getBaseContext());
        Button deleteCertificateButton = (Button) findViewById(R.id.deleteCertificateButton);
        try {
            connectionObject.buildKeystore();
            deleteCertificateButton.setVisibility(View.VISIBLE);
        } catch (CertificateException | NoSuchAlgorithmException |
                 KeyStoreException | IOException e) {
            deleteCertificateButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        EditText domainInput = (EditText) findViewById(R.id.serverDomain);
        EditText pathInput = (EditText) findViewById(R.id.path);
        EditText usernameInput = (EditText) findViewById(R.id.username);
        EditText passwordInput = (EditText) findViewById(R.id.password);
        domainInput.setText(settings.getString("serverDomain", ""));
        pathInput.setText(settings.getString("path", ""));
        usernameInput.setText(settings.getString("username", ""));
        passwordInput.setText(settings.getString("password", ""));

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable editable) {
                saveSettings();
            }
        };
        domainInput.addTextChangedListener(watcher);
        pathInput.addTextChangedListener(watcher);
        usernameInput.addTextChangedListener(watcher);
        passwordInput.addTextChangedListener(watcher);

        Button testButton = (Button) findViewById(R.id.testButton);
        testButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new SyncServerRequest(
                        getBaseContext(),
                        new Messenger(new ResponseHandler()),
                        findViewById(R.id.serverDomain),
                        findViewById(R.id.deleteCertificateButton)).execute(
                            "/ajax/read.php",
                            "",
                            Integer.toString(SyncServerRequest.SYNC_RESPONSE)
                        );
                }
            }
        );

        Button deleteCertificateButton = (Button) findViewById(R.id.deleteCertificateButton);
        deleteCertificateButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setVisibility(View.INVISIBLE);
                    SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
                    SharedPreferences.Editor settingsEditor = settings.edit();
                    settingsEditor.putString("certificate", "");
                    settingsEditor.apply();
                }
            }
        );
        this.testCertificate();
    }
}
