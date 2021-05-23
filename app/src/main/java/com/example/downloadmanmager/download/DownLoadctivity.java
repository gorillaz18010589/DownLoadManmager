package com.example.downloadmanmager.download;
//目的:DownLoadManger 下載檔案,搭配進度條跟RecyclerView
//1.Ui畫面配置
//2.adapter設定
//3.DialogUi
//12.搜尋 byte to human readable 把讀出的Byte數據轉成可讀的
//13.Realm支持加密，格式化查询，流式API，JSON，数据变更通知等等。

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.mbms.DownloadRequest;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.downloadmanmager.DeleteFileUtil;
import com.example.downloadmanmager.FileUtils;
import com.example.downloadmanmager.MainActivity;
import com.example.downloadmanmager.R;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownLoadctivity extends AppCompatActivity {
    private Button btnDownLoadList;
    private RecyclerView recyclerView;
    private DownLoadAdapter downLoadAdapter;
    private List<DownLoadModel> downLoadModels = new ArrayList<>();
    public final static String TAG = "hank";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_loadctivity);

        init();
        btnDownLoadList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });
    }

    private void init() {
        //7.註冊廣播
        registerReceiver(onCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        btnDownLoadList = findViewById(R.id.btnDownLoadList);
        recyclerView = findViewById(R.id.rv);

        downLoadAdapter = new DownLoadAdapter(DownLoadctivity.this, downLoadModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(downLoadAdapter);

        DeleteFileUtil.delete("/storage/emulated/0/Download/weixin_800-8.apk");
    }


    private void showInputDialog() {
        AlertDialog.Builder alertBuilderDialog = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.input_dialog, null);
        alertBuilderDialog.setView(view);

        EditText editInput = view.findViewById(R.id.editInput);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);


        //按下送出按url按鍵
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                CharSequence charSequence = clipboardManager.getPrimaryClip().getItemAt(0).getText();
                editInput.setText(charSequence);
            }
        });

        //按下下載按鈕
        alertBuilderDialog.setPositiveButton("下載", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                downloadFile(editInput.getText().toString());
                downloadFile("http://gdown.baidu.com/data/wisegame/8d5889f722f640c8/weixin_800.apk");
            }
        });

        //按下取消按鈕
        alertBuilderDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                cancel();
            }
        });

        alertBuilderDialog.show();

    }

    //1.下載檔案彈窗
    private void downloadFile(String url) {
        //設訂檔案名稱跟路徑
        String fileName = URLUtil.guessFileName(url, null, null);
        String downloadPate = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File file = new File(downloadPate, fileName);

        Log.d("hank", "downloadFile() +url:" + url + "/fileName:" + fileName);


        //設定Request
        DownloadManager.Request request = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(fileName);
            request.setDescription("Download..");
            request.setAllowedOverRoaming(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setDestinationUri(Uri.fromFile(file));
            request.setAllowedOverMetered(true);
//            request.setRequiresCharging(false); //是否需要插入設備才能下載
        } else {
            request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(fileName);
            request.setDescription("Download..");
            request.setAllowedOverRoaming(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setDestinationUri(Uri.fromFile(file));
            request.setAllowedOverMetered(true);
        }

        //
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        //設定第一筆資料
        DownLoadModel downLoadModel = new DownLoadModel();
        downLoadModel.setId(11);
        downLoadModel.setStatus("Downloading");
        downLoadModel.setTitle(fileName);
        downLoadModel.setFile_size("0");
        downLoadModel.setProgress("0");
        downLoadModel.setPaused(false);
        downLoadModel.setDownloadId(downloadId);
        downLoadModel.setFile_path("");


        downLoadModels.add(downLoadModel);
        downLoadAdapter.notifyItemChanged(downLoadModels.size() - 1);

        //9.執行續物件實體化
        DownloadStatusTask downloadStatusTask = new DownloadStatusTask(downLoadModel);

        //11.執行任務
        runTask(downloadStatusTask, "" + downloadId);
    }

    //2.進度條更新執行續
    public class DownloadStatusTask extends AsyncTask<String, String, String> {
        private DownLoadModel downLoadModel;

        public DownloadStatusTask(DownLoadModel downLoadModel) {
            this.downLoadModel = downLoadModel;
        }

        @Override
        protected String doInBackground(String... strings) {
            downloadFileProcess(strings[0]);
            return null;
        }


        //3.
        private void downloadFileProcess(String downloadId) {
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            boolean downloading = true;

            while (downloading) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(Long.parseLong(downloadId));
                Cursor cursor = downloadManager.query(query);
                cursor.moveToFirst();

                int byte_downloads = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int total_size = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));


                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false;
                    //下載完成
                    goToApp(downloadManager,Long.parseLong(downloadId));
                }

                int progress = (int) ((byte_downloads * 100L) / total_size);
                String status = getStatusMessage(cursor);
                publishProgress(new String[]{String.valueOf(progress), String.valueOf(byte_downloads), status});
                cursor.close();
                Log.d(TAG, "downloading-> byte_downloads:" + byte_downloads + "/total_size:" + total_size + "/progress:" + progress);

            }
        }

        //5.每次更新時
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            this.downLoadModel.setFile_size(bytesIntoHumanReadable(Long.parseLong(values[1]))); //12.解析可讀的Byte
            this.downLoadModel.setProgress(values[0]);
            if (!this.downLoadModel.getStatus().equalsIgnoreCase("PAUSE") && !this.downLoadModel.getStatus().equalsIgnoreCase("RESUME")) {
                this.downLoadModel.setStatus(values[2]);
            }


            //每次更新adapter的getDownloadId,資料
            downLoadAdapter.changeItem(downLoadModel.getDownloadId());
        }
    }


    //4.取得資料狀態
    private String getStatusMessage(Cursor cursor) {
        String msg = "-";

        switch (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            //下载暂停
            case DownloadManager.STATUS_PAUSED:
                msg = "Paused";
                break;
            //下载延迟
            case DownloadManager.STATUS_PENDING:
                msg = "Pending";
                break;
            //正在下载
            case DownloadManager.STATUS_RUNNING:
                msg = "Running";
                Log.v("hank", "下載中..");
                break;
            //下载完成
            case DownloadManager.STATUS_SUCCESSFUL:
                //下载完成安装APK
                msg = "Completed";
                Log.v("hank", "下載中..");
                break;
            //下载失败
            case DownloadManager.STATUS_FAILED:
                msg = "Fail";
                break;
        }

        return msg;
    }


    //6.註冊完成的廣播監聽
    BroadcastReceiver onCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long completeId = intent.getLongExtra(DownloadManager.ACTION_DOWNLOAD_COMPLETE, -1);
            boolean comp = downLoadAdapter.changeItemWithStatus("Complete", completeId);


            //如果apk下載成功
            if (comp) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(completeId);
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                Cursor cursor = downloadManager.query(query);
                cursor.moveToFirst();

                //取得下載url
                String downloaded_path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                downLoadAdapter.setChangeItemFilePath(downloaded_path, completeId);


            }
        }
    };

    //8.關閉時取消廣播註冊
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onCompleteReceiver);
    }

    //10.跑執行續方法
    public void runTask(DownloadStatusTask downloadStatusTask, String id) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                downloadStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{id});
            } else {
                downloadStatusTask.execute(new String[]{id});
            }

        } catch (Exception e) {

        }
    }


    //將byte資料轉成可讀性
    private String bytesIntoHumanReadable(long bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;
        long terabyte = gigabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return (bytes / gigabyte) + " GB";

        } else if (bytes >= terabyte) {
            return (bytes / terabyte) + " TB";

        } else {
            return bytes + " Bytes";
        }
    }


    //打開應用apk頁面下載
    public void goToApp(DownloadManager downloadManager ,long enqueueId) {
//        Toast.makeText(DownLoadctivity.this, "下載成功:", Toast.LENGTH_SHORT).show();

        Uri downloadFileUri = downloadManager.getUriForDownloadedFile(enqueueId); // content://downloads/all_downloads/1496
        Log.d("hank", "downloadFileUri:" + downloadFileUri); // /storage/emulated/0/Download/test-9.apk
        String fileName = getRealFilePath(DownLoadctivity.this, downloadFileUri);
        Log.d("hank", "fileName:" + fileName);

        if (fileName != null) {
            if (fileName.endsWith(".apk")) {
                if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
                    File file = new File(fileName);
                    Uri apkUri = FileProvider.getUriForFile(
                            DownLoadctivity.this,
                            "com.example.downloadmanmager",
                            file);//在AndroidManifest中的android:authorities值

                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                    install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    startActivity(install);

                } else {

                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(install);
                }
            }
        }
    }

    //將content:// url -> 轉換真實路徑
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }



//        {"data":{
//            "appname": "hoolay.apk",
//                    "serverVersion": "1.0.2",
//                    "serverFlag": "1",
//                    "lastForce" : "1",
//                    "updateurl": "http://releases.b0.upaiyun.com/hoolay.apk",
//                    "upgradeinfo": "V1.0.2版本更新，你想不想要试一下哈！！！"
//        },
//            "error_code":"200","error_msg" :"蛋疼的认识"}

}