package com.xuqiqiang.uikit.requester;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.xuqiqiang.uikit.utils.code.Md5;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE;
import static com.xuqiqiang.uikit.requester.ActivityRequester.DESTROY_ON_PAUSE;

@SuppressWarnings("unused")
public class DownloadRequester {

    private static final Map<Long, DownloadBean> mEventMap = new HashMap<>();
    private static final ScheduledExecutorService mScheduledExecutorService = Executors.newScheduledThreadPool(3);
    private static DownloadManager downloadManager;

    public static long download(Context context, String url, final DownloadListener listener) {
        return download(context, url, null, null, listener);
    }

    public static long download(Context context, String url, String fileName, final DownloadListener listener) {
        return download(context, url, null, fileName, listener);
    }

    public static long download(Context context, String url, String dirPath, String fileName, final DownloadListener listener) {
        if (TextUtils.isEmpty(url)) return -1;
        File file;
        fileName = !TextUtils.isEmpty(fileName) ? fileName : getFileName(url);
        if (!TextUtils.isEmpty(dirPath)) {
            file = new File(dirPath, fileName);
        } else {
            if (PermissionRequester.checkPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            } else {
                file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
            }
        }
        return download(context, new DownloadManager.Request(Uri.parse(url)), file, listener);
    }

    @Deprecated
    public static long download(Context context, Properties properties, final DownloadListener listener) {
        if (properties == null) return -1;
        if (TextUtils.isEmpty(properties.url)) return -1;

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(properties.url));

        request.setTitle(properties.title);
        request.setDescription(properties.description);
        request.setMimeType(properties.mimeType);
        request.setAllowedNetworkTypes(properties.allowedNetworkTypes);
        request.setNotificationVisibility(properties.notificationVisibility);
        request.setVisibleInDownloadsUi(properties.isVisibleInDownloadsUi);
        request.setAllowedOverRoaming(properties.roamingAllowed);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(properties.meteredAllowed);
        }

        File file;
//        String filePath;
        if (!TextUtils.isEmpty(properties.path)) {
            if (!properties.pathAsDirectory) file = new File(properties.path);
            else file = new File(properties.path, properties.getName());
        } else {
            if (PermissionRequester.checkPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), properties.getName());
            } else {
                file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), properties.getName());
            }
        }
        return download(context, request, file, listener);
    }

    /**
     * Enqueue a new download. The download will start automatically once the download manager is
     * ready to execute it and connectivity is available.
     *
     * @return an ID for the download, unique across the system.  This ID is used to make future
     * calls related to this download.
     */
    public static long download(Context context, DownloadManager.Request request, File file, DownloadListener listener) {
        Context appContext = context.getApplicationContext();
        if (downloadManager == null)
            downloadManager = (DownloadManager) appContext.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) return -1;
        long downloadId = downloadManager.enqueue(request);

        request.setDestinationUri(Uri.fromFile(file));

        DownloadEvent event = new DownloadEvent(downloadId, file.getPath(), listener);
        ScheduledFuture<?> scheduledFuture = mScheduledExecutorService.scheduleAtFixedRate(
                event, 0, 1, TimeUnit.SECONDS);
        DownloadBean bean = new DownloadBean(downloadId, scheduledFuture);
        mEventMap.put(downloadId, bean);

        if (context instanceof Activity)
            ActivityRequester.postOnDestroyed((Activity) context, DESTROY_ON_PAUSE, new CancelEvent(downloadId));
        return downloadId;
    }

    public static void cancel(Context context, long downloadId) {
        DownloadBean bean = mEventMap.remove(downloadId);
        if (bean != null)
            bean.future.cancel(false);
        if (downloadManager == null)
            downloadManager = (DownloadManager) context.getApplicationContext()
                    .getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) return;
        downloadManager.remove(downloadId);
    }

    public static String getFileName(String url) {
        int start = url.lastIndexOf("/");
        int end = url.indexOf("?", start);
        if (end < 0)
            end = url.length();
        if (start >= 0 && end >= 0 && start + 1 < end) {
            return url.substring(start + 1, end);
        } else {
//            return Hex.encodeHexStr(url.getBytes());
            return Md5.getStringMD5(url);
        }
    }

    public static int[] getBytesAndStatus(long downloadId) {
        int[] bytesAndStatus = new int[]{-1, -1, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return bytesAndStatus;
    }

    public interface DownloadListener {
        void onComplete(String filePath);
    }

    public static class CancelEvent implements Runnable {

        private final long downloadId;

        public CancelEvent(long downloadId) {
            this.downloadId = downloadId;
        }

        @Override
        public void run() {
            downloadManager.remove(downloadId);
            DownloadBean bean = mEventMap.remove(downloadId);
            if (bean != null)
                bean.future.cancel(false);
        }
    }

    public static class DownloadEvent implements Runnable {

        private final long downloadId;
        private final String filePath;
        private final DownloadListener listener;

        public DownloadEvent(long downloadId, String filePath, DownloadListener listener) {
            this.downloadId = downloadId;
            this.filePath = filePath;
            this.listener = listener;
        }

        @Override
        public void run() {
            int[] bytesAndStatus = getBytesAndStatus(downloadId);
            int currentSize = bytesAndStatus[0];//当前大小
            int totalSize = bytesAndStatus[1];//总大小
            int status = bytesAndStatus[2];//下载状态
//            Logger.d("checkStatus getBytesAndStatus", currentSize + "", totalSize + "", status + "");
            //noinspection StatementWithEmptyBody
            if (status == DownloadManager.STATUS_PENDING) { //下载延迟
            } else if (status == DownloadManager.STATUS_RUNNING) { //正在下载
                DownloadBean bean = mEventMap.get(downloadId);
                if (bean != null) {
                    if (bean.status != DownloadManager.STATUS_RUNNING) {
                        bean.status = DownloadManager.STATUS_RUNNING;
                        if (listener instanceof DownloadAdapter)
                            ((DownloadAdapter) listener).onStart(totalSize);
                    } else {
                        if (listener instanceof DownloadAdapter)
                            ((DownloadAdapter) listener).onProcess(totalSize, currentSize);
                    }
                }

            } else if (status == DownloadManager.STATUS_PAUSED) { //下载暂停
                DownloadBean bean = mEventMap.get(downloadId);
                if (bean != null) {
                    if (bean.status != DownloadManager.STATUS_PAUSED) {
                        bean.status = DownloadManager.STATUS_PAUSED;
                        if (listener instanceof DownloadAdapter)
                            ((DownloadAdapter) listener).onPaused();
                    }
                }
            } else {
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    if (listener != null)
                        listener.onComplete(filePath);
                } else if (status == DownloadManager.STATUS_FAILED) {
                    if (listener instanceof DownloadAdapter)
                        ((DownloadAdapter) listener).onError();
                }
                DownloadBean bean = mEventMap.remove(downloadId);
                if (bean != null)
                    bean.future.cancel(false);
            }
        }
    }

    public abstract static class DownloadAdapter implements DownloadListener {
        public void onStart(long size) {
        }

        public void onError() {
        }

        public void onPaused() {
        }

        public void onProcess(long size, long downloadedSize) {
        }

        @Override
        public void onComplete(String filePath) {
        }
    }

    private static class DownloadBean {
        long downloadId;
        ScheduledFuture<?> future;
        int status = DownloadManager.STATUS_PENDING;

        public DownloadBean(long downloadId, ScheduledFuture<?> future) {
            this.downloadId = downloadId;
            this.future = future;
        }
    }

    public static class Properties implements Serializable {
        private static final long serialVersionUID = 1L;
        String url;
        String path;
        boolean pathAsDirectory;
        CharSequence title;
        CharSequence description;
        String mimeType;
        boolean roamingAllowed = true;
        boolean meteredAllowed = true;
        int allowedNetworkTypes = ~0; // default to all network types allowed
        int notificationVisibility = VISIBILITY_VISIBLE;
        boolean isVisibleInDownloadsUi = true;
        private String name;

        public static Properties build() {
            return new Properties();
        }

        public Properties url(String url) {
            this.url = url;
            return this;
        }

        public Properties path(String path, boolean pathAsDirectory) {
            this.path = path;
            this.pathAsDirectory = pathAsDirectory;
            return this;
        }

        public Properties name(String name) {
            this.name = name;
            return this;
        }

        public Properties title(String title) {
            this.title = title;
            return this;
        }

        public Properties description(String description) {
            this.description = description;
            return this;
        }

        public Properties mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        /**
         * 移动网络情况下是否允许漫游
         */
        public Properties roamingAllowed(boolean roamingAllowed) {
            this.roamingAllowed = roamingAllowed;
            return this;
        }

        public Properties meteredAllowed(boolean meteredAllowed) {
            this.meteredAllowed = meteredAllowed;
            return this;
        }

        public Properties allowedNetworkTypes(int allowedNetworkTypes) {
            this.allowedNetworkTypes = allowedNetworkTypes;
            return this;
        }

        public Properties notificationVisibility(int notificationVisibility) {
            this.notificationVisibility = notificationVisibility;
            return this;
        }

        public Properties isVisibleInDownloadsUi(boolean isVisibleInDownloadsUi) {
            this.isVisibleInDownloadsUi = isVisibleInDownloadsUi;
            return this;
        }

        public String getName() {
            if (TextUtils.isEmpty(name)) return getFileName(url);
            return name;
        }
    }
}