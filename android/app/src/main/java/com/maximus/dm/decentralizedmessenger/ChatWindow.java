package com.maximus.dm.decentralizedmessenger;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ChatWindow extends AppCompatActivity implements View.OnClickListener {

    ListView lvMessages;
    EditText etMessage;
    Button bSend;

    ArrayAdapter<String> messagesAdapter;
    List<String> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        setTitle("Someones Name");

        etMessage = (EditText) findViewById(R.id.etMessage);
        bSend = (Button) findViewById(R.id.bSend);
        bSend.setOnClickListener(this);
        lvMessages = (ListView) findViewById(R.id.lvMessages);
        messages = new ArrayList<String>();

        messages.add("Hello");
        messages.add("World");
        messagesAdapter = new ArrayAdapter<String>(this, R.layout.temp_chat_elem, R.id.tvListitem, messages);

        lvMessages.setAdapter(messagesAdapter);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bSend:
                String messageToSend = etMessage.getText().toString();
                if (messageToSend.length() > 0) {
                    messages.add(messageToSend.toString());
                    messagesAdapter.notifyDataSetChanged();
                    etMessage.setText("");
                }
                break;
        }
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
