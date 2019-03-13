package io.github.chronosx88.influence.views;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.MainPresenterContract;
import io.github.chronosx88.influence.contracts.MainViewContract;
import io.github.chronosx88.influence.contracts.observer.Observer;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.MessageActions;
import io.github.chronosx88.influence.presenters.MainPresenter;
import io.github.chronosx88.influence.views.fragments.ChatFragment;
import io.github.chronosx88.influence.views.fragments.SettingsFragment;
import io.github.chronosx88.influence.views.fragments.StartChatFragment;

public class MainActivity extends AppCompatActivity implements Observer, MainViewContract {

    private MainPresenterContract presenter;
    private ProgressDialog progressDialog;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.action_chats:
                    selectedFragment = new ChatFragment();
                    break;
                case R.id.action_settings:
                    selectedFragment = new SettingsFragment();
                    break;
                case R.id.action_start_chat:
                    selectedFragment = new StartChatFragment();
                    break;
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_fragment_container, selectedFragment);
            transaction.commit();

            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.main_bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, new ChatFragment())
                .commit();

        presenter = new MainPresenter(this);
        AppHelper.getObservable().register(this);

        progressDialog = new ProgressDialog(MainActivity.this, R.style.AlertDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();
        presenter.initPeer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        AppHelper.getObservable().unregister(this);
    }

    @Override
    public void handleEvent(JSONObject object) {
        try {
            switch ((int) object.get("action")) {
                case MessageActions.BOOTSTRAP_NOT_SPECIFIED: {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Bootstrap-нода не указана. Прерываю подключение к сети...", Toast.LENGTH_LONG).show();
                    });

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
