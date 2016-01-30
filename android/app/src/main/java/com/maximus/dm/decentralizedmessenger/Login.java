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
import android.widget.TextView;

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

                User userDB = userDatabase.getUser(UserDatabase.USER_ID);
                String usernameDB = userDB.getUsername();
                String emailDB = userDB.getEmail();
                String passwordDB = userDB.getPassword();

                if(enteredEmailOrUsername.equals(usernameDB) || enteredEmailOrUsername.equals(emailDB)) {
                    if(enteredPassword.equals(passwordDB)) {
                        userLocalStore.storeUserData(userDB);
                        userLocalStore.setUserLoggedIn(true);
                        startActivity(new Intent(this, MainActivity.class));
                    }
                }

                break;
            case R.id.tvSignUp:
                //register clicked

                startActivity(new Intent(this, Register.class));
                break;
        }
    }

}
