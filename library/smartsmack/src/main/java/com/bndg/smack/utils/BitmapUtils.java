package com.bndg.smack.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;

import org.jivesoftware.smack.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BitmapUtils {
    private static BitmapUtils instance;

    private BitmapUtils() {
    }

    public static BitmapUtils getInstance() {
        if (null == instance) {
            synchronized (BitmapUtils.class) {
                instance = new BitmapUtils();
            }
        }
        return instance;
    }

    public static byte[] getURIBytes(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        if (inputStream == null) {
            throw new IOException("Failed to open input stream from URI");
        }

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * 将Bitmap转换成InputStream(压缩率quality、100表示不压缩、10表示压缩90%)
     */
    public InputStream Bitmap2InputStream(Bitmap bm, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    /**
     * 将Bitmap转换成InputStream
     *
     * @param bm
     * @return
     */
    public InputStream Bitmap2InputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    /**
     * 将InputStream转换成Bitmap
     *
     * @param is
     * @return
     */
    public Bitmap InputStream2Bitmap(InputStream is) {
        return BitmapFactory.decodeStream(is);
    }

    /**
     * Drawable转换成InputStream
     *
     * @param d
     * @return
     */
    public InputStream Drawable2InputStream(Drawable d) {
        Bitmap bitmap = this.drawable2Bitmap(d);
        return this.Bitmap2InputStream(bitmap);
    }

    /**
     * InputStream转换成Drawable
     *
     * @param is
     * @return
     */
    public Drawable InputStream2Drawable(InputStream is) {
        Bitmap bitmap = this.InputStream2Bitmap(is);
        return this.bitmap2Drawable(bitmap);
    }

    /**
     * Drawable转换成byte[]
     *
     * @param d
     * @return
     */
    public byte[] Drawable2Bytes(Drawable d) {
        Bitmap bitmap = this.drawable2Bitmap(d);
        return this.Bitmap2Bytes(bitmap);
    }

    /**
     * byte[]转换成Drawable
     *
     * @param b
     * @return
     */
    public Drawable Bytes2Drawable(byte[] b) {
        Bitmap bitmap = this.Bytes2Bitmap(b);
        return this.bitmap2Drawable(bitmap);
    }

    /**
     * Bitmap转换成byte[]
     *
     * @param bm
     * @return
     */
    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * byte[]转换成Bitmap
     *
     * @param b
     * @return
     */
    public Bitmap Bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return null;
    }

    /**
     * 将byte[]转换成InputStream
     *
     * @param b
     * @return
     */
    public InputStream Byte2InputStream(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        return bais;
    }

    /**
     * 将InputStream转换成byte[]
     *
     * @param is
     * @return
     */
    public byte[] InputStream2Bytes(InputStream is) {
        String str = "";
        byte[] readByte = new byte[1024];
        int readCount = -1;
        try {
            while ((readCount = is.read(readByte, 0, 1024)) != -1) {
                str += new String(readByte).trim();
            }
            return str.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Drawable转换成Bitmap
     *
     * @param drawable
     * @return
     */
    public Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Bitmap转换成Drawable
     *
     * @param bitmap
     * @return
     */
    public Drawable bitmap2Drawable(Bitmap bitmap) {
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        Drawable d = (Drawable) bd;
        return d;
    }

    /**
     * 将Bitmap转换成Base64
     *
     * @param bit
     * @return
     */
    public String getImgStr(Bitmap bit) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 100, bos);//参数100表示不压缩
        byte[] bytes = bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * 将Base64转换成bitmap
     *
     * @param str
     * @return
     */
    public Bitmap getimg(String str) {
        byte[] bytes;
        bytes = Base64.decode(str, 0);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 文件转字节
     *
     * @param file file
     * @return byte[]
     * @throws IOException
     */
    public static byte[] getFileBytes(File file) throws IOException {
        long fileLength = file.length();
        if (fileLength > Integer.MAX_VALUE) {
            throw new IOException("File is too large");
        }
        int bytes = (int) fileLength;
        byte[] buffer = new byte[bytes];

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            int readBytes = bis.read(buffer);
            if (readBytes != bytes) {
                throw new IOException("Entire file not read");
            }
        }

        return buffer;
    }

    public static String getAvatarHash(byte[] imageData) {
        byte[] imageHash = null;
        String avatarHash = null;

        try {
            if (imageData != null) {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
                imageHash = messageDigest.digest(imageData);
            }
        } catch (NoSuchAlgorithmException ex) {
        }

        if (imageHash != null)
            avatarHash = StringUtils.encodeHex(imageHash);

        return avatarHash;
    }

    /**
     * 获取图片的宽高和类型
     * @param imageFile
     * @return 0 宽，1 高，2 类型
     */
    public static String[] getImageMetaInfo(File imageFile) {
        if (!imageFile.exists()) {
            return null;
        }
        String[] metaInfo = new String[3];
        // 使用 BitmapFactory 获取图片的宽高
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 只获取图片的宽高，不加载图片内容

        // 解码图片，获取宽高
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // 获取图片类型
        String mimeType = getMimeType(imageFile);
        // 输出图片的宽度、高度和类型
        metaInfo[0] = String.valueOf(options.outWidth);
        metaInfo[1] = String.valueOf(options.outHeight);
        metaInfo[2] = mimeType;
        return metaInfo;
    }

    // 获取文件的 MIME 类型
    public static String getMimeType(File file) {
        String extension = getFileExtension(file);
        switch (extension.toLowerCase()) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }

    // 获取文件扩展名
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
}
