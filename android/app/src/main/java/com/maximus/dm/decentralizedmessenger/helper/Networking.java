package com.maximus.dm.decentralizedmessenger.helper;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by Maximus on 18/02/2016.
 */
public class Networking {

    public static final String SERVER_DOMAIN = "https://dm.john-kevin.me";
    public static final String SERVER_PATH_LOGIN = "/login";
    public static final String SERVER_PATH_REGISTER = "/register";
    public static final String SERVER_PATH_GET_FRIENDS = "/getFriends";
    public static final String SERVER_PATH_ADD_FRIEND = "/addFriend";
    public static final String SERVER_PATH_ADD_FRIEND_NAME = "/addFriendName";
    public static final String SERVER_PATH_REFRESH_TOKEN = "/refreshToken";
    public static final String SERVER_PATH_CONFIRM_FRIEND = "/confirmFriend";
    public static final String SERVER_PATH_CANCEL_FRIEND_REQUEST = "/cancelFriend";
    public static final String SERVER_PATH_MESSAGE = "/message";
    public static final String SERVER_PATH_MESSAGES = "/messages";
    public static final String SERVER_PATH_SEARCH = "/search";

    private Context mContext;

    public Networking(Context context) {
        mContext = context;
    }

    // Use this method to connect
    public String connect(String path, String urlParams) {
        NetworkingAsync n = new NetworkingAsync();
        String returned = "";
        try {
            returned = n.execute(path, urlParams).get();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return returned;
    }

    private class NetworkingAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return getJsonFromServer(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private String getJsonFromServer(String path, String urlParams) {
        InputStream caInput = null;

        String fullPath = SERVER_DOMAIN + path;
        //System.out.println("FULLPATH: " + fullPath + " PARAMS: " + urlParams);
        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            caInput = new BufferedInputStream(mContext.getAssets().open("ca.crt"));
            Certificate ca;

            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            URL url2 = new URL(fullPath);
            HttpsURLConnection urlConnection =
                    (HttpsURLConnection)url2.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Write to connection
            DataOutputStream dos =  new DataOutputStream(urlConnection.getOutputStream());
            dos.writeBytes(urlParams);
            dos.flush();
            dos.close();

            // Get response
            InputStream isr = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(isr));
            String line = "";
            StringBuffer response = new StringBuffer();

            // while there are lines to read
            while((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                caInput.close();
            } catch(Exception e) {}
        }
        return null;
    }

}
