package com.hjq.demo.chat.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.hjq.demo.utils.Trace;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密用户的登录名和密码
 */
public class CryptoUtil {

    private static final String TAG = "EncryptionUtils";

    private static final String SHARED_PREF_NAME = "encrypted_data";
    private static final String KEY_USERNAME = "encrypted_username";
    private static final String KEY_PASSWORD = "encrypted_password";
    private static final String KEY_IV = "KEY_IV";

    // AES encryption parameters
    private static final int KEY_SIZE = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int IV_LENGTH = 16;

    private static final String ENCRYPTION_KEY = "bndg.cn.smart.chat"; // Change this to your own encryption key

    public static void encryptAndSaveCredentials(Context context, String username, String password) {
        try {
            // Generate random IV
            byte[] iv = generateIV();

            // Generate encryption key from ENCRYPTION_KEY
            SecretKey secretKey = generateSecretKey(ENCRYPTION_KEY);

            // Encrypt username and password
            byte[] encryptedUsername = encrypt(username.getBytes(), secretKey, iv);
            byte[] encryptedPassword = encrypt(password.getBytes(), secretKey, iv);

            // Convert encrypted data and IV to Base64 strings
            String base64EncryptedUsername = Base64.encodeToString(encryptedUsername, Base64.DEFAULT);
            String base64EncryptedPassword = Base64.encodeToString(encryptedPassword, Base64.DEFAULT);
            String base64IV = Base64.encodeToString(iv, Base64.DEFAULT);
            SPUtils.getInstance().put(KEY_IV, base64IV);
            SPUtils.getInstance().put(KEY_USERNAME, base64EncryptedUsername);
            SPUtils.getInstance().put(KEY_PASSWORD, base64EncryptedPassword);
        } catch (Exception e) {
            Trace.e(TAG, "Error encrypting and saving credentials: " + e.getMessage());
        }
    }

    public static String[] decryptCredentials(Context context) {
        Log.d(">>>>", "decryptCredentials: start");
        try {
            String base64EncryptedUsername = SPUtils.getInstance().getString(KEY_USERNAME, "").toString();
            String base64EncryptedPassword = SPUtils.getInstance().getString(KEY_PASSWORD, "").toString();

            if (!TextUtils.isEmpty(base64EncryptedUsername) && !TextUtils.isEmpty(base64EncryptedPassword)) {
                // Decode Base64 strings
                byte[] encryptedUsername = Base64.decode(base64EncryptedUsername, Base64.DEFAULT);
                byte[] encryptedPassword = Base64.decode(base64EncryptedPassword, Base64.DEFAULT);

                // Decode IV
                byte[] iv = Base64.decode(SPUtils.getInstance().getString(KEY_IV, "").toString(), Base64.DEFAULT);

                // Generate encryption key from ENCRYPTION_KEY
                SecretKey secretKey = generateSecretKey(ENCRYPTION_KEY);

                // Decrypt username and password
                String username = new String(decrypt(encryptedUsername, secretKey, iv));
                String password = new String(decrypt(encryptedPassword, secretKey, iv));
                Log.d(">>>>", "decryptCredentials: end");
                return new String[]{username, password};
            }
        } catch (Exception e) {
            Trace.e(TAG, "Error decrypting credentials: " + e.getMessage());
        }
        return null;
    }

    public static String[] decryptCredentials2() {
        Log.d(">>>>", "decryptCredentials: start");
        try {
            String base64EncryptedUsername = SPUtils.getInstance().getString(KEY_USERNAME, "").toString();
            String base64EncryptedPassword = SPUtils.getInstance().getString(KEY_PASSWORD, "").toString();
            if (!TextUtils.isEmpty(base64EncryptedUsername) && !TextUtils.isEmpty(base64EncryptedPassword)) {
                // Decrypt username and password
                String username = new String(decrypt(base64EncryptedUsername, ENCRYPTION_KEY));
                String password = new String(decrypt(base64EncryptedPassword, ENCRYPTION_KEY));
                Log.d(">>>>", "decryptCredentials: end");
                return new String[]{username, password};
            }
        } catch (Exception e) {
            Trace.e(TAG, "Error decrypting credentials: " + e.getMessage());
        }
        return null;
    }

    public static String encrypt(String plaintext, String key) {
        try {
            Cipher cipher = Cipher.getInstance("RC4");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "RC4");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String ciphertext, String key) {
        try {
            Cipher cipher = Cipher.getInstance("RC4");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "RC4");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.decode(ciphertext, Base64.DEFAULT));
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void encryptAndSaveCredentials2(String username, String password) {
        String encryptedUsername = encrypt(username, ENCRYPTION_KEY);
        String encryptedPassword = encrypt(password, ENCRYPTION_KEY);
        SPUtils.getInstance().put(KEY_USERNAME, encryptedUsername);
        SPUtils.getInstance().put(KEY_PASSWORD, encryptedPassword);
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private static SecretKey generateSecretKey(String encryptionKey) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(encryptionKey.toCharArray(), new byte[16], ITERATION_COUNT, KEY_SIZE);
        SecretKey secretKey = factory.generateSecret(spec);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    private static byte[] encrypt(byte[] data, SecretKey secretKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    private static byte[] decrypt(byte[] encryptedData, SecretKey secretKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(encryptedData);
    }


    /**
     * 对字符串简单加密
     *
     * @param data
     * @return
     */
    public static String simpleEncrypt(String data) {
        if (TextUtils.isEmpty(data)) {
            return data;
        } else {
            return encrypt(data, ENCRYPTION_KEY);
        }
    }

    public static String simpleDecrypt(String data) {
        if (TextUtils.isEmpty(data)) {
            return data;
        } else {
            return decrypt(data, ENCRYPTION_KEY);
        }
    }
}