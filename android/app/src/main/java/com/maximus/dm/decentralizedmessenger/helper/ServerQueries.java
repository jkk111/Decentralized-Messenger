package com.maximus.dm.decentralizedmessenger.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Maximus on 29/02/2016.
 */
public class ServerQueries {

    private static final String TAG = "ServerQueries";

    private Context mContext;

    public ServerQueries(Context context) {
        mContext = context;
    }

    // FRIENDS
    // friend request using an id
    public boolean sendFriendRequest(int senderId, String token, int clientId, String secret) {
        JSONObject jsonObject = null;
        boolean responseReturn = false;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("sender", senderId);
            jsonObject.put("token", token);
            jsonObject.put("client", clientId);
            jsonObject.put("secret", secret);

            Log.d(TAG, "sendFriendRequest, jsonToUrl, " + Encoder.jsonToUrl(jsonObject) + ", path " + Networking.SERVER_PATH_ADD_FRIEND);

            Networking networking = new Networking(mContext);
            String response = networking.connect(Networking.SERVER_PATH_ADD_FRIEND, Encoder.jsonToUrl(jsonObject));
            if (response != null) {
                jsonObject = new JSONObject(response);
                Log.d(TAG, "sendFriendRequest, response " + response);
                responseReturn = jsonObject.getBoolean("success");
            }

            //Log.d(TAG, "sendFriendRequest, response " + response);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return responseReturn;
    }

    public boolean confirmFriend(boolean response, String token, int senderId, int friendshipId) {
        JSONObject jsonObject = null;
        boolean success = false;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("response", response);
            jsonObject.put("token", token);
            jsonObject.put("sender", senderId);
            jsonObject.put("friendshipId", friendshipId);

            Networking networking = new Networking(mContext);
            String serverResponse = networking.connect(Networking.SERVER_PATH_CONFIRM_FRIEND, Encoder.jsonToUrl(jsonObject));
            jsonObject = new JSONObject(serverResponse);
            Log.d(TAG, "friendRequestResponse, sent " + response + ",response " + serverResponse);
            success = jsonObject.getBoolean("success");
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return success;
    }

    public JSONArray getFriendList(int senderId, String token) {
        Object jsonReceived = null;

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sender", senderId);
            jsonObject.put("token", token);

            Log.d(TAG, "getFriendList, jsonToUrl, " + Encoder.jsonToUrl(jsonObject));

            Networking networking = new Networking(mContext);
            String strResponse = networking.connect(Networking.SERVER_PATH_GET_FRIENDS, Encoder.jsonToUrl(jsonObject));
            Log.d(TAG, "getFriendList, " + strResponse);

            if (strResponse != null) {
                jsonReceived = new JSONTokener(strResponse).nextValue();
            }
            if (jsonReceived instanceof JSONObject) {
                // Received object which is an error
                return null;
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return (JSONArray) jsonReceived;
    }

    public boolean cancelFriend(int friendshipId, int senderId, String token) {
        JSONObject jsonObject = null;
        boolean methodReturn = false;
        try {

            jsonObject = new JSONObject();
            jsonObject.put("friendshipId", friendshipId);
            jsonObject.put("sender", senderId);
            jsonObject.put("token", token);

            Networking networking = new Networking(mContext);
            String serverResponse = networking.connect(Networking.SERVER_PATH_CANCEL_FRIEND_REQUEST, Encoder.jsonToUrl(jsonObject));
            if (serverResponse != null) {
                jsonObject = new JSONObject(serverResponse);
            }

            if (jsonObject.has("success")) {
                if (jsonObject.getBoolean("success") == true) {
                    // Request cancelled successfully
                    methodReturn = true;
                }
            }

            Log.d(TAG, "getMessages, response: " + serverResponse);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return methodReturn;
    }

    // MESSAGES
    public JSONObject getMessages(int senderId, String token) {
        JSONObject jsonObject = null;
        String serverResponse = "";
        try {

            jsonObject = new JSONObject();
            jsonObject.put("sender", senderId);
            jsonObject.put("token", token);

            Networking networking = new Networking(mContext);
            serverResponse = networking.connect(Networking.SERVER_PATH_MESSAGES, Encoder.jsonToUrl(jsonObject));
            if (serverResponse != null) {
                jsonObject = new JSONObject(serverResponse);
            }

        } catch(JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getMessages, response: " + serverResponse);
        return jsonObject;
    }

    public boolean sendMessage(int senderId, String token, int destId, String message, String publicKeySender, String publicKeyRecipient) {
        JSONObject jsonObject = null;
        String serverResponse = "";
        boolean success = false;

        try {
            Encrypt encrypt = new Encrypt();
            String messageSender;
            String messageRecipient;

            messageSender = encrypt.encrypt(message, publicKeySender);
            messageRecipient = encrypt.encrypt(message, publicKeyRecipient);

            Log.d(TAG, "sendMessage publicKeySender " + publicKeySender);
            String testEnc = "Hello World!";
//            testEnc = encrypt.encrypt(testEnc, publicKeySender);
//            testEnc = encrypt.decrypt(testEnc, publicKeySender);
//            Log.d(TAG, "sendMessage testEnc " + testEnc);

            Log.d(TAG, "sendMessage, message " + message + " publicKeySender " + publicKeySender + " publicKeyRecepient " + publicKeyRecipient);

            jsonObject = new JSONObject();
            jsonObject.put("sender", senderId);
            jsonObject.put("token", token);
            jsonObject.put("dest", destId);
            jsonObject.put("messageSender", messageSender);
            jsonObject.put("messageRecipient", messageRecipient);

            Networking networking = new Networking(mContext);
            serverResponse = networking.connect(Networking.SERVER_PATH_MESSAGE, Encoder.jsonToUrl(jsonObject));
            Log.d(TAG, "sendMessage, response: " + serverResponse);
            if (serverResponse != null) {
                jsonObject = new JSONObject(serverResponse);
                success = jsonObject.getBoolean("success");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    // SEARCH
    public JSONArray search(int senderId, String token, String query) {
        JSONObject jsonObject = null;
        JSONArray resultArray = null;
        String serverResponse = "";
        try {

            jsonObject = new JSONObject();
            jsonObject.put("sender", senderId);
            jsonObject.put("token", token);
            jsonObject.put("query", query);

            Networking networking = new Networking(mContext);
            serverResponse = networking.connect(Networking.SERVER_PATH_SEARCH, Encoder.jsonToUrl(jsonObject));
            if (serverResponse != null) {
                jsonObject = new JSONObject(serverResponse);
            }
            resultArray = jsonObject.getJSONArray("users");

        } catch(JSONException e) {
            e.printStackTrace();
        }

        return resultArray;
    }

}
