package com.maximus.dm.decentralizedmessenger;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.maximus.dm.decentralizedmessenger.User.UserDatabase;
import com.maximus.dm.decentralizedmessenger.helper.ChatWindowAdapter;
import com.maximus.dm.decentralizedmessenger.helper.ServerQueries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatWindow extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "ChatWindow";

    ListView lvMessages;
    EditText etMessage;
    Button bSend;

    ChatWindowAdapter chatWindowAdapter;

    private ServerQueries serverQueries;
    private UserDatabase userDatabase;

    private int friendsId;
    private String friendsName;
    private String friendsPublicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        getExtras();
        setTitle(friendsName);
        userDatabase = new UserDatabase(this);
        serverQueries = new ServerQueries(this);

        etMessage = (EditText) findViewById(R.id.etMessage);
        bSend = (Button) findViewById(R.id.bSend);
        bSend.setOnClickListener(this);
        lvMessages = (ListView) findViewById(R.id.lvMessages);
        chatWindowAdapter = new ChatWindowAdapter(this);

        refreshChatFull();

        lvMessages.setAdapter(chatWindowAdapter);
    }

    private void getExtras() {
        Intent intent = getIntent();
        friendsId = intent.getIntExtra(UserProfile.EXTRA_FRIENDS_ID, -1);
        friendsName = intent.getStringExtra(UserProfile.EXTRA_FRIENDS_NAME);
        friendsPublicKey = intent.getStringExtra(UserProfile.EXTRA_FRIENDS_PUBLIC_KEY);
        Log.d(TAG, "getExtras, friendsPublicKey " + friendsPublicKey);
    }

    private void refreshChatFull() {
        chatWindowAdapter.clearList();
        JSONObject jsonMessages = serverQueries.getMessages(userDatabase.getCurrentId(), userDatabase.getToken());
        JSONArray conversation = getConversation(jsonMessages, Integer.toString(friendsId));
        JSONObject currentMessage = null;
        if (conversation != null) {
            // Found messages with current friend, ready to populate listview
            for (int i = 0; i < conversation.length(); i++) {
                try {
                    currentMessage = (JSONObject) conversation.get(i);
                    chatWindowAdapter.addMessage(currentMessage);
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
        Get specific conversation from all messages
     */
    private JSONArray getConversation(JSONObject allMessages, String otherPartyId) {
        JSONArray conversation = null;
        if (allMessages != null) {
            if (allMessages.has(otherPartyId)) {
                try {
                    conversation = allMessages.getJSONArray(otherPartyId);
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return conversation;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bSend:
                String messageToSend = etMessage.getText().toString();
                if (messageToSend.length() > 0) {
                    boolean success = sendMessage(messageToSend, friendsPublicKey);
                    if (success) {
                        etMessage.setText("");
                        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
                        //TODO: Don't refresh full chat on message sent, create an object and add it manually
                        refreshChatFull();
                    } else {
                        Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private boolean sendMessage(String message, String publicKeyRecipient) {
        return serverQueries.sendMessage(
                userDatabase.getCurrentId(),
                userDatabase.getToken(),
                friendsId,
                message,
                userDatabase.getPublicKey(),
                publicKeyRecipient
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
