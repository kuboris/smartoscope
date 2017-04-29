package cz.binarytrio.molescope.util;

/*
 * Created by nicko on 4/19/17
 */


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cz.binarytrio.molescope.R;
import cz.binarytrio.molescope.listener.AFSDownloadListener;
import cz.binarytrio.molescope.listener.OkListener;
import cz.binarytrio.molescope.listener.YNListener;

public class Helper {

    public static final String TAG = Helper.class.getCanonicalName();
    public static final int UNDEFINED = -1;

    public static final int KB = 1024;
    public static final int MB = 1024 * KB;

    private static final int MINUTE = 60;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;

    private static final boolean DEBUG = true;
    private static final DateFormat VERSION_FORMAT = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#0.0");
    private static final DecimalFormat VERSION_FORMAT_DECIMAL = new DecimalFormat("00");

    public static void fetchModelInteractively(String storageConnectionString, String shareName, String remoteModelName, long bufferSize, String modelStorage, String versionFileExtension, AFSDownloadListener listener) {
        try {
            long downloadTime = System.currentTimeMillis();
            CloudFile modelFile = getAFSFile(storageConnectionString, shareName, remoteModelName);
            long modelVersion = getRemoteModelVersion(modelFile);
            long modelSize = getRemoteModelSize(modelFile);
            listener.onAttributesObtained(modelVersion, modelSize);
            int blocks = (int) Math.ceil((double)modelSize/bufferSize);
            log("Downloading " + describeSize(modelSize) + " file in "+blocks+" blocks of "+describeSize(bufferSize)+" each...");
            FileOutputStream localModelStream = new FileOutputStream(modelStorage);

            long blockTime;
            for (int i=0; i<blocks; i++) {
                blockTime = System.currentTimeMillis();
                modelFile.downloadRange(bufferSize*i, (long) bufferSize, localModelStream);
                blockTime = System.currentTimeMillis()-blockTime;
                log("\t["+(i+1)+"/"+blocks+"] done in " + blockTime + " ms");
                if (i!=blocks-1) listener.onDownloadProgress((i+1)*bufferSize*100/modelSize, bufferSize*1000/blockTime);
//                if (i==20) break;
            }

//            create version file
            new DataOutputStream(new FileOutputStream(modelStorage+versionFileExtension)).writeLong(modelVersion);

            downloadTime = System.currentTimeMillis()-downloadTime;
            System.out.println("Download done in " + downloadTime/1000 + "s (" + describeSize(modelSize*1000/downloadTime) + "/s)");
            listener.onDownloadFinished(downloadTime);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onDownloadError(e.toString());
        }
    }

    public static long getLocalModelVersion(String modelStorage, String versionFileExtension) {
        try {
            File versionFile = new File(modelStorage+versionFileExtension);
            if (versionFile.exists())
                return new DataInputStream(new FileInputStream(versionFile)).readLong();
        } catch (Exception e) { e.printStackTrace(); }
        return UNDEFINED;
    }

    public static String getLocalModelStorage(Context context, String modelPrefix, boolean writeRequired) {
        //TODO handle more special cases
        File internalStorage = new File(context.getFilesDir(), modelPrefix);
        boolean accessToExternal = writeRequired?isExternalStorageWritable():isExternalStorageReadable();
        File storage;
        if (accessToExternal) {
            File externalStorage = new File(context.getExternalFilesDir(null), modelPrefix);
            storage = (externalStorage.exists() || !internalStorage.exists())?externalStorage:internalStorage;
        } else storage = internalStorage;
        return storage.getAbsolutePath();
    }

    public static void showYNDialog(Context context, int title, int message, final YNListener listener) {showYNDialog(context, context.getString(title), context.getString(message), listener);}
    public static void showYNDialog(Context context, String title, String message, final YNListener listener) {
        final DialogInterface.OnClickListener _listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { switch (which){
                    case DialogInterface.BUTTON_POSITIVE: listener.yes(); break;
                    case DialogInterface.BUTTON_NEGATIVE: listener.no(); break;
            }}};

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.yes, _listener)
                .setNegativeButton(R.string.no, _listener)
                .show();
    }

    public static void showOkDialog(Context context, int title, int message) {showOkDialog(context, context.getString(title), context.getString(message));}
    public static void showOkDialog(Context context, String title, String message) {
        showOkDialog(context, title, message, new OkListener() {
            @Override
            public void ok() {}});
    }
    public static void showOkDialog(Context context, String title, String message, final OkListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.ok();
                    }
                })
                .show();
    }

    public static void log(String... msgs) {
        if (DEBUG) for (String msg: msgs) Log.d(TAG, msg);
    }

    public static boolean hasInternet(Context context) {
        NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static long getRemoteModelVersion(CloudFile AFSFile) {
        long time = AFSFile.getProperties().getLastModified().getTime();
        log("got version raw " + time + ", formatted " + describeVersion(time));
        return time;
    }

    public static long getRemoteModelSize(CloudFile AFile) {
        return AFile.getProperties().getLength();
    }

    public static CloudFile getAFSFile(String connection, String share, String file) throws StorageException, URISyntaxException, InvalidKeyException {
        CloudStorageAccount account = CloudStorageAccount.parse(connection);
        CloudFileClient client = account.createCloudFileClient();
        CloudFileShare cloudShare = client.getShareReference(share);
        CloudFileDirectory rootDir = cloudShare.getRootDirectoryReference();
        CloudFile modelFile = rootDir.getFileReference(file);
        modelFile.downloadAttributes(); // determine file size
        return modelFile;
    }

    public static String describeSize(long downloadSizeB) {
        if (downloadSizeB>=MB) return SIZE_FORMAT.format((double) downloadSizeB/MB) + " MB";
        else if (downloadSizeB>=KB) return SIZE_FORMAT.format((double) downloadSizeB/KB) + " KB";
        else return downloadSizeB + " B";
    }

    public static String describeSpeed(long speedBpS) {
        return describeSize(speedBpS) + "/S";
    }

    public static String describeVersion(long versionNumber) {
        Calendar cv = Calendar.getInstance();
        cv.setTimeInMillis(versionNumber);
        return VERSION_FORMAT_DECIMAL.format(cv.get(Calendar.YEAR)%100)
             + VERSION_FORMAT_DECIMAL.format(cv.get(Calendar.MONTH))
             + VERSION_FORMAT_DECIMAL.format(cv.get(Calendar.DAY_OF_MONTH));
    }

    public static String describeTime(long seconds) {return describeTime(seconds,2);}
    private static String describeTime(long seconds, int level) {
        if (level>0) {
            if (seconds>DAY) return seconds/DAY + "d " + describeTime(seconds%DAY, level-1);
            else if (seconds>HOUR) return seconds/HOUR + "h " + describeTime(seconds%HOUR, level-1);
            else if (seconds>MINUTE) return seconds/MINUTE + "m " + describeTime(seconds%MINUTE, level-1);
            else return seconds + "s";
        } else return "";
    }
}
