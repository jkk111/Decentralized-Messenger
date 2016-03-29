package com.maximus.dm.decentralizedmessenger.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maximus.dm.decentralizedmessenger.R;
import com.maximus.dm.decentralizedmessenger.User.Friend;

import java.util.List;

/**
 * Created by Maximus on 29/02/2016.
 */
public class FriendsTabAdapter extends ArrayAdapter<Friend> {

    private Context context;
    private int layoutResource;

    public FriendsTabAdapter(Context context, int resource, List<Friend> objects) {
        super(context, resource, objects);
        this.context = context;
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
                tvFriendshipStatus.setText("Friends");
            }
        }
        return customView;
    }
}
