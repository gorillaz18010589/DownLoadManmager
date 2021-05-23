package com.example.downloadmanmager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.util.List;

public class PackageManagerUtils {
    public PackageManagerUtils() {
    }

    /**
     * 获取当前设备上安装的所有App
     */
    public List<PackageInfo> getAllApp(Context context){
        PackageManager pm = context.getPackageManager();
        List<PackageInfo>  packageInfos = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        return packageInfos;
    }

    /**
     * 判断 App 是否安装
     */
    public boolean isInstalled (Context context,String packageName){
        if(packageName == null || packageName.length() < 1){
            return false;
        }
        PackageManager pm = context.getPackageManager();
        try{
            PackageInfo packageInfo = pm.getPackageInfo(packageName,PackageManager.GET_ACTIVITIES);
            return packageInfo != null;
        }catch(Throwable ignore){

        }

        return false;
    }

    /**
     * 根据包名获取 PackageInfo
     */
    public PackageInfo getPackageInfo (Context context,String packageName){
        if(packageName == null || packageName.length() < 1){
            return null;
        }
        PackageManager pm = context.getPackageManager();
        try{
            PackageInfo packageInfo = pm.getPackageInfo(packageName,PackageManager.GET_ACTIVITIES);
            return packageInfo ;
        }catch(Throwable ignore){

        }

        return null;
    }

    /**
     * 根据包名获取 版本号
     */
    public int getPackageVersionCode (Context context,String packageName){
        PackageInfo packageInfo = getPackageInfo (context, packageName);
        if(packageName != null){
            return packageInfo.versionCode;
        }

        return-1;
    }

    /**
     * 根据包名获取 版本名
     */
    public String getPackageVersionName (Context context,String packageName){
        PackageInfo packageInfo = getPackageInfo (context, packageName);
        if(packageName != null){
            return packageInfo.versionName;
        }

        return null;
    }

    /**
     * 获取 App名
     */
    public String getApplicationLabel (Context context,String packageName){
        if(packageName == null || packageName.length() < 1){
            return null;
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = getPackageInfo (context, packageName);
        if(packageInfo != null){
            return packageInfo.applicationInfo.loadLabel(pm).toString();
        }
        return null;
    }

    /**
     * 获取 App的 icon
     */
    public Drawable getApplicationIcon (Context context, String packageName){
        if(packageName == null || packageName.length()< 1){
            return null;
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = getPackageInfo (context, packageName);
        if(packageInfo != null){
            return packageInfo.applicationInfo.loadIcon(pm);
        }
        return null;
    }

    /**
     * 通过Apk路径，获取Apk信息
     */
    public PackageInfo getPackageArchiveInfo (Context context, String apkPath){
        try{
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath,PackageManager.GET_ACTIVITIES);
            return packageInfo;
        }catch(Throwable ignore){
            return null;
        }
    }

    public static boolean isUpgrade(String oldVersion, String newVersion) {
        if (newVersion == null || TextUtils.isEmpty(newVersion)) {
            newVersion = "0";
        }
        String tempOldVersion = oldVersion.replace(".", "");
        String tempNewVersion = newVersion.replace(".", "");
        if (tempOldVersion.length() < tempNewVersion.length()) {
            int fillNumber = tempNewVersion.length() - tempOldVersion.length();
            for (int i = 0; i < fillNumber; i++) {
                tempOldVersion = tempOldVersion + "0";
            }
        } else if (tempOldVersion.length() > tempNewVersion.length()) {
            int fillNumber = tempOldVersion.length() - tempNewVersion.length();
            for (int i = 0; i < fillNumber; i++) {
                tempNewVersion = tempNewVersion + "0";
            }
        }
        int intOldVersion = Integer.parseInt(tempOldVersion);
        int intNewVersion = Integer.parseInt(tempNewVersion);
        if (intOldVersion >= intNewVersion) {
            return false;
        } else {
            return true;
        }
    }

}
