package cz.binarytrio.molescope.listener;

/**
 * Created by nicko on 4/24/17
 */

public interface AFSDownloadListener {
    public void onAttributesObtained(long versionNumber, long downloadSizeB);
    public void onDownloadProgress(float progressPercentage, long speedBpS);
    public void onDownloadFinished(long durationMillis);
    public void onDownloadError(String exception);
}
