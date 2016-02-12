package com.maximus.dm.decentralizedmessenger;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.maximus.dm.decentralizedmessenger.helper.Encoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity implements View.OnClickListener {

    EditText etEmailOrUsername, etPassword, etHostip;
    Button bLogin;
    TextView tvSignUp;
    UserLocalStore userLocalStore;
    UserDatabase userDatabase;

    static String SERVER_LOGIN = "/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // assign variables
        etEmailOrUsername = (EditText) findViewById(R.id.etEmailOrUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etHostip = (EditText) findViewById(R.id.etHostip);
        tvSignUp = (TextView) findViewById(R.id.tvSignUp);
        bLogin = (Button) findViewById(R.id.bLogin);

        bLogin.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);

        userLocalStore = new UserLocalStore(this);
        userDatabase = new UserDatabase(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bLogin:
                //login clicked
                String enteredEmailOrUsername = etEmailOrUsername.getText().toString();
                String enteredPassword = etPassword.getText().toString();
                /*
                User userDB = userDatabase.getUser(UserDatabase.USER_ID);
                String usernameDB = userDB.getUsername();
                String emailDB = userDB.getEmail();
                String passwordDB = userDB.getPassword();
                */

                // When login is clicked
                // Take user + password
                // Encode
                // Send to server
                // If response == success, user logged in

                // Create object for logging in.
                // Params: user, password
                boolean yes = false;
                if (enteredEmailOrUsername.length() > 0 && enteredPassword.length() > 0)
                    yes = true;
                if (yes) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("user", enteredEmailOrUsername);
                        jsonObject.put("password", enteredPassword);

                        // Sending request to server
                        String jsonToUrl = Encoder.jsonToURLEncoding(jsonObject);
                        String jsonToUrlNoSpace = Encoder.replaceWhitespaces(jsonToUrl);
                        String serverResponse = connect(SERVER_LOGIN, jsonToUrlNoSpace);

                        JSONObject jsonResponse = new JSONObject(serverResponse);
                        // If there is a variable "success"

                        if (jsonResponse.has("success")) {
                            if (jsonResponse.getBoolean("success")) {
                                //"Login: Success"
                                Toast.makeText(this, serverResponse, Toast.LENGTH_SHORT).show();
                            } else {
                                //"Login: Fail"
                                Toast.makeText(this, serverResponse, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //"Error"
                            Toast.makeText(this, serverResponse, Toast.LENGTH_SHORT).show();
                        }

                        Toast.makeText(this, serverResponse, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println("LOGIN ACTIVITY: " + e.toString());
                    }
                } else {
                    Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show();
                }
                /*
                if(enteredEmailOrUsername.equals(usernameDB) || enteredEmailOrUsername.equals(emailDB)) {
                    if(enteredPassword.equals(passwordDB)) {
                        userLocalStore.storeUserData(userDB);
                        userLocalStore.setUserLoggedIn(true);
                        startActivity(new Intent(this, MainActivity.class));
                    }
                }
                */

                break;
            case R.id.tvSignUp:
                //register clicked

                startActivity(new Intent(this, Register.class));
                break;
        }
    }

    public String connect(String path, String urlParams) {
        Networking n = new Networking();
        String returned = "";
        try {
            returned = n.execute(path, urlParams).get();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return returned;
    }

    // Calls get JSON from server, returns string to UI thread
    public class Networking extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return getJsonFromServer(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    // Gets JSON string from server
    private String getJsonFromServer(String path, String urlParams) {
        // Setup stuff and other stuff aka "variable declaration"
        String domain = "http://john-kevin.me:8080";
        URL url = null;
        HttpURLConnection connection = null;

        try {
            url = new URL(domain + path);
            connection = (HttpURLConnection) url.openConnection();

            // Connection setup
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Write to connection
            DataOutputStream dos =  new DataOutputStream(connection.getOutputStream());
            dos.writeBytes(urlParams);
            dos.flush();
            dos.close();

            // Get response
            InputStream isr = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(isr));
            String line = "";
            StringBuffer response = new StringBuffer();

            // while there are lines to read
            while((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            System.out.println("RESPONSE: " + response.toString());
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
