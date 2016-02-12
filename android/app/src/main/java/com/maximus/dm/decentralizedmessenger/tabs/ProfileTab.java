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
import com.maximus.dm.decentralizedmessenger.User;
import com.maximus.dm.decentralizedmessenger.UserLocalStore;

import org.w3c.dom.Text;

/**
 * Created by Maximus on 30/01/2016.
 */
public class ProfileTab extends Fragment implements View.OnClickListener {

    public static String TAB_NAME = "Profile";

    UserLocalStore userLocalStore;

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
        userLocalStore = new UserLocalStore(this.getContext());

        tvWelcome = (TextView)this.getView().findViewById(R.id.tvWelcome);
        tvUsername = (TextView) this.getView().findViewById(R.id.tvUsername);
        tvEmail = (TextView) this.getView().findViewById(R.id.tvEmail);
        bLogout = (Button) this.getView().findViewById(R.id.bLogout);

        bLogout.setOnClickListener(this);

        if(authenticate()) {
            tvWelcome.setText("Maximus111 hw");
            showUserData();
        }
    }

    private boolean authenticate() {
        return userLocalStore.getUserLoggedIn();
    }

    private void showUserData() {
        User user = userLocalStore.getLoggedInUser();
        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bLogout:
                //register clicked
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);

                startActivity(new Intent(getActivity(), Login.class));
                break;
        }
    }
}
