package cz.binarytrio.molescope.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cz.binarytrio.molescope.R;
import cz.binarytrio.molescope.application.MoleApp;
import cz.binarytrio.molescope.listener.AFSDownloadListener;
import cz.binarytrio.molescope.listener.YNListener;
import cz.binarytrio.molescope.util.Helper;
import cz.binarytrio.molescope.util.ModelKeeperService;
import cz.binarytrio.molescope.util.ModelKeeperStateReceiver;

public class SplashScreenActivity extends Activity implements AFSDownloadListener {

    private static final int DELAY_TIME = 1000;

    private static final Handler mHandler = new Handler();


    private static final int NOTIFY_ID = 1;
    private ModelKeeperStateReceiver mReceiver;
    private NotificationManager mNotificationManager;
    private Notification.Builder mBuilder;

    @Override
    public void onAttributesObtained(long versionNumber, long downloadSizeB) {
        Toast.makeText(this, "model v." + Helper.describeVersion(versionNumber) + " size " + Helper.describeSize(downloadSizeB), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDownloadProgress(float progressPercentage, float speedBpS) {
        Helper.log("progress tracker " + progressPercentage + "% done (" + Helper.describeSize((long)speedBpS) + "/S)");
        mBuilder.setProgress((int) progressPercentage, 100, false);
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    @Override
    public void onDownloadFinished(long durationMillis) {
        mNotificationManager.cancel(NOTIFY_ID);
        goFurther();
    }

    @Override
    public void onDownloadError(String exception) {
        Helper.showOkDialog(this, "Error while downloading", exception);
        mNotificationManager.cancel(NOTIFY_ID);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new ModelKeeperStateReceiver(this);
        registerReceiver(mReceiver, new IntentFilter(ModelKeeperService.EVENT_MODEL_ATTRIBUTES_OBTAINED));
        registerReceiver(mReceiver, new IntentFilter(ModelKeeperService.EVENT_MODEL_FETCH_FAIL));
        registerReceiver(mReceiver, new IntentFilter(ModelKeeperService.EVENT_MODEL_FETCH_FINISHED));
        registerReceiver(mReceiver, new IntentFilter(ModelKeeperService.EVENT_MODEL_FETCH_PROGRESS));

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Model downloading")
//                            .setLargeIcon(R.drawable.doctor)
                .setSmallIcon(R.drawable.doctor);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null)
            getActionBar().hide();

        setContentView(R.layout.activity_splash_screen);
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.relative_splash);

        long localVersion = Helper.getLocalModelVersion(Helper.getLocalModelStorage(this, MoleApp.MODEL_NAME, false), MoleApp.VERSIONFILE_EXTENSION);
        //download model if it doesnt exist
        if (localVersion==Helper.UNDEFINED) {
            Helper.showYNDialog(this, R.string.no_model_title, R.string.no_model_msg, new YNListener() {
                @Override
                public void yes() {
                    startService(new Intent(SplashScreenActivity.this, ModelKeeperService.class).putExtra(ModelKeeperService.ATTR_KEY_1, ModelKeeperService.ACTION_FETCH_MODEL));
                    mBuilder.setProgress(0, 0, false);
                    mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
                }

                @Override
                public void no() {
                    onBackPressed();
                }
            });
        } else Helper.log("found local model " + Helper.describeVersion(localVersion) );


//        final Runnable looper = new Runnable() {
//            @Override
//            public void run() {
//                goFurther();
//            }
//        };
//
//        mHandler.postDelayed(looper, DELAY_TIME);

        if(mainLayout != null){
            mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.log("Splash screen clicked.");
//                    mHandler.removeCallbacks(looper);
//                    goFurther();
                    startService(new Intent(SplashScreenActivity.this, ModelKeeperService.class).putExtra(ModelKeeperService.ATTR_KEY_1, ModelKeeperService.ACTION_FETCH_MODEL));
                    mBuilder.setProgress(0, 0, false);
                    mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
                }
            });
        }
    }

    private void goFurther() {
        startActivity(new Intent(SplashScreenActivity.this, ClassifierActivity.class));
    }
}
