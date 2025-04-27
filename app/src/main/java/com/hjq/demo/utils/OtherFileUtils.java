package com.hjq.demo.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;


import com.hjq.demo.R;
import com.hjq.demo.manager.ActivityManager;


import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description：
 * Created by kang on 2018/3/9.
 */

public class OtherFileUtils {
    /**
     * 判断文件是否存在
     *
     * @param file 文件
     * @return {@code true}: 存在<br>{@code false}: 不存在
     */
    public static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsFile(final File file) {
        if (file == null) return false;
        // 如果存在，是文件则返回 true，是目录则返回 false
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
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(final File file) {
        // 如果存在，是目录则返回 true，是文件则返回 false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param dirPath 目录路径
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(final String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    public static File getFileByPath(final String filePath) {
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

    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(File file) {
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    /**
     * 10MB以上文件不预览
     *
     * @param totalSize
     * @return
     */
    public static boolean isBigFile(int totalSize) {
        return totalSize >= 10 * 1024 * 1024;
    }

    /**
     * 判断SDCard是否可用
     */
    public static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取data/data/包名/files 路径
     * /data/user/0/
     *
     * @return
     */
    public static String getDataFilesPath() {
        return ActivityManager.getInstance().getTopActivity().getFilesDir().getPath();
    }

    public static String getFileName(String filePath) {
        if (isFileExists(new File(filePath))) {
            return filePath.substring(filePath.lastIndexOf("/"), filePath.length());
        }
        return "";
    }

    /**
     * 取文件名称
     *
     * @param str
     * @return
     */
    public static String getFileNameByString(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        int dotIndex = str.lastIndexOf(".");
        String fileName;

        if (dotIndex != -1) {
            fileName = str.substring(0, dotIndex);
        } else {
            fileName = str;
        }
        return fileName;
    }

    /**
     * 取文件名称
     *
     * @param url
     * @return
     */
    public static String getFileNameByUrl(String url, String query) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        // 定义正则表达式
        Pattern pattern = Pattern.compile(query + "([^&]+)");
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(url);
        // 查找匹配的内容
        if (matcher.find()) {
            // 提取 filename 的值
            String filename = matcher.group(1);
            return filename;
        }
        return url;
    }

    /**
     * 取文件扩展名
     *
     * @param fileUrl
     * @return
     */
    public static String getFileExtensionByString(String fileUrl) {
        if (TextUtils.isEmpty(fileUrl)) {
            return "";
        }
        return fileUrl
                .substring(fileUrl.lastIndexOf(".") + 1);
    }

    /**
     * 取文件图标
     *
     * @param fileUrl
     * @return
     */
    public static int getFileImageResource(String fileUrl) {
        String end = getFileExtensionByString(fileUrl);
        if (end.equals("doc") || end.equals("docx")) {
            return R.drawable.icon_file_word;
        } else if (end.equals("xls") || end.equals("xlsx")) {
            return R.drawable.icon_file_excel;
        } else if (end.equals("ppt") || end.equals("pptx")) {
            return R.drawable.icon_file_ppt;
        } else if (end.equals("pdf")) {
            return R.drawable.icon_file_pdf;
        } else if (end.equals("txt") || end.equals("java")
                || end.equals("json") || end.equals("cpp") || end.equals("htm")
                || end.equals("html") || end.equals("php") || end.equals("jsp")) {
            return R.drawable.icon_file_txt;
        } else {
            return R.drawable.icon_file_default;
        }

    }

    public static String getFileMd5Name(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(filePath.getBytes());
            return new BigInteger(1, md.digest()).toString(32);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Save image to the SD card
     *
     * @param photoBitmap
     * @param photoName
     * @param path
     */
    public static String savePhoto(Bitmap photoBitmap, String path,
                                   String photoName) {
        String localPath = null;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File photoFile = new File(path, photoName + ".png");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100,
                            fileOutputStream)) {
                        localPath = photoFile.getPath();
                        fileOutputStream.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                localPath = null;
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
                localPath = null;
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                        fileOutputStream = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return localPath;
    }

    public static String getFileMd5NameByString(String room) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] messageDigest = md.digest(room.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
    }
}