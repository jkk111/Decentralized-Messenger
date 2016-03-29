package com.maximus.dm.decentralizedmessenger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.maximus.dm.decentralizedmessenger.User.UserDatabase;
import com.maximus.dm.decentralizedmessenger.helper.ServerQueries;

public class UserProfile extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "UserProfile";

    public static final String EXTRA_FRIENDS_ID = "id";
    public static final String EXTRA_FRIENDS_NAME = "name";
    public static final String EXTRA_FRIENDS_PUBLIC_KEY = "publicKey";
    public static final String EXTRA_FRIENDSHIP_ID = "friendshipId";
    public static final String EXTRA_FRIENDSHIP_PENDING = "friendshipPending";
    public static final String EXTRA_FRIENDSHIP_INIT_BY_SELF = "friendshipInitBySelf";

    private final int MODE_ADDING = 0;
    private final int MODE_MESSAGING = 1;

    private int friendsId;
    private String friendsName;
    private int friendshipId;
    private boolean friendshipPending;
    private boolean friendshipInitBysSelf;
    private String publicKey;

    private TextView tvTitle;
    private TextView tvName;
    private Button bDeleteOrCancel;
    private Button bAddOrMessage;

    private int addOrMessageMode;

    private UserDatabase userDatabase;
    private ServerQueries serverQueries;

    //TODO: Check if friendship exists before sending a request

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getExtras();

        tvTitle = (TextView) findViewById(R.id.tvUsersProfileTitle);
        tvName = (TextView) findViewById(R.id.tvUsersProfileName);
        bDeleteOrCancel = (Button) findViewById(R.id.bUsersProfileDeleteOrCancel);
        bDeleteOrCancel.setOnClickListener(this);
        bAddOrMessage = (Button) findViewById(R.id.bUsersProfileAddOrMessage);
        bAddOrMessage.setOnClickListener(this);

        pageSetup();

        userDatabase = new UserDatabase(getBaseContext());
        serverQueries = new ServerQueries(getBaseContext());
    }

    private void getExtras() {
        Intent intent = getIntent();
        friendsId = intent.getIntExtra(EXTRA_FRIENDS_ID, -1);
        friendsName = intent.getStringExtra(EXTRA_FRIENDS_NAME);
        friendshipId = intent.getIntExtra(EXTRA_FRIENDSHIP_ID, -1);
        friendshipPending = intent.getBooleanExtra(EXTRA_FRIENDSHIP_PENDING, false);
        if (friendshipPending) {
            friendshipInitBysSelf = intent.getBooleanExtra(EXTRA_FRIENDSHIP_INIT_BY_SELF, false);
        }
        publicKey = intent.getStringExtra(EXTRA_FRIENDS_PUBLIC_KEY);
        Log.d(TAG, "getExtras, friendsId: " + friendsId + ", friendsName: " + friendsName
            + ", friendshipId: " + friendshipId + ", friendshipPending: " + friendshipPending
                + ", friendshipInitBySelf: " + friendshipInitBysSelf);
    }

    private void pageSetup() {
        // Setup TextViews
        tvTitle.setText(friendsName + "'s Profile");
        tvName.setText(friendsName);

        // Setup buttons
        if (friendshipPending) {
            addOrMessageMode = MODE_ADDING;
            if (friendshipInitBysSelf) {
                // Waiting for user to accept friend request
                bAddOrMessage.setText("Message");
                bAddOrMessage.setEnabled(false);
                bDeleteOrCancel.setText("Cancel request");
                bDeleteOrCancel.setEnabled(true);
            } else {
                // Someone sent us a friend request
                bAddOrMessage.setText("Accept request");
                bAddOrMessage.setEnabled(true);
                bDeleteOrCancel.setText("Reject request");
                bDeleteOrCancel.setEnabled(true);
            }
        } else {
            addOrMessageMode = MODE_MESSAGING;
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bUsersProfileAddOrMessage:
                if (addOrMessageMode == MODE_MESSAGING) {
                    // Already friends, send message
                    Intent intent = new Intent(this, ChatWindow.class);
                    intent.putExtra(EXTRA_FRIENDS_ID, friendsId);
                    intent.putExtra(EXTRA_FRIENDS_NAME, friendsName);
                    intent.putExtra(EXTRA_FRIENDS_PUBLIC_KEY, publicKey);
                    startActivity(intent);
                } else {
                    // Not friends, accept request
                    boolean success = serverQueries.confirmFriend(true, userDatabase.getToken(),
                            userDatabase.getCurrentId(), friendshipId);
                    if (success) {
                        Toast.makeText(getBaseContext(), "Request accepted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        Toast.makeText(getBaseContext(), "Error, try again", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.bUsersProfileDeleteOrCancel:
                if (addOrMessageMode == MODE_MESSAGING) {
                    // Friends already, delete user

                } else {
                    if (!friendshipInitBysSelf) {
                        // Received request, reject it
                        boolean success = serverQueries.confirmFriend(false, userDatabase.getToken(),
                                userDatabase.getCurrentId(), friendshipId);
                        if (success) {
                            Toast.makeText(getBaseContext(), "Request rejected", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                        } else {
                            Toast.makeText(getBaseContext(), "Error, try again", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Sent request, cancel it
                        boolean success = serverQueries.cancelFriend(friendshipId, userDatabase.getCurrentId(), userDatabase.getToken());
                        if (success) {
                            Toast.makeText(getBaseContext(), "Request cancelled", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                        } else {
                            Toast.makeText(getBaseContext(), "Error, try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }
}
