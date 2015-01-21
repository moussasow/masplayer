package com.mas.masstreamer.activity;

import com.mas.masstreamer.R;
import com.mas.masstreamer.adapter.TabsPagerAdapter;
import com.mas.masstreamer.utility.MasConstants.TabsName;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener{
	
	private ViewPager mViewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar mActionBar;
	public static Menu mMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mViewPager = (ViewPager) findViewById(R.id.id_pager);
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
		mViewPager.setOnPageChangeListener( onPageChangedListener );
		mViewPager.setAdapter(mAdapter);
		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		//Add tabs
		for(String tabName: TabsName.tabs){
			mActionBar.addTab(mActionBar.newTab().setText(tabName).setTabListener(this));
		}

	}	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		mMenu = menu;
		return true;		
	};
	
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
		case R.id.action_shuffle:
			
			break;
		case R.id.action_end:
			//stopService(new Intent(this,PlayerService.class));
			//removeFragment(); 
			break;
			
		case R.id.action_download:
			  mActionBar.setSelectedNavigationItem(2);
			break;
		}
		
		return super.onOptionsItemSelected(item);
	};
	
	/**
	 * 
	 */
	ViewPager.SimpleOnPageChangeListener onPageChangedListener = new ViewPager.SimpleOnPageChangeListener(){
		
		@Override	
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			
		};
		
		@Override
		public void onPageScrollStateChanged(int state) {
			
		};
	};

	
	
	

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction transaction) {
		mViewPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

}
