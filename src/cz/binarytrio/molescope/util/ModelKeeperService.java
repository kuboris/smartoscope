package cz.binarytrio.molescope.util;

import android.app.IntentService;
import android.content.Intent;

import com.microsoft.azure.storage.file.CloudFile;

import cz.binarytrio.molescope.application.MoleApp;
import cz.binarytrio.molescope.listener.AFSDownloadListener;

/**
 * Created by nicko on 4/20/17
 */

public class ModelKeeperService extends IntentService implements AFSDownloadListener {

    public static final String ATTR_KEY_1 = "cz.binarytrio.molescope.ATTR_KEY_1";
    public static final String ATTR_KEY_2 = "cz.binarytrio.molescope.ATTR_KEY_2";

    public static final int ACTION_CHECK_VERSION = 1;
    public static final int ACTION_FETCH_MODEL = 2;

    public static final String EVENT_MODEL_FETCH_FINISHED = "cz.binarytrio.molescope.MODEL_KEEPER_FETCH_OK";
    public static final String EVENT_MODEL_FETCH_FAIL = "cz.binarytrio.molescope.MODEL_KEEPER_FETCH_FAIL";
    public static final String EVENT_MODEL_FETCH_PROGRESS = "cz.binarytrio.molescope.MODEL_KEEPER_FETCH_PROGRESS";
    public static final String EVENT_MODEL_ATTRIBUTES_OBTAINED = "cz.binarytrio.molescope.MODEL_KEEPER_ATTRIBUTES_OBTAINED";

    public ModelKeeperService() {super("MolescopeModelKeeper");}
    public ModelKeeperService(String name) {super(name);}

    @Override
    protected void onHandleIntent(Intent workIntent) { try {
        int action = workIntent.getIntExtra(ATTR_KEY_1, Helper.UNDEFINED);
        switch (action) {
            case ACTION_CHECK_VERSION:
                CloudFile modelFile = Helper.getAFSFile(MoleApp.STORAGE_CONNECTION_STRING, MoleApp.SHARE_NAME, MoleApp.MODEL_NAME);
                sendBroadcast(new Intent(EVENT_MODEL_ATTRIBUTES_OBTAINED)
                        .putExtra(ATTR_KEY_1, Helper.getRemoteModelVersion(modelFile))
                        .putExtra(ATTR_KEY_2, Helper.getRemoteModelSize(modelFile)));
                break;
            case ACTION_FETCH_MODEL:
                Helper.fetchModelInteractively(
                        MoleApp.STORAGE_CONNECTION_STRING, MoleApp.SHARE_NAME, MoleApp.MODEL_NAME,
                        851 * Helper.KB, Helper.getLocalModelStorage(this, MoleApp.MODEL_NAME, true), MoleApp.VERSIONFILE_EXTENSION, this);
                break;
            default:
                Helper.log("unexpected action " + action + " in ModelKeeperService");
        }
    } catch (Exception e) {sendBroadcast(new Intent(EVENT_MODEL_FETCH_FAIL).putExtra(ATTR_KEY_1, e.toString()));}}

    @Override
    public void onAttributesObtained(long versionNumber, long downloadSizeB) {
        sendBroadcast(new Intent(EVENT_MODEL_ATTRIBUTES_OBTAINED)
                .putExtra(ATTR_KEY_1, versionNumber)
                .putExtra(ATTR_KEY_2, downloadSizeB));
    }

    @Override
    public void onDownloadProgress(float progressPercentage, long speedBpS) {
        sendBroadcast(new Intent(EVENT_MODEL_FETCH_PROGRESS)
                .putExtra(ATTR_KEY_1, progressPercentage)
                .putExtra(ATTR_KEY_2, speedBpS));
    }

    @Override
    public void onDownloadFinished(long durationMillis) {
        sendBroadcast(new Intent(EVENT_MODEL_FETCH_FINISHED)
                .putExtra(ATTR_KEY_1, durationMillis));
    }

    @Override
    public void onDownloadError(String exception) {
        sendBroadcast(new Intent(EVENT_MODEL_FETCH_FAIL)
                .putExtra(ATTR_KEY_1, exception));
    }
}
