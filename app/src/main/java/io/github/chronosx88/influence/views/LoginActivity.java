/*
 * Copyright (C) 2019 ChronosX88
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.chronosx88.influence.views;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.XMPPConnectionService;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;

public class LoginActivity extends AppCompatActivity implements CoreContracts.ILoginViewContract {
    private EditText jidEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private BroadcastReceiver broadcastReceiver;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        jidEditText = findViewById(R.id.login_jid);
        passwordEditText = findViewById(R.id.login_password);
        signInButton = findViewById(R.id.sign_in_button);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        signInButton.setOnClickListener((v) -> {
            if(checkLoginCredentials()) {
                saveLoginCredentials();
                doLogin();
            }
        });
    }

    @Override
    public void loadingScreen(boolean state) {
        if(state)
            progressDialog.show();
        else
            progressDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case XMPPConnectionService.INTENT_AUTHENTICATED: {
                        loadingScreen(false);
                        finish();
                        break;
                    }
                    case XMPPConnectionService.INTENT_AUTHENTICATION_FAILED: {
                        loadingScreen(false);
                        jidEditText.setError("Invalid JID/Password/Server");
                        break;
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(XMPPConnectionService.INTENT_AUTHENTICATED);
        filter.addAction(XMPPConnectionService.INTENT_AUTHENTICATION_FAILED);
        this.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(broadcastReceiver);
    }

    private boolean checkLoginCredentials() {
        jidEditText.setError(null);
        passwordEditText.setError(null);

        String jid = jidEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordEditText.setError("Invalid password");
            focusView = passwordEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(jid)) {
            jidEditText.setError("Field is required!");
            focusView = jidEditText;
            cancel = true;
        } else if (!isEmailValid(jid)) {
            jidEditText.setError("Invalid JID");
            focusView = jidEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void saveLoginCredentials() {
        AppHelper.getPreferences().edit()
                .putString("jid", jidEditText.getText().toString())
                .putString("pass", passwordEditText.getText().toString())
                .putBoolean("logged_in", true)
                .apply();
    }

    private void doLogin() {
        loadingScreen(true);
        startService(new Intent(this, XMPPConnectionService.class));
    }
}
