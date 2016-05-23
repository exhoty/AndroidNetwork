package com.huanchi.lightpay.network.ssl;

import com.huanchi.lightpay.util.ApplicationUtil;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by odin on 5/22/16.
 */
public class DefaultSSLSocketFactory {

    private static SSLSocketFactory socketFactory;

    public static SSLSocketFactory getDefault() {
        if (socketFactory == null){
            try {
                InputStream in  = ApplicationUtil.getCertificate();
                Certificate ca = CertificateFactory.getInstance("X.509").generateCertificate(in);
                in.close();

                String keystoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keystoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory mg = TrustManagerFactory.getInstance(defaultAlgorithm);
                mg.init(keyStore);


                SSLContext tls = SSLContext.getInstance("TLS");
                tls.init(null, mg.getTrustManagers(), null);
                socketFactory = tls.getSocketFactory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return socketFactory;
    }
}
