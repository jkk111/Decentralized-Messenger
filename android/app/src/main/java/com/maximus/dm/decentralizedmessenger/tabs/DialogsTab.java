package com.maximus.dm.decentralizedmessenger.tabs;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.maximus.dm.decentralizedmessenger.ChatWindow;
import com.maximus.dm.decentralizedmessenger.R;
import com.maximus.dm.decentralizedmessenger.User.Friend;
import com.maximus.dm.decentralizedmessenger.User.UserDatabase;
import com.maximus.dm.decentralizedmessenger.UserProfile;
import com.maximus.dm.decentralizedmessenger.helper.Encoder;
import com.maximus.dm.decentralizedmessenger.helper.Networking;
import com.maximus.dm.decentralizedmessenger.helper.ServerQueries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maximus on 30/01/2016.
 */
public class DialogsTab extends Fragment implements AdapterView.OnItemClickListener {
    public final static String TAG = "Dialogs";
    public final static String TAB_NAME = "Dialogs";

    //TODO: Move "getFriends" somewhere else

    UserDatabase userDatabase;
    ServerQueries serverQueries;
    ListView dialogList;
    List<String> listData;

    List<Friend> friendsArray;

    //set view for the fragment
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dialogs, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        View v = this.getView();
        userDatabase = new UserDatabase(getContext());
        serverQueries = new ServerQueries(getContext());
        friendsArray = new ArrayList<Friend>();
        getFriendList();

        listData = new ArrayList<String>();

        ArrayAdapter<String> dialogAdapter = new ArrayAdapter<String>(
                getContext(), R.layout.listelement_dialog, R.id.tvListString, listData);

        dialogList = (ListView) v.findViewById(R.id.list_dialog);
        dialogList.setAdapter(dialogAdapter);
        dialogList.setOnItemClickListener(this);

        updateDialog();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String messageToShow = "POS: " + position + " ITEM: " + parent.getItemAtPosition(position).toString();
        System.out.print("ITEM AT POS: " + messageToShow);
        Toast.makeText(this.getContext(), messageToShow, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), ChatWindow.class);
        intent.putExtra(UserProfile.EXTRA_FRIENDS_ID, friendsArray.get(position).getUserId());
        intent.putExtra(UserProfile.EXTRA_FRIENDS_NAME, friendsArray.get(position).getUsername());
        intent.putExtra(UserProfile.EXTRA_FRIENDS_PUBLIC_KEY, friendsArray.get(position).getPublicKey());
        Log.d(TAG, "onItemClick, friendsPublicKey " + friendsArray.get(position).getPublicKey());
        startActivity(intent);
    }

    private void updateDialog() {
        int sender = userDatabase.getCurrentId();
        String token = userDatabase.getToken();

        JSONObject messages = serverQueries.getMessages(sender, token);
        if (messages != null && messages.length() > 0) {
            JSONArray dialog = null;
            for (int i = 0; i < friendsArray.size(); i++) {
                Log.d(TAG, "updateDialog, current friend: " + friendsArray.get(i).getUsername());
                try {
                    dialog = messages.getJSONArray(Integer.toString(friendsArray.get(i).getUserId()));
                    listData.add(friendsArray.get(i).getUsername());
                    Log.d(TAG, "updateDialog, adding to friendsArray: " + friendsArray.get(i).getUsername());
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Add friends who are not "pending" to the array
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

                    Log.d(TAG, "getFriendList, publicKey " + publicKey);

                    if (!pending) {
                        friendsArray.add(new Friend(friendshipId, id, username, pending, publicKey));
                    }
                }
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

    }

}
