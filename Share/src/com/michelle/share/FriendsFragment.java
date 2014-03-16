package com.michelle.share;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FriendsFragment extends ListFragment implements PeerListListener{
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_friends, peers));
	}

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	ProgressDialog progressDialog = null;
	
	View mContentView = null;
	
	public FriendsFragment() {
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((DeviceActionListener) getActivity()).startChat(device);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_friends,	null);
		return mContentView;
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.peers.clear();
        this.peers.addAll(peers.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (this.peers.size() == 0) {
            Log.d(MainActivity.TAG, "No devices found");
            return;
        }
	}
	
	private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

		private List<WifiP2pDevice> items;
		public WiFiPeerListAdapter(Context context, int resource,
				List<WifiP2pDevice> objects) {
			super(context, resource, objects);
			items = objects;
		}
		/* (non-Javadoc)
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row_friends, null);
			}
			WifiP2pDevice device = items.get(position);
			if (device != null) {
				TextView  top = (TextView) v.findViewById(R.id.device_name);
				TextView bottom = (TextView) v.findViewById(R.id.device_details);
				
				if (top != null) {
					top.setText(device.deviceName);
				}
				if (bottom != null) {
					bottom.setText(getDeviceStatus(device.status));
				}
			}
			return v;
		}		
	}
	
	public void clearPeers() {
		peers.clear();
		((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
	}
	
	/**
	 * 
	 */
	public void onInitiateDiscovery() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		progressDialog =  ProgressDialog.show(getActivity(),"Press back to cancel", "finding peers", true,
                true, new DialogInterface.OnCancelListener() {

	            @Override
	            public void onCancel(DialogInterface dialog) {
	            	
	            } 
            });
	}
	
	private String getDeviceStatus(int status) {

		Log.d(MainActivity.TAG, "Peer status :" + status);
        switch (status) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
	}
	
	/**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
	public interface DeviceActionListener {
		
		void startChat(WifiP2pDevice device);

        void cancelDisconnect();

        void connect(WifiP2pConfig config);

        void disconnect();
	}
}
