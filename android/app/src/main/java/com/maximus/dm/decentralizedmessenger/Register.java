package com.maximus.dm.decentralizedmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maximus.dm.decentralizedmessenger.helper.Encoder;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity implements View.OnClickListener {

    EditText etUsername, etEmail, etPassword, etConfirmPassword;
    Button bRegister;
    UserDatabase userDatabase;

    static String SERVER_REGISTER = "/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // assign variables
        etUsername = (EditText) findViewById(R.id.etUsername);
        etEmail = (EditText) findViewById(R.id.etEmail);
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
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                // if passwords match, send user off to the server

                //add check for existing email
                boolean yes = false;
                if (username.length() > 0 &&
                        email.length() > 0 &&
                        password.length() > 0 &&
                        password.equals(confirmPassword)) { yes = true; }

                if(yes) {
                    // for local db
                    User user = new User(username, email, password);
                    userDatabase.storeUser(user);

                    // server stuff
                    Login networking = new Login();
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("user", username);
                        jsonObject.put("password", password);

                        // Send request, get response (POST)
                        String jsonToUrl = Encoder.jsonToURLEncoding(jsonObject);
                        String jsonToUrlNoSpace = Encoder.replaceWhitespaces(jsonToUrl);
                        String serverResponse = networking.connect(SERVER_REGISTER, jsonToUrlNoSpace);
                        JSONObject jsonResponse = new JSONObject(serverResponse);

                        Toast.makeText(this,serverResponse, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println("REGISTER ACTIVITY: " + e.toString());
                    }

                    //startActivity(new Intent(this, Login.class));
                }

                break;
        }
    }
}
