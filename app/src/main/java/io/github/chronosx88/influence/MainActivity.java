package io.github.chronosx88.influence;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import io.github.chronosx88.influence.contracts.MainPresenterContract;
import io.github.chronosx88.influence.contracts.MainViewContract;
import io.github.chronosx88.influence.contracts.observer.Observer;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.MessageActions;
import io.github.chronosx88.influence.presenters.MainPresenter;

public class MainActivity extends AppCompatActivity implements Observer, MainViewContract {

    private MainPresenterContract presenter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
