package com.maximus.dm.decentralizedmessenger.tabs;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.maximus.dm.decentralizedmessenger.Login;
import com.maximus.dm.decentralizedmessenger.R;
import com.maximus.dm.decentralizedmessenger.User.User;
import com.maximus.dm.decentralizedmessenger.User.UserDatabase;
import com.maximus.dm.decentralizedmessenger.User.UserLocalStore;

/**
 * Created by Maximus on 30/01/2016.
 */
public class ProfileTab extends Fragment implements View.OnClickListener {

    //TODO: Clear logged in user after user logs out

    public static String TAB_NAME = "Profile";

    UserDatabase userDatabase;

    TextView tvWelcome, tvUsername, tvEmail;
    Button bLogout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        userDatabase = new UserDatabase(this.getContext());

        tvWelcome = (TextView)this.getView().findViewById(R.id.tvWelcome);
        tvUsername = (TextView) this.getView().findViewById(R.id.tvUsername);
        tvEmail = (TextView) this.getView().findViewById(R.id.tvEmail);
        bLogout = (Button) this.getView().findViewById(R.id.bLogout);

        bLogout.setOnClickListener(this);

        showUserData();
    }

    private void showUserData() {
        User user = userDatabase.getLoggedIn();
        tvWelcome.setText(user.getUsername() + "'s Profile");
        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bLogout:
                //register clicked
                startActivity(new Intent(getActivity(), Login.class));
                break;
        }
    }
}
