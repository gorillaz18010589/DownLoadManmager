package com.example.downloadmanmager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private Button btnUpdate;
    private int WRITE_EXTERNAL_PERMISSION = 200;
    private long enqueueId;
    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    private DownloadManager downloadManager;
    private  Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        btnUpdate = findViewById(R.id.btnUpadte);
        btnUpdate.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnUpadte:
                    if (
                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            }, WRITE_EXTERNAL_PERMISSION);
                        }


                    } else {
                        downLoadFile("test.apk", "https://www.gdaily.org/22200/netflix-apk");
                    }
                    break;
            }
    };




    };
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WRITE_EXTERNAL_PERMISSION){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
//                downLoadFile("test","https://www.gdaily.org/22200/netflix-apk");
                downLoadFile("test.apk","https://m.apkpure.com/tw/line-free-calls-messages/jp.naver.line.android/download?from=details");
            }else {
                Toast.makeText(MainActivity.this,"權限失敗:" , Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downLoadFile(String file, String url) {
        Uri uri = Uri.parse(url);
         downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        try {
            if(downloadManager != null){
                DownloadManager.Request request = new DownloadManager.Request(uri);
                /**設定用於下載時的網路狀態*/
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setTitle("下載apk");
                request.setAllowedOverMetered(true);
                /**設定漫遊狀態下是否可以下載*/
                request.setAllowedOverRoaming(true);
                request.setDescription("下載" + file +"中...");
//                request.setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory().getAbsolutePath(),file);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS ,file);
//                request.setMimeType(gerMimeType(uri));
                request.setMimeType("application/vnd.android.package-archive");
                enqueueId = downloadManager.enqueue(request);

                registerReceiver(myBroadcastReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//                Toast.makeText(MainActivity.this,"下載成功:",Toast.LENGTH_SHORT).show();
            }else {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        }catch (Exception e){
            Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_SHORT).show();

        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            long downloadCompletedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,0);

            if(downloadCompletedId == enqueueId){
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(enqueueId);
                cursor = downloadManager.query(query);

                if(cursor.moveToFirst()){
                    //取得COLUMN_STATUS
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                    switch (status){
                        //下载暂停
                        case DownloadManager.STATUS_PAUSED:
                            break;
                        //下载延迟
                        case DownloadManager.STATUS_PENDING:
                            break;
                        //正在下载
                        case DownloadManager.STATUS_RUNNING:
                            break;
                        //下载完成
                        case DownloadManager.STATUS_SUCCESSFUL:
                            //下载完成安装APK
                            installAPK();
//                            get();
                            break;
                        //下载失败
                        case DownloadManager.STATUS_FAILED:
                            Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                cursor.close();
            }
        }
    }

    private void installAPK() {
        Toast.makeText(MainActivity.this,"下載完成" ,Toast.LENGTH_SHORT).show();

            //获取下载文件的Uri
            Uri downloadFileUri = downloadManager.getUriForDownloadedFile(enqueueId);
        Log.d("hank","downloadFileUri:" + downloadFileUri);
            if (downloadFileUri != null) {
                Intent intent= new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
                startActivity(intent);
                unregisterReceiver(myBroadcastReceiver);
            }


    }


    private void get(){
        String fileName = cursor.getString(0);//承接我的代码，filename指获取到了我的文件相应路径
        if (fileName != null) {
            if (fileName.endsWith(".apk")) {
                if(Build.VERSION.SDK_INT>=24) {//判读版本是否在7.0以上
                    File file= new File(fileName);
                    Uri apkUri = FileProvider.getUriForFile(
                            MainActivity.this
                            , "com.example.downloadmanmager"
                            , file);//在AndroidManifest中的android:authorities值
                    Log.d("hank","apkUri:" + apkUri +"/file:" + file.getAbsolutePath().toString());
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                    install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    startActivity(install);
                } else{
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(install);
                }
            }
        }

    }


}