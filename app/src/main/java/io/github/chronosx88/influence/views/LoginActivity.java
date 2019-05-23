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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.XMPPConnectionService;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.HashUtils;
import io.github.chronosx88.influence.models.appEvents.AuthenticationStatusEvent;

public class LoginActivity extends AppCompatActivity implements CoreContracts.ILoginViewContract {
    private EditText jidEditText;
    private EditText passwordEditText;
    private TextInputLayout jidInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button signInButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        jidEditText = findViewById(R.id.login_jid);
        passwordEditText = findViewById(R.id.login_password);

        jidInputLayout = findViewById(R.id.jid_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        jidInputLayout.setErrorEnabled(true);
        passwordInputLayout.setErrorEnabled(true);

        signInButton = findViewById(R.id.sign_in_button);
        progressDialog = new ProgressDialog(LoginActivity.this, R.style.AlertDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        signInButton.setOnClickListener((v) -> {
            if(checkLoginCredentials()) {
                saveLoginCredentials();
                doLogin();
            }
        });
        EventBus.getDefault().register(this);
    }

    @Override
    public void loadingScreen(boolean state) {
        if(state)
            progressDialog.show();
        else
            progressDialog.dismiss();
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
        } else if (!isJidValid(jid)) {
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

    private boolean isJidValid(String jid) {
        return jid.contains("@");
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
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                XMPPConnectionService.XMPPServiceBinder binder = (XMPPConnectionService.XMPPServiceBinder) service;
                AppHelper.setXmppConnection(binder.getConnection());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                AppHelper.setXmppConnection(null);
            }
        };
        AppHelper.setServiceConnection(connection);
        bindService(new Intent(this, XMPPConnectionService.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthenticate(AuthenticationStatusEvent event) {
        switch (event.authenticationStatus) {
            case AuthenticationStatusEvent.CONNECT_AND_LOGIN_SUCCESSFUL: {
                loadingScreen(false);
                finish();
                break;
            }
            case AuthenticationStatusEvent.INCORRECT_LOGIN_OR_PASSWORD: {
                loadingScreen(false);
                passwordInputLayout.setError("Invalid JID/Password");
                break;
            }
            case AuthenticationStatusEvent.NETWORK_ERROR: {
                loadingScreen(false);
                jidInputLayout.setError("Network error");
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
