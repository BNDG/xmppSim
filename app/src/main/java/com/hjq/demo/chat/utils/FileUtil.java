package com.hjq.demo.chat.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import com.blankj.utilcode.util.FileUtils;
import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.entity.ChatVideoBean;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.Trace;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.bndg.smack.enums.SmartContentType;

public class FileUtil {

    /**
     * 存储语音消息到缓存文件夹
     *
     * @param context
     * @param voiceFileUrl
     * @return
     */
    public static File getVoiceFile(Context context, String voiceFileUrl) {
        // 获取应用的内部存储目录
        File appDirectory = context.getFilesDir();
        // 创建一个名为 "voice" 的子目录
        File voiceDirectory = new File(appDirectory, "voice");
        if (!voiceDirectory.exists()) {
            voiceDirectory.mkdirs(); // 创建目录及其父目录
        }
        // 从 URL 中提取文件名
        String fileName = voiceFileUrl.substring(voiceFileUrl.lastIndexOf('/') + 1);
        // 去除文件名中的扩展名
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            fileName = fileName.substring(0, dotIndex);
        }
        // 创建一个 .wav 文件
        fileName += ".wav";
        return new File(voiceDirectory, fileName);
    }

    /**
     * 根据URI获取文件真实路径（兼容多张机型）
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getFilePathByUri(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {

            int sdkVersion = Build.VERSION.SDK_INT;
            if (sdkVersion >= 19) { // api >= 19
                return getRealPathFromUriAboveApi19(context, uri);
            } else { // api < 19
                return getRealPathFromUriBelowAPI19(context, uri);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String type = documentId.split(":")[0];
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                filePath = getDataColumn(context, contentUri, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            } else if (isExternalStorageDocument(uri)) {
                // ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else {
                //Log.e("路径错误");
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static List<String> uploadFile(String serverUrl, String filePath) {
        List<String> imageList = new ArrayList<>();
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try {
            URL url = new URL(serverUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            /* 允许Input、Output，不使用Cache */
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            /* 设置传送的method=POST */
            con.setRequestMethod("POST");
            /* setRequestProperty */
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            /* 设置DataOutputStream */
            DataOutputStream ds =
                    new DataOutputStream(con.getOutputStream());
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; " +
                    "name=\"file\";filename=\"" +
                    fileName + "\"" + end);
            ds.writeBytes(end);
            /* 取得文件的FileInputStream */
            FileInputStream fStream = new FileInputStream(filePath);
            /* 设置每次写入1024bytes */
            int bufferSize = 10240;
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            /* 从文件读取数据至缓冲区 */
            while ((length = fStream.read(buffer)) != -1) {
                /* 将资料写入DataOutputStream中 */
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
            /* close streams */
            fStream.close();
            ds.flush();
            /* 取得Response内容 */
            InputStream is = con.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            try {
                imageList = JsonParser.getListFromJson(b.toString(), String.class);
            } catch (Exception e) {
                e.printStackTrace();
                imageList = new ArrayList<>();
            }
            /* 关闭DataOutputStream */
            ds.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageList;
    }

    public static boolean copyUriToFile(Context context, Uri uri, File file) throws IOException {
        // 创建一个目录
        // 复制数据到文件
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Failed to open input stream from URI");
        }
        OutputStream outputStream = new FileOutputStream(file);
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } finally {
            // 关闭流
            inputStream.close();
            outputStream.close();
        }
        return true;
    }

    public static ChatVideoBean buildVideoMessage(String path) {
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            mmr.setDataSource(path);
            String sDuration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            Bitmap bitmap = mmr.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_NEXT_SYNC);

            if (bitmap == null) {
                Trace.e("buildVideoMessage() bitmap is null");
                return null;
            }
            String bitmapPath = FileUtil.getVideoCoverFile().getAbsolutePath();
            boolean result = saveBitmap(bitmapPath, bitmap);
            if (!result) {
                Trace.e("build video message, save bitmap failed.");
                return null;
            }
            String videoPath = path;
            int imgWidth = bitmap.getWidth();
            int imgHeight = bitmap.getHeight();
            long duration = Long.valueOf(sDuration);
            ChatVideoBean msg = new ChatVideoBean();
            msg.setThumbnailLocalPath(bitmapPath);
            msg.setVideoLocalPath(videoPath);
            msg.setDuration(duration);
            msg.setThumbnailWidth(imgWidth);
            msg.setThumbnailHeight(imgHeight);
            msg.msgType = SmartContentType.VIDEO;
            return msg;
        } catch (Exception ex) {
            Trace.e("MediaMetadataRetriever exception " + ex);
        } finally {
            try {
                mmr.release();
            } catch (IOException e) {
                Trace.e("MediaMetadataRetriever exception " + e);
            }
        }
        return null;
    }

    /**
     * 通过jid获取用户头像文件
     *
     * @return
     */
    public static File getVideoCoverFile() {
        File avatarDir = new File(ActivityManager.getInstance().getApplication().getFilesDir(), "Thumbnail");
        FileUtils.createOrExistsDir(avatarDir);
        return new File(avatarDir, System.nanoTime() + "_" + Math.abs(new Random().nextInt()) + ".jpg");
    }

    public static boolean saveBitmap(String path, Bitmap b) {
        try {
            FileOutputStream fout = new FileOutputStream(path);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static File getThumbnailFile(Context context, String voiceFileUrl) {
        // 获取应用的内部存储目录
        File appDirectory = context.getFilesDir();
        // 创建一个名为 "voice" 的子目录
        File voiceDirectory = new File(appDirectory, "thumbnail");
        if (!voiceDirectory.exists()) {
            voiceDirectory.mkdirs(); // 创建目录及其父目录
        }
        // 从 URL 中提取文件名
        String fileName = voiceFileUrl.substring(voiceFileUrl.lastIndexOf('/') + 1);
        // 去除文件名中的扩展名
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            fileName = fileName.substring(0, dotIndex);
        }
        // 创建一个 .wav 文件
        fileName += ".jpg";
        return new File(voiceDirectory, fileName);
    }

    /**
     * 将音视频通话时长，转换为时间格式
     *
     * @param time 通话时长，单位秒
     */
    public static String formatCallTime(int time) {

        int hour = time / 3600;
        int minute = (time % 3600) / 60;
        int second = time % 60;
        if (hour == 0) {
            return String.format(Locale.CHINA, "%02d:%02d", minute, second);
        }
        return String.format(Locale.CHINA, "%02d:%02d:%02d", hour, minute, second);
    }

    public static String getFileExtension(String filename) {
        if (!TextUtils.isEmpty(filename)) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1 && dot < filename.length() - 1) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1000) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1000000) {
            fileSizeString = df.format((double) fileS / 1000) + "KB";
        } else if (fileS < 1000000000) {
            fileSizeString = df.format((double) fileS / 1000000) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1000000000) + "GB";
        }
        return fileSizeString;
    }

    public static boolean fileSizeLimit(long fileS) {
        long limit = getFileLimitSize();
        if (limit < 0) {
            return false;
        }
        long limitSize = limit * 1000000L;
        return fileS > limitSize;
    }

    public static Long getFileLimitSize() {
        long limit = Constant.FILE_LIMIT;
        return limit;
    }

    private static final Map<String, Integer> fileRes = getFileTypeMap();

    public static int getFileIcon(String type) {
        if (!TextUtils.isEmpty(type)) {
            String lowerType = type.toLowerCase(Locale.getDefault());
            Integer result = fileRes.get(lowerType);
            return result != null ? result : R.drawable.ic_unknown_file;
        }
        return R.drawable.ic_unknown_file;
    }

    public static String getUrlFileName(Context context, Uri uri) {
        String displayName = "";
        try {
            Cursor resolver = context.getContentResolver().query(uri, null, null, null, null, null);
            resolver.moveToFirst();
            int nameIndex = resolver.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            displayName = "";
            if (nameIndex >= 0) {
                displayName = resolver.getString(nameIndex);
                Trace.d("ChatUtils", "pick file result uri(" + displayName + ") -->> " + uri);
            }
            resolver.close();
        } catch (Exception e) {
        }
        return displayName;
    }

    public static String getUrlFileSize(Context context, Uri uri) {
        Cursor resolver = context.getContentResolver().query(uri, null, null, null, null, null);
        resolver.moveToFirst();
        int sizeIndex = resolver.getColumnIndex(OpenableColumns.SIZE);
        String displaySize = "0";
        if (sizeIndex >= 0) {
            displaySize = resolver.getString(sizeIndex);
            Trace.d("ChatUtils", "pick file result uri(" + displaySize + ") -->> " + uri);
        }
        resolver.close();

        return displaySize;
    }

    private static Map<String, Integer> getFileTypeMap() {
        Map<String, Integer> fileRes = new HashMap<>();
        fileRes.put("doc", R.drawable.ic_word_file);
        fileRes.put("docx", R.drawable.ic_word_file);
        fileRes.put("xls", R.drawable.ic_excel_file);
        fileRes.put("xlsx", R.drawable.ic_excel_file);
        fileRes.put("ppt", R.drawable.ic_ppt_file);
        fileRes.put("pptx", R.drawable.ic_ppt_file);
        //    fileRes.put("keynote", R.drawable.ic_ppt_file);
        fileRes.put("jpg", R.drawable.ic_image_file);
        fileRes.put("png", R.drawable.ic_image_file);
        fileRes.put("jpeg", R.drawable.ic_image_file);
        //    fileRes.put("psd", R.drawable.ic_image_file);
        fileRes.put("tiff", R.drawable.ic_image_file);
        fileRes.put("gif", R.drawable.ic_image_file);
        fileRes.put("zip", R.drawable.ic_rar_file);
        fileRes.put("7z", R.drawable.ic_rar_file);
        fileRes.put("tar", R.drawable.ic_rar_file);
        fileRes.put("rar", R.drawable.ic_rar_file);
        fileRes.put("pdf", R.drawable.ic_pdf_file);
        fileRes.put("rtf", R.drawable.ic_pdf_file);
        fileRes.put("txt", R.drawable.ic_text_file);
        fileRes.put("csv", R.drawable.ic_excel_file);
        fileRes.put("html", R.drawable.ic_html_file);
        fileRes.put("mp4", R.drawable.ic_video_file);
        fileRes.put("avi", R.drawable.ic_video_file);
        fileRes.put("wmv", R.drawable.ic_video_file);
        fileRes.put("mpeg", R.drawable.ic_video_file);
        fileRes.put("m4v", R.drawable.ic_video_file);
        fileRes.put("mov", R.drawable.ic_video_file);
        fileRes.put("asf", R.drawable.ic_video_file);
        fileRes.put("flv", R.drawable.ic_video_file);
        fileRes.put("f4v", R.drawable.ic_video_file);
        fileRes.put("rmvb", R.drawable.ic_video_file);
        fileRes.put("rm", R.drawable.ic_video_file);
        fileRes.put("3gp", R.drawable.ic_video_file);
        //    fileRes.put("vob", R.drawable.ic_video_file);
        fileRes.put("mp3", R.drawable.ic_mp3_file);
        fileRes.put("aac", R.drawable.ic_mp3_file);
        fileRes.put("wav", R.drawable.ic_mp3_file);
        fileRes.put("wma", R.drawable.ic_mp3_file);
        //    fileRes.put("cda", R.drawable.ic_mp3_file);
        fileRes.put("flac", R.drawable.ic_mp3_file);
        fileRes.put("unknown", R.drawable.ic_unknown_file);
        fileRes.put("", R.drawable.ic_unknown_file);
        return fileRes;
    }

}
