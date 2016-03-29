package com.maximus.dm.decentralizedmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maximus.dm.decentralizedmessenger.User.User;
import com.maximus.dm.decentralizedmessenger.User.UserDatabase;
import com.maximus.dm.decentralizedmessenger.helper.Encoder;
import com.maximus.dm.decentralizedmessenger.helper.Encrypt;
import com.maximus.dm.decentralizedmessenger.helper.Networking;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Register";

    EditText etUsername, etPassword, etConfirmPassword;
    Button bRegister;
    UserDatabase userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // assign variables
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
        bRegister = (Button) findViewById(R.id.bRegister);

        bRegister.setOnClickListener(this);

        userDatabase = new UserDatabase(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bRegister:
                //register clicked
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();
                Encrypt encrypt = new Encrypt();
                Encrypt.StringKeyPair keyPair = null;
                String privKey = null;
                String pubKey = null;

                try {
                    keyPair = encrypt.generatePrivateKey();
                    privKey = keyPair.getPrivateKey();
                    pubKey = keyPair.getPublicKey();
                    Log.d(TAG, "onClick, privKey " + privKey);
                    Log.d(TAG, "onClick, pubKey " + pubKey);
                } catch(Exception ee) {
                    ee.printStackTrace();
                }

                if (username.length() > 0 && password.length() > 0 && confirmPassword.length() > 0) {
                    if(password.equals(confirmPassword)) {
                        JSONObject jsonToSend = new JSONObject();
                        JSONObject jsonResponse = null;

                        try {
                            jsonToSend.put("user", username);
                            jsonToSend.put("password", password);
                            jsonToSend.put("privKey", privKey);
                            jsonToSend.put("pubKey", pubKey);
                            jsonToSend.put("managed", true);

                            Networking networking = new Networking(this);
                            String params = Encoder.jsonToUrl(jsonToSend);

                            String response = networking.connect(Networking.SERVER_PATH_REGISTER, params);
                            Log.d(TAG, "onClick sending " + params);
                            Log.d(TAG, "onClick server response " + response);
                            jsonResponse = new JSONObject(response);

                            // If user registered successfully
                            if (register(jsonResponse)) {
                                startActivity(new Intent(this, Login.class));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    private boolean register(JSONObject jsonResponse) {
        Log.d(TAG, "register: " + jsonResponse.toString());
        String successName = "success";
        try {
            if(jsonResponse.has(successName)) {
                if(jsonResponse.getBoolean(successName)) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
