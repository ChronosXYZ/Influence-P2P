package io.github.chronosx88.influence.views;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.presenters.MainPresenter;
import io.github.chronosx88.influence.views.fragments.DialogListFragment;
import io.github.chronosx88.influence.views.fragments.SettingsFragment;
import kotlin.Pair;

public class MainActivity extends AppCompatActivity implements CoreContracts.IMainViewContract {

    private CoreContracts.IMainPresenterContract presenter;
    private ProgressDialog progressDialog;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = (item) -> {
        Fragment selectedFragment = null;

        switch (item.getItemId()) {
            case R.id.action_chats:
                selectedFragment = new DialogListFragment();
                break;
            case R.id.action_settings:
                selectedFragment = new SettingsFragment();
                break;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, selectedFragment);
        transaction.commit();

        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.main_bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FloatingActionButton fab = findViewById(R.id.add_chat);
        fab.setOnClickListener((v) -> {
            Pair<AlertDialog.Builder, EditText> pair = ViewUtils.INSTANCE.setupEditTextDialog(MainActivity.this, getString(R.string.input_companion_username));
            pair.getFirst().setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                presenter.startChatWithPeer(pair.getSecond().getText().toString());
            });
            pair.getFirst().setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                dialog.cancel();
            });
            pair.getFirst().show();
        });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, new DialogListFragment())
                .commit();

        presenter = new MainPresenter(this);

        progressDialog = new ProgressDialog(MainActivity.this, R.style.AlertDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        presenter.initConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_logout_item: {
                presenter.logoutFromAccount();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            }
        }

        return true;
    }

    @Override
    public void showSnackbar(@NotNull String message) {
        runOnUiThread(() -> Snackbar.make(getRootView(), message, Snackbar.LENGTH_LONG).show());
    }

    @Override
    public void showProgressBar(boolean state) {
        runOnUiThread(() -> {
            if(state) {
                progressDialog.show();
            } else {
                progressDialog.dismiss();
            }
        });
    }

    private View getRootView() {
        final ViewGroup contentViewGroup = findViewById(android.R.id.content);
        View rootView = null;

        if(contentViewGroup != null)
            rootView = contentViewGroup.getChildAt(0);

        if(rootView == null)
            rootView = getWindow().getDecorView().getRootView();

        return rootView;
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
