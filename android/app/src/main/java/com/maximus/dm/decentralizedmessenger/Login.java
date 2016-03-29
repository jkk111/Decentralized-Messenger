package com.maximus.dm.decentralizedmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.maximus.dm.decentralizedmessenger.User.User;
import com.maximus.dm.decentralizedmessenger.User.UserDatabase;
import com.maximus.dm.decentralizedmessenger.helper.Encoder;
import com.maximus.dm.decentralizedmessenger.helper.Networking;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Login";

    TextView tvSignUp;
    EditText etUsername, etPassword;
    Button bLogin;
    UserDatabase userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // assign variables
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        tvSignUp = (TextView) findViewById(R.id.tvSignUp);

        bLogin.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);

        userDatabase = new UserDatabase(this);
        userDatabase.clearAll();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bLogin:
                //login clicked
                String enteredUsername = etUsername.getText().toString();
                String enteredPassword = etPassword.getText().toString();

                if(enteredUsername.length() > 0 && enteredPassword.length() > 0) {
                    JSONObject jsonToSend = new JSONObject();
                    JSONObject jsonResponse = null;
                    try {
                        // Send entered info to server
                        // Convert response to json, and analyse
                        jsonToSend.put("user", enteredUsername);
                        jsonToSend.put("password", enteredPassword);
                        Networking networking = new Networking(this);
                        String responseStr = networking.connect(Networking.SERVER_PATH_LOGIN, Encoder.jsonToUrl(jsonToSend));
                        jsonResponse = new JSONObject(responseStr);

                        if(validInfo(jsonResponse)) {
                            // Store everything in local db (SharedPrefs)
                            userDatabase.storeUser(enteredUsername, jsonResponse.getInt("id"));
                            userDatabase.setToken(jsonResponse.getString("token"));
                            userDatabase.setPublicKey(jsonResponse.getString("public"));

                            Log.d(TAG, "onLogin, token " + jsonResponse.getString("token"));

                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.tvSignUp:
                //register clicked

                startActivity(new Intent(this, Register.class));
                break;
        }
    }

    private boolean validInfo(JSONObject jsonResponse) {
        if (jsonResponse == null) return false;

        String successField = "success";
        try {
            if(jsonResponse.has(successField)) {
                if(jsonResponse.getBoolean(successField)) {
                    Toast.makeText(this, "SUCCESS: " + jsonResponse, Toast.LENGTH_SHORT).show();
                    return true;
                }
            } else {
                Toast.makeText(this, "FAILED: " + jsonResponse, Toast.LENGTH_SHORT).show();
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

}
