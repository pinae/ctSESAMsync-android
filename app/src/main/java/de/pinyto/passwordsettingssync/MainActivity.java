package de.pinyto.passwordsettingssync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;


public class MainActivity extends AppCompatActivity {

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
                    }
                });
    }
}
