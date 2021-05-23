package com.example.downloadmanmager;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtils {
    /**
     * 判斷文件是否存在
     *
     * @param file 文件
     * @return {@code true}: 存在{@code false}: 不存在
     */
    public static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }

    /**
     * 判斷文件是否存在，不存在則判斷是否創建成功
     *
     * @param file 文件
     * @return true:存在或創建成功，false:不存在或創建失敗
     */
    public static boolean createOrExistsFile(final File file) {
        if (file == null) return false;
        // 如果存在，是文件則返回 true，是目錄則返回 false
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判斷目錄是否存在，不存在則判斷是否創建成功
     *
     * @param file 文件
     * @return true:存在或創建成功，false:不存在或創建失敗
     */
    public static boolean createOrExistsDir(final File file) {
        // 如果存在，是目錄則返回 true，是文件則返回 false，不存在則返回是否創建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 判斷目錄是否存在，不存在則判斷是否創建成功
     *
     * @param dirPath 目錄路徑
     * @return true:存在或創建成功，false:不存在或創建失敗
     */
    public static boolean createOrExistsDir(final String dirPath) {
        Log.d("UPDATE@@@", "createOrExistsDir string: " + dirPath);
        boolean b = createOrExistsDir(getFileByPath(dirPath));
        Log.d("UPDATE@@@", "createOrExistsDir string: " + b);
        return b;
    }

    /**
     * 根據文件路徑獲取文件
     *
     * @param filePath 文件路徑
     * @return 文件
     */
    public static File getFileByPath(final String filePath) {
        Log.d("UPDATE@@@", "@@@getFileByPath string: " + filePath);
        Log.d("UPDATE@@@", "@@@getFileByPath isSpace: " + isSpace(filePath));
        return isSpace(filePath) ? null : new File(filePath);
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
//    PATH_CHALLENGE_VIDEO
//
//    public void persistImage(Bitmap bitmap, String name) {
//        String filePath = "";
//        if (FileUtils.createOrExistsDir(PATH_CHALLENGE_VIDEO)) {
//            filePath = PATH_CHALLENGE_VIDEO + File.separator + name;
//        }
//        if (TextUtils.isEmpty(filePath)) {
//            Log.e("UPDATE@@@", "downloadVideo: 存儲路徑為空");
//            return;
//        }
//        File file = new File(filePath);
//        if (!FileUtils.isFileExists(file) && FileUtils.createOrExistsFile(file)) {
//            OutputStream os;
//            try {
//                os = new FileOutputStream(file);
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
//                os.flush();
//                os.close();
//            } catch (Exception e) {
//                Log.e("File", "Error writing bitmap", e);
//            }
//        }
//    }
}
