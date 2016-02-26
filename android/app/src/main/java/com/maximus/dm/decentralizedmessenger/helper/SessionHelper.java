package com.maximus.dm.decentralizedmessenger.helper;

import android.content.Context;
import android.util.Log;

import com.maximus.dm.decentralizedmessenger.User.UserDatabase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Maximus on 24/02/2016.
 */
public class SessionHelper {

    //TODO: This is a test class, if works, move it somewhere else!!!

    Context mContext;
    private static final String TAG = "SessionHelper";

    UserDatabase userDatabase;

    public SessionHelper(Context context) {
        mContext = context;
    }

    public boolean refreshToken() {
        userDatabase = new UserDatabase(mContext);
        String token = userDatabase.getToken();

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("token", token);

            Networking networking = new Networking(mContext);
            String response = networking.connect(Networking.SERVER_PATH_REFRESH_TOKEN, Encoder.jsonToUrl(jsonObject));

            Log.d(TAG, "refreshToken, response " + response);
            //userDatabase.setToken(token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
