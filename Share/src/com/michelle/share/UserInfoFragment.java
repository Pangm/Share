package com.michelle.share;

import android.app.ProgressDialog;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UserInfoFragment extends Fragment {
	public static final String ARG_SECTION_NUMBER = null;
	
	public static final String TAG = "UserInfoFragment";
	
	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    ProgressDialog progressDialog = null;
    
	public UserInfoFragment() {
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_user_info,
				container, false);

		((UserInfoFragListener) getActivity()).updateUserFragUI();
		return mContentView;
	}

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
    	TextView view = (TextView) mContentView.findViewById(R.id.my_name);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.my_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.my_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
    }
	
    /**
	 * Update UI for User device information
	 * @param device
	 */
	void ShowDeviceDetails(WifiP2pDevice device) {
		this.device = device;
		TextView view = (TextView) mContentView.findViewById(R.id.my_name);
		view.setText(device.deviceName);
        view = (TextView) mContentView.findViewById(R.id.my_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.my_info);
        view.setText(device.toString());
	}
	
	public interface UserInfoFragListener {
		public void updateUserFragUI();
	}
}
