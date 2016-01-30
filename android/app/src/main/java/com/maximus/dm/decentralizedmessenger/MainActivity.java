package com.maximus.dm.decentralizedmessenger;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

import com.maximus.dm.decentralizedmessenger.tabs.DialogsTab;
import com.maximus.dm.decentralizedmessenger.tabs.ProfileTab;
import com.maximus.dm.decentralizedmessenger.tabs.TabPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, TabHost.OnTabChangeListener {

    ViewPager viewPager;
    TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create tab host (holds tabs)
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        //create tabs one by one (to populate tab host)
        String[] tabName = {ProfileTab.TAB_NAME, DialogsTab.TAB_NAME};
        for(int i = 0; i < tabName.length; i++) {
            TabHost.TabSpec tabSpec;
            tabSpec = tabHost.newTabSpec(tabName[i]);
            tabSpec.setIndicator(tabName[i]);
            tabSpec.setContent(new TabContent(getApplicationContext()));
            tabHost.addTab(tabSpec);
        }
        tabHost.setOnTabChangedListener(this);

        //create tabs for viewpager
        List<Fragment> tabList = new ArrayList<Fragment>();
        tabList.add(new ProfileTab());
        tabList.add(new DialogsTab());

        //view pager create
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabList);

        viewPager.setAdapter(tabPagerAdapter);
        viewPager.setOnPageChangeListener(this);
    }

    public class TabContent implements TabHost.TabContentFactory {
        Context context;
        public TabContent(Context mContext) {
            context = mContext;
        }

        @Override
        public View createTabContent(String tag) {

            View tabView = new View(context);
            tabView.setMinimumHeight(0);
            tabView.setMinimumWidth(0);

            return tabView;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int selectedPage) {
        tabHost.setCurrentTab(selectedPage);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onTabChanged(String tabId) {
        int selectedPage = tabHost.getCurrentTab();
        viewPager.setCurrentItem(selectedPage);
    }

}
