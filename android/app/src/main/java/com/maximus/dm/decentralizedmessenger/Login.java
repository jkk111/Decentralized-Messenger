package com.maximus.dm.decentralizedmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.maximus.dm.decentralizedmessenger.User.User;
import com.maximus.dm.decentralizedmessenger.User.UserDatabase;
import com.maximus.dm.decentralizedmessenger.User.UserLocalStore;
import com.maximus.dm.decentralizedmessenger.helper.Encoder;
import com.maximus.dm.decentralizedmessenger.helper.Networking;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity implements View.OnClickListener {

    TextView tvSignUp;
    EditText etEmailOrUsername, etPassword;
    Button bLogin;
    UserDatabase userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // assign variables
        etEmailOrUsername = (EditText) findViewById(R.id.etEmailOrUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        tvSignUp = (TextView) findViewById(R.id.tvSignUp);

        bLogin.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);

        userDatabase = new UserDatabase(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bLogin:
                //login clicked
                String enteredEmailOrUsername = etEmailOrUsername.getText().toString();
                String enteredPassword = etPassword.getText().toString();

                if(enteredEmailOrUsername.length() > 0 && enteredPassword.length() > 0) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("user", enteredEmailOrUsername);
                        jsonObject.put("password", enteredPassword);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Networking networking = new Networking(this);
                    String response = networking.connect(Networking.SERVER_PATH_LOGIN, Encoder.jsonToUrl(jsonObject));
                    //System.out.println("RESPONSE: " + response);

                    if(validInfo(response)) {
                        User currentUser = new User(enteredEmailOrUsername, "mail@noemail.mail.ie.com");
                        userDatabase.setLoggedIn(currentUser);

                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    }
                }
                break;
            case R.id.tvSignUp:
                //register clicked

                startActivity(new Intent(this, Register.class));
                break;
        }
    }

    private boolean validInfo(String strResponse) {
        JSONObject jsonResponse;
        String successName = "success";

        try {
            jsonResponse = new JSONObject(strResponse);
            if(jsonResponse.has(successName)) {
                if(jsonResponse.getBoolean(successName)) {
                   //System.out.print("SUCCESS: " + jsonResponse);
                    Toast.makeText(this, "SUCCESS: " + jsonResponse, Toast.LENGTH_SHORT).show();
                    return true;
                }
            } else {
                //System.out.print("FAILED: " + jsonResponse);
                Toast.makeText(this, "FAILED: " + jsonResponse, Toast.LENGTH_SHORT).show();
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

}
