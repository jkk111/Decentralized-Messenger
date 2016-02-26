package com.maximus.dm.decentralizedmessenger.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Created by Maximus on 18/02/2016.
 */
public class Encoder {

    // Use this method to fully convert json to urlEncoded
    public static String jsonToUrl(JSONObject jsonObject) {
        String urlEncodedObject = jsonToURLEncoding(jsonObject);
        String  noSpaces = replaceWhitespaces(urlEncodedObject);
        return replacePluses(noSpaces);
    }

    private static String jsonToURLEncoding(JSONObject json) {
        String output = "";
        String[] keys = Encoder.getKeys(json);
        for (String currKey : keys)
            try {
                output += jsonToURLEncodingAux(json.get(currKey), currKey);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        return output.substring(0, output.length() - 1);
    }

    private static String jsonToURLEncodingAux(Object json, String prefix) {
        String output = "";
        if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject)json;
            String[] keys = Encoder.getKeys(obj);
            for (String currKey : keys) {
                String subPrefix = prefix + "[" + currKey + "]";
                try {
                    output += jsonToURLEncodingAux(obj.get(currKey), subPrefix);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArr = (JSONArray) json;
            int arrLen = jsonArr.length();

            for (int i = 0; i < arrLen; i++) {
                String subPrefix = prefix + "[" + i + "]";
                Object child = null;
                try {
                    child = jsonArr.get(i);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                output += jsonToURLEncodingAux(child, subPrefix);
            }
        } else {
            output = prefix + "=" + json.toString() + "&";
        }

        return output;
    }

    private static String replaceWhitespaces(String str) {
        return str.replaceAll("\\s+", "%20");
    }

    private static String replacePluses(String str) {
        return str.replaceAll(Pattern.quote("+"), "%2B");
    }

    private static String[] getKeys(JSONObject jsonObject) {
        ArrayList<String> keyList = new ArrayList<>();
        Iterator<String> it = jsonObject.keys();
        while(it.hasNext()) {
            keyList.add(it.next());
        }
        return keyList.toArray(new String[keyList.size()]);
    }

}
