package io.github.chronosx88.influence.views;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.main.MainPresenterContract;
import io.github.chronosx88.influence.contracts.main.MainViewContract;
import io.github.chronosx88.influence.contracts.observer.Observer;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.observable.MainObservable;
import io.github.chronosx88.influence.presenters.MainPresenter;
import io.github.chronosx88.influence.views.fragments.ChatListFragment;
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
                    selectedFragment = new ChatListFragment();
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
                .replace(R.id.main_fragment_container, new ChatListFragment())
                .commit();

        presenter = new MainPresenter(this);
        AppHelper.getObservable().register(this, MainObservable.UI_ACTIONS_CHANNEL);

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
        AppHelper.getObservable().unregister(this, MainObservable.UI_ACTIONS_CHANNEL);
    }

    @Override
    public void handleEvent(JsonObject object) {
        switch (object.get("action").getAsInt()) {
            case UIActions.BOOTSTRAP_NOT_SPECIFIED: {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Bootstrap-нода не указана. Прерываю подключение к сети...", Toast.LENGTH_LONG).show();
                });
                break;
            }
            case UIActions.NETWORK_ERROR: {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Ошибка сети. Возможно, нода недоступна, или у вас отсутствует Интернет.", Toast.LENGTH_LONG).show();
                });
                break;
            }
            case UIActions.BOOTSTRAP_SUCCESS: {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Нода успешно запущена!", Toast.LENGTH_LONG).show();
                });
                break;
            }
            case UIActions.PORT_FORWARDING_ERROR: {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Проблемы с пробросом портов. Возможно, у вас не настроен uPnP.", Toast.LENGTH_LONG).show();
                });
                break;
            }
            case UIActions.BOOTSTRAP_ERROR: {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Не удалось подключиться к бутстрап-ноде.", Toast.LENGTH_LONG).show();
                });
                break;
            }
            case UIActions.RELAY_CONNECTION_ERROR: {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Не удалось подключиться к relay-ноде.", Toast.LENGTH_LONG).show();
                });
                break;
            }
        }
    }
}
