package com.maximus.dm.decentralizedmessenger.tabs;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.maximus.dm.decentralizedmessenger.R;

/**
 * Created by Maximus on 30/01/2016.
 */
public class DialogsTab extends Fragment {

    public static String TAB_NAME = "Dialogs";

    ListView dialogList;

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

        String[] listData = {"shoutout", "to", "whoeverz", "readn", "this", "you're beautiful"};
        ArrayAdapter<String> dialogAdapter = new ArrayAdapter<String>(
                getContext(), R.layout.listelement_dialog, R.id.tvListString, listData);

        dialogList = (ListView) v.findViewById(R.id.list_dialog);
        dialogList.setAdapter(dialogAdapter);
    }



}
