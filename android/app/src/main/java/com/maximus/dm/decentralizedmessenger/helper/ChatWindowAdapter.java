package com.maximus.dm.decentralizedmessenger.helper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.maximus.dm.decentralizedmessenger.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Maximus on 17/03/2016.
 */
public class ChatWindowAdapter extends BaseAdapter {
    private static final String TAG = "ChatWindowAdapter";


    private static final int TYPE_SENT = 0;
    private static final int TYPE_RECEIVED = 1;
    private static final int TYPE_MAX_COUNT = TYPE_RECEIVED + 1;

    private Context mContext;
    private List<JSONObject> mMessages;
    private LayoutInflater mInflater;

    private TreeSet mReceivedSet = new TreeSet();

    public ChatWindowAdapter(Context context) {
        mContext = context;
        mMessages = new ArrayList<JSONObject>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addMessage(final JSONObject message) {
        boolean fromSelf = true;
        if (message != null) {
            try {
                fromSelf = message.getBoolean("fromSelf");
            } catch(JSONException e){
                e.printStackTrace();
            }

            if (fromSelf) {
                addSentMessage(message);
            } else {
                addReceivedMessage(message);
            }
        }
    }

    public void addSentMessage(final JSONObject message) {
        mMessages.add(message);
        notifyDataSetChanged();
    }

    public void addReceivedMessage(final JSONObject message) {
        mMessages.add(message);
        // save received position
        mReceivedSet.add(mMessages.size() - 1);
        notifyDataSetChanged();
    }

    public void clearList() {
        mMessages.clear();
    }

    @Override
    public int getItemViewType(int position) {
        return mReceivedSet.contains(position) ? TYPE_RECEIVED : TYPE_SENT;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //TODO: Only handled case when view wasn't recycled, handle other
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        int type = getItemViewType(position);
        JSONObject currentMessage = mMessages.get(position);
        String message = "";
        String time = "";
        TextView messageView;
        TextView timeView;
        try {
            message = currentMessage.getString("message");
            time = currentMessage.getString("sent");
        } catch(JSONException e) {
            e.printStackTrace();
        }

        if (convertView == null) {
            holder = new ViewHolder();

            switch(type) {
                case TYPE_SENT:
                    convertView = mInflater.inflate(R.layout.chat_msg_sent, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.tvChatMsgSentMessage);
                    messageView = (TextView)convertView.findViewById(R.id.tvChatMsgSentMessage);
                    messageView.setText(message);
                    timeView = (TextView)convertView.findViewById(R.id.tvChatMsgSentTime);
                    timeView.setText(Utils.getTime(time));
                    break;
                case TYPE_RECEIVED:
                    convertView = mInflater.inflate(R.layout.chat_msg_rcv, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.tvChatMsgRcvMessage);
                    messageView = (TextView)convertView.findViewById(R.id.tvChatMsgRcvMessage);
                    messageView.setText(message);
                    timeView = (TextView)convertView.findViewById(R.id.tvChatRcvSentTime);
                    String newTime = Utils.getTime(time);
                    Log.d(TAG, "getView, time of message: " + newTime);
                    timeView.setText(newTime);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            switch(type) {
                case TYPE_SENT:
                    holder.textView = (TextView) convertView.findViewById(R.id.tvChatMsgSentMessage);
                    messageView = (TextView)convertView.findViewById(R.id.tvChatMsgSentMessage);
                    messageView.setText(message);
                    timeView = (TextView)convertView.findViewById(R.id.tvChatMsgSentTime);
                    timeView.setText(Utils.getTime(time));
                    break;
                case TYPE_RECEIVED:
                    holder.textView = (TextView) convertView.findViewById(R.id.tvChatMsgRcvMessage);
                    messageView = (TextView)convertView.findViewById(R.id.tvChatMsgRcvMessage);
                    messageView.setText(message);
                    timeView = (TextView)convertView.findViewById(R.id.tvChatRcvSentTime);
                    timeView.setText(Utils.getTime(time));
                    break;
            }
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}
