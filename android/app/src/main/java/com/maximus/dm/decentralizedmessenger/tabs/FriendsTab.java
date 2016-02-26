package com.maximus.dm.decentralizedmessenger.tabs;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.maximus.dm.decentralizedmessenger.R;
import com.maximus.dm.decentralizedmessenger.User.Friend;
import com.maximus.dm.decentralizedmessenger.User.UserDatabase;
import com.maximus.dm.decentralizedmessenger.helper.Encoder;
import com.maximus.dm.decentralizedmessenger.helper.Networking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maximus on 19/02/2016.
 */
public class FriendsTab extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private final static String TAG = "FriendsTab";
    public final static String TAB_NAME = "Friends";
    Context context;

    ListView lvFriendsTab;
    List<Friend> friendsArray;

    UserDatabase userDatabase;

    // Sending friend request
    EditText etFriendsTabAddFriendUsername;
    Button bFriendsTabAddFriend;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_friends_main, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.context = getContext();
        View v = this.getView();

        userDatabase = new UserDatabase(context);
        friendsArray = new ArrayList<Friend>();

        initComponents(v);
        getFriendList();

        lvFriendsTab = (ListView) v.findViewById(R.id.lvFriendsTab);
        FriendsTabAdapter friendsTabAdapter = new FriendsTabAdapter(
                context,
                R.layout.listelement_friendstab,
                friendsArray
        );

        lvFriendsTab.setAdapter(friendsTabAdapter);
    }

    private void initComponents(View v) {
        etFriendsTabAddFriendUsername = (EditText) v.findViewById(R.id.etFriendsTabAddFriendUsername);
        bFriendsTabAddFriend = (Button) v.findViewById(R.id.bFriendsTabAddFriend);

        bFriendsTabAddFriend.setOnClickListener(this);
    }

    private void getFriendList() {
        int senderId = userDatabase.getCurrentId();
        String senderToken = userDatabase.getToken();

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("sender", senderId);
            jsonObject.put("token", senderToken);

            //Log.d(TAG, "getFriendList, token " + senderToken);
            String sentToken = Encoder.jsonToUrl(jsonObject);
            Networking networking = new Networking(context);
            String strResponse = networking.connect(Networking.SERVER_PATH_GET_FRIENDS, sentToken);
            Log.d(TAG, "getFriendList, " + strResponse);

            // TODO: Save friends to database
            JSONArray receivedFriendsArray = new JSONArray(strResponse);
            JSONObject currentFriend;
            for(int i = 0; i < receivedFriendsArray.length(); i++) {
                currentFriend = (JSONObject) receivedFriendsArray.get(i);
                int id = currentFriend.getInt("id");
                String username = currentFriend.getString("username");
                boolean pending = currentFriend.getBoolean("pending");
                boolean initiatedBySelf = currentFriend.getBoolean("initiatedBySelf");

                friendsArray.add(new Friend(id, username, pending, initiatedBySelf));
            }
            Log.d(TAG, "getFriendList, friend array length " + friendsArray.size());

        } catch(JSONException e) {
            e.printStackTrace();
        }

    }

    /*
        {
            sender: "The user to add friend to",
            token: "user token",
            client: "The client id to add as friend",
            secretL "Not implemented yet send any value to prevent errors"
        }
    */
    private void sendFriendRequest() {
        String enteredUsername = etFriendsTabAddFriendUsername.getText().toString();
        if (enteredUsername.length() > 0) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject();
                jsonObject.put("sender", userDatabase.getCurrentId());
                jsonObject.put("token", userDatabase.getToken());
                jsonObject.put("client", enteredUsername);
                jsonObject.put("secret", "rofl");

                Networking networking = new Networking(context);
                String response = networking.connect(Networking.SERVER_PATH_ADD_FRIEND_ID, Encoder.jsonToUrl(jsonObject));
                Log.d(TAG, "sendFriendRequest, senderToken " + userDatabase.getToken());
                Log.d(TAG, "sendFriendRequest, response " + response);
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bFriendsTabAddFriend:
                sendFriendRequest();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "ListView onItemClick, clicked");
        switch(view.getId()) {
            default:

        }
    }

    private class FriendsTabAdapter extends ArrayAdapter<Friend> {

        private int layoutResource;

        public FriendsTabAdapter(Context context, int resource, List<Friend> objects) {
            super(context, resource, objects);
            layoutResource = resource;
        }

        //TODO: Use one layout pending existing friendships.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View customView = convertView;
            if (convertView == null) {
                // Inflate layout onto customView
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                customView = inflater.inflate(layoutResource, parent, false);

                Friend currentFriend = getItem(position);
                String username = currentFriend.getUsername();
                boolean pending = currentFriend.isPending();
                boolean initiatedBySelf = currentFriend.isInitiatedBySelf();

                TextView tvFriendsName = (TextView) customView.findViewById(R.id.tvFriendsTabFriendName);
                tvFriendsName.setText(username);

                TextView tvFriendshipStatus = (TextView) customView.findViewById(R.id.tvFriendsTabFriendshipStatus);
                if (pending) {
                    if (initiatedBySelf) {
                        tvFriendshipStatus.setText("Request sent");
                    } else {
                        tvFriendshipStatus.setText("Accept/Reject");
                    }
                } else {

                }


            }
            return customView;
        }
    }
}
