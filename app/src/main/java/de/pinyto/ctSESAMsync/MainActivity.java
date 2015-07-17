package de.pinyto.ctSESAMsync;

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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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
                    Log.d("sync response", "recieved");
                    String syncData = msg.getData().getString("respData");
                    Log.d("data", syncData);
                    TextView connectionStatus = (TextView) findViewById(R.id.connectionStatus);
                    if (syncData.length() > 0) {
                        try {
                            JSONObject responseObject = new JSONObject(syncData);
                            Log.d("status", responseObject.getString("status"));
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
        EditText certificateInput = (EditText) findViewById(R.id.certificate);
        EditText usernameInput = (EditText) findViewById(R.id.username);
        EditText passwordInput = (EditText) findViewById(R.id.password);
        Switch syncOnMobileDataSwitch = (Switch) findViewById(R.id.syncOnMobileData);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString("serverDomain", domainInput.getText().toString());
        settingsEditor.putString("path", pathInput.getText().toString());
        settingsEditor.putString("certificate", certificateInput.getText().toString());
        settingsEditor.putString("username", usernameInput.getText().toString());
        settingsEditor.putString("password", passwordInput.getText().toString());
        settingsEditor.putBoolean("syncOnMobileData", syncOnMobileDataSwitch.isChecked());
        settingsEditor.apply();
    }

    private void testCertificate() {
        SyncServerConnection connectionObject = new SyncServerConnection(getBaseContext());
        TextView certificateErrors = (TextView) findViewById(R.id.certificateErrors);
        try {
            connectionObject.buildKeystore();
            certificateErrors.setText("");
        } catch (CertificateException e) {
            certificateErrors.setText(R.string.certificate_error_wrong_format);
        } catch (NoSuchAlgorithmException e) {
            certificateErrors.setText(R.string.certificate_error_no_such_algorithm);
        } catch (IOException e) {
            certificateErrors.setText(R.string.certificate_error_IO_Exception);
        } catch (KeyStoreException e) {
            certificateErrors.setText(R.string.certificate_error_Key_Store_Exception);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        EditText domainInput = (EditText) findViewById(R.id.serverDomain);
        EditText pathInput = (EditText) findViewById(R.id.path);
        EditText certificateInput = (EditText) findViewById(R.id.certificate);
        EditText usernameInput = (EditText) findViewById(R.id.username);
        EditText passwordInput = (EditText) findViewById(R.id.password);
        Switch syncOnMobileDataSwitch = (Switch) findViewById(R.id.syncOnMobileData);
        domainInput.setText(settings.getString("serverDomain", ""));
        pathInput.setText(settings.getString("path", ""));
        certificateInput.setText(settings.getString("certificate", ""));
        usernameInput.setText(settings.getString("username", ""));
        passwordInput.setText(settings.getString("password", ""));
        syncOnMobileDataSwitch.setChecked(settings.getBoolean("syncOnMobileData", true));

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable editable) {
                saveSettings();
                testCertificate();
            }
        };
        domainInput.addTextChangedListener(watcher);
        pathInput.addTextChangedListener(watcher);
        certificateInput.addTextChangedListener(watcher);
        usernameInput.addTextChangedListener(watcher);
        passwordInput.addTextChangedListener(watcher);

        syncOnMobileDataSwitch.setOnCheckedChangeListener(
            new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    saveSettings();
                    testCertificate();
                }
            }
        );

        Button testButton = (Button) findViewById(R.id.testButton);
        testButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new SyncServerRequest(
                        getBaseContext(),
                        new Messenger(new ResponseHandler())).execute(
                            "/ajax/read.php",
                            "",
                            Integer.toString(SyncServerRequest.SYNC_RESPONSE)
                        );
                }
            }
        );

        testCertificate();
    }
}
