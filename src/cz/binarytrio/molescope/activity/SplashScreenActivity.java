package cz.binarytrio.molescope.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;

import cz.binarytrio.molescope.R;
import cz.binarytrio.molescope.application.MoleApp;
import cz.binarytrio.molescope.listener.AFSDownloadListener;
import cz.binarytrio.molescope.listener.YNListener;
import cz.binarytrio.molescope.util.Helper;
import cz.binarytrio.molescope.util.ModelKeeperService;
import cz.binarytrio.molescope.util.ModelKeeperStateReceiver;

public class SplashScreenActivity extends Activity implements AFSDownloadListener {

    private static final Handler mHandler = new Handler();
    private static final int DELAY_TIME = 150;
    private static final int NOTIFY_ID = 1;
    private final int COLOR_OK_REACHED = Color.parseColor("#99FA99");
    private final int COLOR_OK_UNREACHED = Color.parseColor("#CCCCCC");
    private final int COLOR_FAIL = Color.parseColor("#FF3D7F");

    private ModelKeeperStateReceiver mReceiver;

    private NotificationManager mNotificationManager;

    private Notification.Builder mBuilder;

    private TextView mDownloadSpeedTV;
    private TextView mDownloadStatusTV;
    private TextView mETATV;

    private NumberProgressBar mDownloadProgressBar;

    private long mDownloadSizeBytes;


    @Override
    public void onAttributesObtained(long versionNumber, long downloadSizeB) {
        mDownloadSizeBytes = downloadSizeB;
        String status = getString(R.string.downloading_model) + " " + Helper.describeVersion(versionNumber) + " (" + Helper.describeSize(downloadSizeB) + ")";
        mDownloadStatusTV.setText(status);
        mBuilder.setContentText(status);
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    @Override
    public void onDownloadProgress(float progressPercentage, long speedBpS) {
        Helper.log("progress tracker " + progressPercentage + "% done (" + Helper.describeSpeed(speedBpS) + ")");

        String speed = Helper.describeSpeed(speedBpS);
        String eta = Helper.describeTime((long) (mDownloadSizeBytes*(100-progressPercentage)*1000/speedBpS)) + " " + getString(R.string.left);

        mBuilder.setProgress(100, (int) progressPercentage, false);
        mBuilder.setContentText(speed + ", " + eta);
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());

        mDownloadProgressBar.setProgress((int) progressPercentage);
        mDownloadSpeedTV.setText(speed);
        mETATV.setText(eta);
    }

    @Override
    public void onDownloadFinished(long durationMillis) {
        mNotificationManager.cancel(NOTIFY_ID);

        mDownloadProgressBar.setProgress(100);
        mDownloadSpeedTV.setText("");
        mETATV.setText("");

        startMainActivity();
    }

    @Override
    public void onDownloadError(String exception) {
        Helper.showOkDialog(this, "Error while downloading", exception);
        Toast.makeText(this, R.string.download_error_occured, Toast.LENGTH_LONG).show();
        mDownloadProgressBar.setReachedBarColor(COLOR_FAIL);
        mDownloadProgressBar.setProgressTextColor(COLOR_FAIL);
        mDownloadProgressBar.setUnreachedBarColor(COLOR_FAIL);
        resetProgressViews();
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
                .setContentText(getString(R.string.downloading_model_attributes))
                .setSmallIcon(R.drawable.doctor)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.doctor));
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
        mDownloadSpeedTV = (TextView) findViewById(R.id.tv_download_speed);
        mDownloadStatusTV = (TextView) findViewById(R.id.tv_download_status);
        mETATV = (TextView) findViewById(R.id.tv_eta);
        mDownloadProgressBar = (NumberProgressBar) findViewById(R.id.npb_download);

        long localVersion = Helper.getLocalModelVersion(Helper.getLocalModelStorage(this, MoleApp.MODEL_NAME, false), MoleApp.VERSIONFILE_EXTENSION);
        if (localVersion==Helper.UNDEFINED) {
            Helper.showYNDialog(this, R.string.no_model_title, R.string.no_model_msg, new YNListener() {
                @Override public void yes() {startModelDownload();}
                @Override public void no() {finish();}
            });
        } else {
            Helper.log("found local model " + Helper.describeVersion(localVersion) );
            mDownloadStatusTV.setText(getString(R.string.found) + " " + Helper.describeVersion(localVersion));
            mHandler.postDelayed(new Runnable() {@Override public void run() {startMainActivity();}}, DELAY_TIME);
        }
    }

    private void startModelDownload() {
        startService(new Intent(this, ModelKeeperService.class).putExtra(ModelKeeperService.ATTR_KEY_1, ModelKeeperService.ACTION_FETCH_MODEL));
        mBuilder.setProgress(100, 0, false);
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
        resetProgressBar();
        mDownloadStatusTV.setText(R.string.downloading_model_attributes);
    }

    private void startMainActivity() {
        startActivity(new Intent(this, ClassifierActivity.class));
        finish();
    }

    private void resetProgressViews() {
        mNotificationManager.cancel(NOTIFY_ID);
        mDownloadSpeedTV.setText("");
        mETATV.setText("");
    }

    private void resetProgressBar() {
        mDownloadProgressBar.setVisibility(View.VISIBLE);
        mDownloadProgressBar.setReachedBarColor(COLOR_OK_REACHED);
        mDownloadProgressBar.setProgressTextColor(COLOR_OK_REACHED);
        mDownloadProgressBar.setUnreachedBarColor(COLOR_OK_UNREACHED);
        mDownloadProgressBar.setProgress(0);
    }
}
