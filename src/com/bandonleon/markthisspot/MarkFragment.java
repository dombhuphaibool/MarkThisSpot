package com.bandonleon.markthisspot;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MarkFragment extends Fragment implements OnClickListener {

	private static int mID = 0;
	
	private EditText mSpotName;
	private Spinner mSpotType;
	private EditText mSpotDesc;
	
	private View mLatLngContainer;
	private EditText mSpotLat;
	private EditText mSpotLng;
	
	private long mListIdxId = 0;
	
	public interface OnSpotEditListener {
		public void onSaveEdit(long id, LocationInfo loc);
		public void onCancelEdit();
		public void onLatLngOverride(float lat, float lng);
		public int getID();
	}
	private OnSpotEditListener mListener = null;

	public void setOnSpotEditListener(OnSpotEditListener listener) {
		mListener = listener;
	}
	
	public MarkFragment() {
		mID++;
		Log.w("MarkFragment", "Constructor " + mID);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
//		if (container == null)
//			return null;
		
		View rootView = inflater.inflate(R.layout.spot_mark, container, false);

		mSpotName = (EditText) rootView.findViewById(R.id.mark_name);
		mSpotType = (Spinner) rootView.findViewById(R.id.mark_type);
		mSpotDesc = (EditText) rootView.findViewById(R.id.mark_desc);

		mLatLngContainer = rootView.findViewById(R.id.latlng_override);
		mSpotLat = (EditText) rootView.findViewById(R.id.mark_lat);
		mSpotLng = (EditText) rootView.findViewById(R.id.mark_lng);
		
		setOnClickListener((Button) rootView.findViewById(R.id.check_latlng));
		setOnClickListener((Button) rootView.findViewById(R.id.save_edit));
		setOnClickListener((Button) rootView.findViewById(R.id.cancel_edit));
		
		return rootView;
	}

	/*
	 * Public method to update all the info in our fragment
	 */
	public void updateInfo(long id, LocationInfo loc) {
		mListIdxId = id;
		
		if (mSpotName != null)
			mSpotName.setText(loc.getName());
		if (mSpotDesc != null)
			mSpotDesc.setText(loc.getDesc());
		
		if (mSpotLat != null)
			mSpotLat.setText(String.valueOf(loc.getLat()));
		if (mSpotLng != null)
			mSpotLng.setText(String.valueOf(loc.getLng()));
	}
	
	/*
	 * Button clicks handling
	 */
	private void setOnClickListener(Button btn) {
		if (btn != null)
			btn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.check_latlng: onCheckLatLngClicked(view); 	break;
			case R.id.save_edit:	onSaveEditClicked(view);		break;
			case R.id.cancel_edit:	onCancelEditClicked(view);		break;
		}
	}
	
	/*
	 * Button callbacks
	 */
	public void onCheckLatLngClicked(View view) {
		if (mListener != null) {
			float lat = 0.0f;
			float lng = 0.0f;
			
			try {
				lat = Float.parseFloat(mSpotLat.getText().toString());
				lng = Float.parseFloat(mSpotLng.getText().toString());
			} catch (NumberFormatException ex) {
				lat = 0.0f;
				lng = 0.0f;
			}
			mListener.onLatLngOverride(lat, lng);
		}
	}
	
	public void onSaveEditClicked(View view) {
		// TODO: Fix this hack!
		OnSpotEditListener ac = (OnSpotEditListener) getActivity();
		if (ac != null) {
			// TODO: For now hack it
			mListener = ac;
		}
		if (mListener != null) {
			LocationInfo loc = new LocationInfo();
			loc.setName(mSpotName.getText().toString());
			loc.setDesc(mSpotDesc.getText().toString());
			loc.setType(1);
			// TODO: Only set this if the lat & lng are visible for override
			loc.setLatLng(Float.parseFloat(mSpotLat.getText().toString()),
						  Float.parseFloat(mSpotLng.getText().toString()));
			loc.setColor(1);
			loc.setShow(1);
			
			mListener.onSaveEdit(mListIdxId, loc);
		}
	}
	
	public void onCancelEditClicked(View view) {
		// TODO: Fix this hack!
		OnSpotEditListener ac = (OnSpotEditListener) getActivity();
		if (ac != null) {
			// TODO: For now hack it
			mListener = ac;
		}
		if (mListener != null) {
			mListener.onCancelEdit();
		}
	}
}
