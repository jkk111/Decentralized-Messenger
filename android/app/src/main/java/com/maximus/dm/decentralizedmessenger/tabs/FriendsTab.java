package com.maximus.dm.decentralizedmessenger.tabs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.maximus.dm.decentralizedmessenger.R;
import com.maximus.dm.decentralizedmessenger.User.Friend;
import com.maximus.dm.decentralizedmessenger.User.UserDatabase;
import com.maximus.dm.decentralizedmessenger.UserProfile;
import com.maximus.dm.decentralizedmessenger.helper.FriendsTabAdapter;
import com.maximus.dm.decentralizedmessenger.helper.ServerQueries;

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
    FriendsTabAdapter friendsTabAdapter;

    UserDatabase userDatabase;
    ServerQueries serverQueries;

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

        serverQueries = new ServerQueries(context);
        userDatabase = new UserDatabase(context);
        friendsArray = new ArrayList<Friend>();

        initComponents(v);
        getFriendList();

        lvFriendsTab = (ListView) v.findViewById(R.id.lvFriendsTab);
        friendsTabAdapter = new FriendsTabAdapter(
                context,
                R.layout.listelement_friendstab,
                friendsArray
        );

        lvFriendsTab.setAdapter(friendsTabAdapter);
        lvFriendsTab.setOnItemClickListener(this);
    }

    private void initComponents(View v) {
        etFriendsTabAddFriendUsername = (EditText) v.findViewById(R.id.etFriendsTabAddFriendUsername);
        bFriendsTabAddFriend = (Button) v.findViewById(R.id.bFriendsTabAddFriend);

        bFriendsTabAddFriend.setOnClickListener(this);
    }

    // Populates friends array to pass to adapter

    private void getFriendList() {
        friendsArray.clear();

        int senderId = userDatabase.getCurrentId();
        String senderToken = userDatabase.getToken();

        JSONObject jsonObject = null;
        try {
            JSONArray receivedFriendsArray = serverQueries.getFriendList(senderId, senderToken);
            JSONObject currentFriend;
            if(receivedFriendsArray != null) {
                for (int i = 0; i < receivedFriendsArray.length(); i++) {
                    currentFriend = (JSONObject) receivedFriendsArray.get(i);

                    int friendshipId = currentFriend.getInt("friendshipId");
                    int id = currentFriend.getInt("id");
                    String username = currentFriend.getString("username");
                    boolean pending = currentFriend.getBoolean("pending");
                    String publicKey = currentFriend.getString("public");

                    if (pending) {
                        boolean initiatedBySelf = currentFriend.getBoolean("initiatedBySelf");
                        friendsArray.add(new Friend(friendshipId, id, username, pending, initiatedBySelf));
                    } else {
                        friendsArray.add(new Friend(friendshipId, id, username, pending, publicKey));
                    }
                }
            }
            Log.d(TAG, "getFriendList, friend array length " + friendsArray.size());

        } catch(JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean sendFriendRequest() {
        boolean methodSuccess = false;

        // Search typed username and get all matched users
        String query = etFriendsTabAddFriendUsername.getText().toString();
        JSONArray results =  serverQueries.search(userDatabase.getCurrentId(), userDatabase.getToken(), query);
        int friendId = -1;

        //TODO: This is temporary functionality. Learn "Searchables" then change.
        if (results.length() == 1) {
            // accept
            try {
                // Get matched users id
                JSONObject friend = results.getJSONObject(0);
                 friendId = friend.getInt("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            boolean response = serverQueries.sendFriendRequest(
                    userDatabase.getCurrentId(),
                    userDatabase.getToken(),
                    friendId,
                    "rofl"
            );

            if (response) {
                methodSuccess = true;
                Toast.makeText(this.getContext(), "Friend request sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.getContext(), "Friend exists", Toast.LENGTH_SHORT).show();
            }

        } else {
            // reject
            Toast.makeText(this.getContext(), "More than one user found", Toast.LENGTH_SHORT).show();
        }

        return methodSuccess;
    }

    private void friendRequestResponse(int friendshipId, boolean response) {
        serverQueries.confirmFriend(response, userDatabase.getToken(), userDatabase.getCurrentId(), friendshipId);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bFriendsTabAddFriend:
                boolean reqSent = sendFriendRequest();
                if (reqSent) {
                    getFriendList();
                    friendsTabAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /*
        onItemClick create appropriate dialog
        // Handle response in onFriendRequestDialog Positive/Negative Click
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Friend focusedFriend = friendsArray.get(position);
        Intent intent = new Intent(this.getActivity(), UserProfile.class);
        intent.putExtra(UserProfile.EXTRA_FRIENDS_ID, focusedFriend.getUserId());
        intent.putExtra(UserProfile.EXTRA_FRIENDS_NAME, focusedFriend.getUsername());
        intent.putExtra(UserProfile.EXTRA_FRIENDSHIP_ID, focusedFriend.getFriendshipId());
        intent.putExtra(UserProfile.EXTRA_FRIENDSHIP_PENDING, focusedFriend.isPending());
        intent.putExtra(UserProfile.EXTRA_FRIENDSHIP_INIT_BY_SELF, focusedFriend.isInitiatedBySelf());
        intent.putExtra(UserProfile.EXTRA_FRIENDS_PUBLIC_KEY, focusedFriend.getPublicKey());
        startActivity(intent);
    }

}
