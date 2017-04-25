package cz.binarytrio.molescope.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cz.binarytrio.molescope.listener.AFSDownloadListener;

/**
 * Created by nicko on 4/25/17
 */

public class ModelKeeperStateReceiver extends BroadcastReceiver {

    private AFSDownloadListener mListener;
    
    public ModelKeeperStateReceiver(AFSDownloadListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (intent.getAction()) {
            case ModelKeeperService.EVENT_MODEL_ATTRIBUTES_OBTAINED:
                mListener.onAttributesObtained(
                        intent.getLongExtra(ModelKeeperService.ATTR_KEY_1, Helper.UNDEFINED),
                        intent.getLongExtra(ModelKeeperService.ATTR_KEY_2, Helper.UNDEFINED));
                return;
            case ModelKeeperService.EVENT_MODEL_FETCH_FAIL:
                mListener.onDownloadError(
                        intent.getStringExtra(ModelKeeperService.ATTR_KEY_1));
                return;
            case ModelKeeperService.EVENT_MODEL_FETCH_FINISHED:
                mListener.onDownloadFinished(
                        intent.getLongExtra(ModelKeeperService.ATTR_KEY_1, Helper.UNDEFINED));
                return;
            case ModelKeeperService.EVENT_MODEL_FETCH_PROGRESS:
                mListener.onDownloadProgress(
                        intent.getFloatExtra(ModelKeeperService.ATTR_KEY_1, Helper.UNDEFINED),
                        intent.getFloatExtra(ModelKeeperService.ATTR_KEY_2, Helper.UNDEFINED));
                return;
            default:
                Helper.log("unexpected action " + action + " in ModelKeeperService");
        }
    }
}
