package com.bndg.smack;

import android.content.Context;

import org.jivesoftware.smack.util.SslContextFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.bndg.smack.utils.SmartTrace;

public class SSLHelper implements SslContextFactory {
    private final Context context;

    public SSLHelper(Context context) {
        this.context = context;
    }

    @Override
    public SSLContext createSslContext() {
        // Load the PEM certificate file from resources (you may adjust the path)
        InputStream inputStream = context.getResources().openRawResource(R.raw.abc);
        SSLContext sslContext = null;
        try {
            // Create a CertificateFactory
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inputStream);
            // Create a KeyStore containing the trusted certificate
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("server", cert);
            // Create a TrustManager that trusts the certificates in our KeyStore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            // Create an SSLContext that uses our TrustManager
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
        SmartTrace.d("SSLContext = " + sslContext);
        return sslContext;
    }

    public SSLContext sslContext() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            return sc;
        } catch (KeyManagementException e) {
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static final String KEYSTORE_PASSWORD = "111111";
    private static final String KEYSTORE_FILE_NAME = "abcd.p12"; // Replace with your Keystore file name

    public static KeyStore getKeyStore(InputStream inputStream) throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12"); // Assuming your Keystore type is PKCS12
        keystore.load(inputStream, KEYSTORE_PASSWORD.toCharArray());
        return keystore;
    }

    public static KeyStore getKeyStoreFromResources(Context context) throws Exception {
        // Load Keystore file from resources (res/raw/tigase4.bndg.cn.p12)
        InputStream inputStream = context.getResources().openRawResource(R.raw.abcd);
        return getKeyStore(inputStream);
    }

    public static KeyStore getKeyStoreFromAssets(Context context) throws Exception {
        // Load Keystore file from assets (assets/tigase4.bndg.cn.p12)
        InputStream inputStream = context.getAssets().open(KEYSTORE_FILE_NAME);
        return getKeyStore(inputStream);
    }
}
