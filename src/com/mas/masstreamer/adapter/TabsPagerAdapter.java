package com.mas.masstreamer.adapter;


import com.mas.masstreamer.fragments.DirectoryFragment;
import com.mas.masstreamer.fragments.DownloadFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {
		Fragment fragment = null;
		switch(index){
		case 0:
			fragment = new DirectoryFragment();
			break;
		case 1:
			fragment = new DownloadFragment();
			break;
		case 2:
			fragment = new DirectoryFragment();
			break;
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return 3;
	}

}
