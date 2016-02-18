package com.maximus.dm.decentralizedmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.maximus.dm.decentralizedmessenger.helper.Encoder;
import com.maximus.dm.decentralizedmessenger.helper.Networking;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity implements View.OnClickListener {

    TextView tvSignUp;
    EditText etEmailOrUsername, etPassword;
    Button bLogin;
    UserLocalStore userLocalStore;
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

        userLocalStore = new UserLocalStore(this);
        userDatabase = new UserDatabase(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bLogin:
                //login clicked
                String enteredEmailOrUsername = etEmailOrUsername.getText().toString();
                String enteredPassword = etPassword.getText().toString();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("user", enteredEmailOrUsername);
                    jsonObject.put("password", enteredPassword);
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                Networking networking = new Networking(this);
                String response = networking.connect(Networking.SERVER_PATH_LOGIN, Encoder.jsonToUrl(jsonObject));
                System.out.println("RESPONSE: " + response);

                break;
            case R.id.tvSignUp:
                //register clicked

                startActivity(new Intent(this, Register.class));
                break;
        }
    }
    /*
    private boolean verifyLogin(String jsonResponse) {

    }
    */
}
