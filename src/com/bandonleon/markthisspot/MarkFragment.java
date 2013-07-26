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
	
	public interface OnSpotEditListener {
		public void onSaveEdit(String name, int type, String desc);
		public void onCancelEdit();
		public void onLatLngOverride(float lat, float lng);
		public int getID();
	}
	private OnSpotEditListener mListener = null;

	public void setOnSpotEditListener(OnSpotEditListener listener) {
		mListener = listener;
		Log.w("MarkFragment", "setOnSpotEditListener(), listner is" + (mListener != null ? " not " : " ") + "null" );
		Log.w("MarkFragment", "listener id is " + mListener.getID());
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
		// TODO: Figure why mListener becomes null!!! :(
		Log.w("Mark Fragment", "Save Edit Clicked " + mID);
		OnSpotEditListener ac = (OnSpotEditListener) getActivity();
		if (ac != null) {
			Log.w("Mark Fragment", "Main activity id is " + ac.getID());
			// TODO: For now hack it
			mListener = ac;
		}
		if (mListener != null) {
			String name = mSpotName.getText().toString();			
			String desc = mSpotDesc.getText().toString();

			// TODO: Pick a good replacement for empty name
			if (name.isEmpty())
				name = "Default Name";
			
			// TODO: Get type from Spinner and parse into int
			int type = 1;
			
			Log.w("Mark Fragment", "Calling onSaveEdit()");
			mListener.onSaveEdit(name, type, desc);
		} else {
			Log.w("Mark Fragment", "mListener is null :(");
		}
	}
	
	public void onCancelEditClicked(View view) {
		// TODO: Figure why mListener becomes null!!! :(
		Log.w("Mark Fragment", "Cancel Edit Clicked " + mID);
		OnSpotEditListener ac = (OnSpotEditListener) getActivity();
		if (ac != null) {
			Log.w("Mark Fragment", "Main activity id is " + ac.getID());
			// TODO: For now hack it
			mListener = ac;
		}
		if (mListener != null) {
			Log.w("Mark Fragment", "Calling onCancelEdit()");
			mListener.onCancelEdit();
		} else {
			Log.w("Mark Fragment", "mListener is null :(");
		}
	}
}
