package com.michelle.share;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import com.michelle.share.FriendsFragment.DeviceActionListener;
import com.michelle.share.UserInfoFragment.UserInfoFragListener;
import com.michelle.share.db.ShareFileService;
import com.michelle.share.socket.ShareChatService;
import com.michelle.share.socket.ShareChatService.MyBinder;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener, ChannelListener,
		DeviceActionListener, ConnectionInfoListener, UserInfoFragListener {

	public static final String TAG = "Share App";

	public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
	
	private WifiP2pDnsSdServiceRequest serviceRequest;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	private TabHost mTabHost;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();

	private WifiP2pManager manager;
	private boolean isWifiP2pEnabled = false;
	private boolean retryChannel = false;

	private final IntentFilter intentFilter = new IntentFilter();
	private Channel channel;
	private BroadcastReceiver receiver = null;

	private FriendsFragment mFriendsFrag = null;
	private UserInfoFragment mUserInfoFrag = null;
	private HistoryFilesFragment mHistoryFilesFrag = null;
	private WifiP2pDevice device = null;
	private ProgressDialog progressDialog = null;

	private MyBinder myBinder = null;
	
	/**
     * 
     * @author mwho Maintains extrinsic info of a tab's construct
     */
    private class TabInfo {
        private String tag;
        private Class<?> clazz;
        private Bundle args;
        private Fragment fragment;

        TabInfo(String tag, Class<?> clazz, Bundle args) {
            this.tag = tag;
            this.clazz = clazz;
            this.args = args;
        }

    }

	class TabFactory implements TabContentFactory {
		 
        private final Context mContext;
 
        /**
         * @param context
         */
        public TabFactory(Context context) {
            mContext = context;
        }
 
        /** (non-Javadoc)
         * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
         */
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// add necessary intent values to be matched.

		//unbindService(null);
		Intent bindIntent = new Intent(this, ShareChatService.class);  
        bindService(bindIntent, new ServiceConnection() {  
        	  
            @Override  
            public void onServiceDisconnected(ComponentName name) {  
            }

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				myBinder = (MyBinder) service; 
				
			}}, BIND_AUTO_CREATE); 
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mTabHost = (TabHost) findViewById(R.id.tabhost);
		mTabHost.setup();
//		View view = LayoutInflater.from(this).inflate(R.layout.tab_layout, null);
		
		TabInfo tabInfo = null;
		AddTab(this, this.mTabHost,
				this.mTabHost.newTabSpec("Tab1").setIndicator(createTabView(getString(R.string.title_section1))), 
				( tabInfo = new TabInfo("Tab1", FriendsFragment.class, savedInstanceState)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		AddTab(this, this.mTabHost, 
				this.mTabHost.newTabSpec("Tab2").setIndicator(createTabView(getString(R.string.title_section2))), 
				( tabInfo = new TabInfo("Tab2", HistoryFilesFragment.class, savedInstanceState)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
	
		AddTab(this, this.mTabHost, 
				this.mTabHost.newTabSpec("Tab3").setIndicator(createTabView(getString(R.string.title_section3))), 
				( tabInfo = new TabInfo("Tab3", UserInfoFragment.class, savedInstanceState)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		
		mTabHost.setOnTabChangedListener(this);
		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
		}

//		List<Fragment> fragments = new Vector<Fragment>();
//        fragments.add(Fragment.instantiate(this, FriendsFragment.class.getName()));
//        fragments.add(Fragment.instantiate(this, HistoryFilesFragment.class.getName()));
//        fragments.add(Fragment.instantiate(this, UserInfoFragment.class.getName()));
//        this.mSectionsPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(this);

//		// For each of the sections in the app, add a tab to the action bar.
//		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//			// Create a tab with text corresponding to the page title defined by
//			// the adapter. Also specify this Activity object, which implements
//			// the TabListener interface, as the callback (listener) for when
//			// this tab is selected.
//			actionBar.addTab(actionBar.newTab()
//					.setText(mSectionsPagerAdapter.getPageTitle(i))
//					.setTabListener(this));
//		}
		
		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);
		startRegistrationAndDiscovery();
		
		((ShareApplication) getApplication()).mainActivity = this;
	}
	
	private View createTabView(String text ){
		View view = LayoutInflater.from(this).inflate(R.layout.tab_layout, null);
		TextView tab_tv = (TextView) view.findViewById(R.id.title);
		tab_tv.setText(text);
		return view;
		
	}

	private static void AddTab(MainActivity activity,
        TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
        // Attach a Tab view factory to the spec
        tabSpec.setContent(activity.new TabFactory(activity));
        tabHost.addTab(tabSpec);
    }
	
	/** (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	protected void onSaveInstanceState(Bundle outState) {	 
		outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
		super.onSaveInstanceState(outState);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			this.mTabHost.setCurrentTabByTag(savedInstanceState
					.getString("tab")); // set the tab as per the saved state
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
	 
	private void startRegistrationAndDiscovery() {
		Map<String, String> record = new HashMap<String, String>();
	    record.put(TXTRECORD_PROP_AVAILABLE, "visible");
	
	    WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
	            SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
	    manager.addLocalService(channel, service, new ActionListener() {
	
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
	
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				
			}
	    });
	
	    discoverService();
	
	}

	private void discoverService() {
		 /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */

        manager.setDnsSdResponseListeners(channel,
                new DnsSdServiceResponseListener() {

					@Override
					public void onDnsSdServiceAvailable(String instanceName,
							String registrationType, WifiP2pDevice srcDevice) {
						// A service has been discovered. Is this our app?

                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

                            // update the UI and add the item the discovered
                            // device.
//                            if (fragment != null) {
//                                WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
//                                        .getListAdapter());
//                                WiFiP2pService service = new WiFiP2pService();
//                                service.device = srcDevice;
//                                service.instanceName = instanceName;
//                                service.serviceRegistrationType = registrationType;
//                                adapter.add(service);
//                                adapter.notifyDataSetChanged();
//                                Log.d(TAG, "onBonjourServiceAvailable "
//                                        + instanceName);
//                            }
                        }
					}
                }, new DnsSdTxtRecordListener() {

                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        Log.d(TAG,
                                device.deviceName + " is "
                                        + record.get(TXTRECORD_PROP_AVAILABLE));
                    }
                });

        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new ActionListener() {

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(int arg0) {
                    }
                });
        manager.discoverServices(channel, new ActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int arg0) {

            }
        });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.FragmentActivity#onAttachFragment(android.support
	 * .v4.app.Fragment)
	 */
	@Override
	public void onAttachFragment(Fragment fragment) {
		if (fragment.getClass() == FriendsFragment.class) {
			mFriendsFrag = (FriendsFragment) fragment;
		} else if (fragment.getClass() == UserInfoFragment.class) {
			mUserInfoFrag = (UserInfoFragment) fragment;
		} else if (fragment.getClass() == HistoryFilesFragment.class) {
			mHistoryFilesFrag = (HistoryFilesFragment) fragment;
			ReceivedFilesBroadcastReceiver myReceiver = new ReceivedFilesBroadcastReceiver(mHistoryFilesFrag);
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.intent.action.FILE_RECEIVE");
			
			registerReceiver(myReceiver,filter);
		}
		registerReceiver(receiver, intentFilter);
		super.onAttachFragment(fragment);
	}
	
	

	public FriendsFragment getFriendsFrag() {

		return mFriendsFrag;
	}

	public UserInfoFragment getUserInfoFrag() {
		return mUserInfoFrag;
	}

	public HistoryFilesFragment getHistoryFilesFrag() {
		return mHistoryFilesFrag;
	}

	/** register the BroadcastReceiver with the intent values to be matched */
	@Override
	public void onResume() {
		super.onResume();
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		// registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		// unregisterReceiver(receiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.atn_direct_enable:
			if (manager != null && channel != null) {

				// Since this is the system wireless settings activity, it's
				// not going to send us a result. We will be notified by
				// WiFiDeviceBroadcastReceiver instead.

				startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
			} else {
				Log.e(TAG, "channel or manager is null");
			}
			return true;

		case R.id.atn_direct_discover:
			if (!isWifiP2pEnabled) {
				Toast.makeText(MainActivity.this, R.string.p2p_off_warning,
						Toast.LENGTH_SHORT).show();
				return true;
			}
			final FriendsFragment fragment = mFriendsFrag;
			fragment.onInitiateDiscovery();
			manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

				@Override
				public void onSuccess() {
					Toast.makeText(MainActivity.this, "Discovery Initiated",
							Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onFailure(int reasonCode) {
					Toast.makeText(MainActivity.this,
							"Discovery Failed : " + reasonCode,
							Toast.LENGTH_SHORT).show();
				}
			});
			return true;
		case R.id.atn_settings:
			disconnect();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
//
//	@Override
//	public void onTabSelected(ActionBar.Tab tab,
//		FragmentTransaction fragmentTransaction) {
//		// When the given tab is selected, switch to the corresponding page in
//		// the ViewPager.
//		mViewPager.setCurrentItem(tab.getPosition());
//	}
//
//	@Override
//	public void onTabUnselected(ActionBar.Tab tab,
//		FragmentTransaction fragmentTransaction) {
//	}
//
//	@Override
//	public void onTabReselected(ActionBar.Tab tab,
//		FragmentTransaction fragmentTransaction) {
//	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.

			Fragment fragment;
			Bundle args = new Bundle();
			switch (position) {
			case 0:
				fragment = new FriendsFragment();
				args.putInt(FriendsFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				break;
			case 1:
				fragment = new UserInfoFragment();
				args.putInt(UserInfoFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				break;
			case 2:
				fragment = new HistoryFilesFragment();
				args.putInt(HistoryFilesFragment.ARG_SECTION_NUMBER,
						position + 1);
				fragment.setArguments(args);
				break;
			default:
				fragment = new FriendsFragment();
				args.putInt(FriendsFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				break;
			}
			return fragment;

		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	@Override
	public void onChannelDisconnected() {
		// we will try once more
		if (manager != null && !retryChannel) {
			Toast.makeText(this, "Channel lost. Trying again",
					Toast.LENGTH_LONG).show();
			resetData();
			retryChannel = true;
			manager.initialize(this, getMainLooper(), this);
		} else {
			Toast.makeText(
					this,
					"Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
					Toast.LENGTH_LONG).show();
		}
	}

	public void setIsWifiP2pEnabled(boolean b) {
		isWifiP2pEnabled = b;
	}

	@Override
	public void startChat(WifiP2pDevice device) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, ChatActivity.class);
		startActivity(intent);
	}

	@Override
	public void cancelDisconnect() {

		/*
		 * A cancel abort request by user. Disconnect i.e. removeGroup if
		 * already connected. Else, request WifiP2pManager to abort the ongoing
		 * request
		 */
		if (manager != null) {
			if (device == null
					|| device.status == WifiP2pDevice.CONNECTED) {
				disconnect();
			} else if (device.status == WifiP2pDevice.AVAILABLE
					|| device.status == WifiP2pDevice.INVITED) {

				manager.cancelConnect(channel, new ActionListener() {

					@Override
					public void onSuccess() {
						Toast.makeText(MainActivity.this,
								"Aborting connection", Toast.LENGTH_SHORT)
								.show();
					}

					@Override
					public void onFailure(int reasonCode) {
						Toast.makeText(
								MainActivity.this,
								"Connect abort request failed. Reason Code: "
										+ reasonCode, Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
		}

	}

	@Override
	public void connect(WifiP2pConfig config) {
		if (serviceRequest != null)
            manager.removeServiceRequest(channel, serviceRequest,
                    new ActionListener() {

                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int arg0) {
                        }
                    });
		
		manager.connect(channel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				// WiFiDirectBroadcastReceiver will notify us. Ignore for now.
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(MainActivity.this, "Connect failed. Retry.",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void disconnect() {
		manager.removeGroup(channel, new ActionListener() {

			@Override
			public void onFailure(int reasonCode) {
				Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

			}

			@Override
			public void onSuccess() {
			}

		});
		mFriendsFrag.clearPeers();
		((ShareApplication) getApplication()).setIsConnected(false);
	}
	
	public WifiP2pDevice getDevice() {
		return device;
	}
	
	/**
	 * Update User Information at UserInfoFragment
	 * @param device
	 */
	public void updateThisDevice(WifiP2pDevice device) {
		this.device = device;
		if (mUserInfoFrag != null && device != null) {
			mUserInfoFrag.ShowDeviceDetails(device);
		}
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
		
		((ShareApplication) getApplication()).setIsConnected(true);
        
        if (myBinder != null) {
        	myBinder.connect(info);
        }
	}

	@Override
	public void updateUserFragUI() {
		if (mUserInfoFrag != null && device != null) {
			mUserInfoFrag.ShowDeviceDetails(device);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		
	}
	

	public void resetData() {
		if (mFriendsFrag != null) {
			mFriendsFrag.clearPeers();
		}
		if (mUserInfoFrag != null) {
			mUserInfoFrag.resetViews();
		}
		
//		// close Chat socket
//		try {
//			((ShareApplication) getApplication()).getChatSocket().close();
//		} catch (Exception e) {
//		}
//		
//		// close transfer socket
//		try {
//			((ShareApplication) getApplication()).getTransferSocket().close();
//		} catch (Exception e) {
//		}
//		
//		// close WiFi p2p connection
//		this.disconnect();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		this.mTabHost.setCurrentTab(position);
	}

	@Override
	public void onTabChanged(String tabId) {
		int pos = this.mTabHost.getCurrentTab();
		this.mViewPager.setCurrentItem(pos);
	}
	
	public class ReceivedFilesBroadcastReceiver extends BroadcastReceiver {

		private HistoryFilesFragment fragement;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("OnReceiver");

			if (intent.getAction().equals("android.intent.action.FILE_RECEIVE")) {
				// notify receive a message. 
				if (fragement != null) {
					fragement.UpdateAdapterData();
				} 
			}
		}

		public ReceivedFilesBroadcastReceiver(HistoryFilesFragment fragement) {
			this.fragement = fragement;
			System.out.println("ReceivedFilesBroadcastReceiver");
		}
	}
}
