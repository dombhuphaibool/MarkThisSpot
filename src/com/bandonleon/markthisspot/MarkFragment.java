package com.bandonleon.markthisspot;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/******************************************************************************
 * 
 * @author 		Dom Bhuphaibool
 * 				dombhuphaibool@yahoo.com
 * 
 * Created: 	19 July 2013
 * Modified:	30 July 2013
 *
 * Description:
 * MarkFragment is a fragment that implements all editing UI for the
 * spot's details. It can be shown or hidden depending on whether you
 * would like to see the details of a spot.
 * 
 *****************************************************************************/
public class MarkFragment extends Fragment implements OnClickListener {
	// UI member variables
	private EditText mSpotName;
	private Spinner mSpotType;
	private EditText mSpotDesc;
	
	private View mLatLngContainer;
	private EditText mSpotLat;
	private EditText mSpotLng;

	private Button mDeleteBtn;
	
	// This should correspond to the currently selected ListView item's ID.
	// TODO: We should probably find a way to keep it only in one place so 
	// there is less chance of it being mismatched. Currently, ListFragment
	// does not know about DetailsFragment, and vice versa. An option is to
	// store it in MainActivity as both will know about MainActivity.
	private long mListIdxId = 0;
	
	// An array to help us convert from type array name to array index.
	// Used for setting the Spinner by index.
	private String[] mTypeList = {};

	// Provide an interface to notify clients of actions in this fragment.
	public interface OnSpotEditListener {
		public void onSaveEdit(long id, LocationInfo loc);
		public void onDeleteEdit(long id);
		public void onCancelEdit();
		public void onLatLngCheck(double lat, double lng);
	}
	private OnSpotEditListener mListener = null;

	public void setOnSpotEditListener(OnSpotEditListener listener) {
		mListener = listener;
	}
	
	// public MarkFragment() { Log.w("MarkFragment", "Constructor"); }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.spot_mark, container, false);

		mSpotName = (EditText) rootView.findViewById(R.id.mark_name);
		mSpotType = (Spinner) rootView.findViewById(R.id.mark_type);
		mSpotDesc = (EditText) rootView.findViewById(R.id.mark_desc);

		mLatLngContainer = rootView.findViewById(R.id.latlng_override);
		mSpotLat = (EditText) rootView.findViewById(R.id.mark_lat);
		mSpotLng = (EditText) rootView.findViewById(R.id.mark_lng);
		
		mDeleteBtn = (Button) rootView.findViewById(R.id.delete_edit);
		setOnClickListener(mDeleteBtn);
		setOnClickListener((Button) rootView.findViewById(R.id.check_latlng));
		setOnClickListener((Button) rootView.findViewById(R.id.save_edit));
		setOnClickListener((Button) rootView.findViewById(R.id.cancel_edit));
		
		return rootView;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        // activity should never be null here, but just in case...
        if (activity != null) {
			if (mSpotType != null) {
				ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
						R.array.loc_type_values, android.R.layout.simple_spinner_item);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				mSpotType.setAdapter(adapter);	
			}
			if (mTypeList.length == 0) {
				mTypeList = activity.getResources().getStringArray(R.array.loc_type_values);
			}
        }
    }

    @Override
    public void onResume() {
    	super.onResume();
		if (mDeleteBtn != null)
			mDeleteBtn.setEnabled(mListIdxId != 0);
		
		// At this point, all the UI member variables should be valid!
		assert(mSpotName != null);
		assert(mSpotType != null);
		assert(mSpotDesc != null);
		assert(mLatLngContainer != null);
		assert(mSpotLat != null);
		assert(mSpotLng != null);
		assert(mDeleteBtn != null);
    }
    
	// TODO: This is a hack for the time being.
    // TODO: High priority => Fix this hack!
    private void validateListener() {
		OnSpotEditListener ac = (OnSpotEditListener) getActivity();
		if (ac != null) {
			mListener = ac;
		}
    }
    
    /*
     * Helper method to find the list index of the location type.
     * Return 0 if not found or the appropriate index if found.
     */
    private int findTypeIndex(String type) {
    	int idx = 0;
    	if (mTypeList.length > 0) {
    		while (idx<mTypeList.length && !mTypeList[idx].equals(type))
    			++idx;
    	}
    	return(idx >= mTypeList.length ? 0 : idx);
    }
    
    /*
     * Public method to show/hide the latitude and longitude override
     */
    public void showLatLngOverride(boolean show) {
    	if (mLatLngContainer != null) {
    		mLatLngContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    	}
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
		if (mSpotType != null)
			mSpotType.setSelection(findTypeIndex(loc.getType()));
		if (mSpotLat != null)
			mSpotLat.setText(String.valueOf(loc.getLat()));
		if (mSpotLng != null)
			mSpotLng.setText(String.valueOf(loc.getLng()));
		if (mDeleteBtn != null)
			mDeleteBtn.setEnabled(mListIdxId != 0);
	}
	
	public void updateLatLng(double lat, double lng) {
		if (mLatLngContainer.isShown()) {
			if (mSpotLat != null)
				mSpotLat.setText(String.valueOf(lat));
			if (mSpotLng != null)
				mSpotLng.setText(String.valueOf(lng));
		}
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
			case R.id.delete_edit:	onDeleteEditClicked(view);		break;
			case R.id.cancel_edit:	onCancelEditClicked(view);		break;
		}
	}
	
	/*
	 * Button callbacks
	 */
	public void onCheckLatLngClicked(View view) {
		validateListener();

		if (mListener != null) {
			double lat = 0.0;
			double lng = 0.0;
			
			try {
				lat = Double.parseDouble(mSpotLat.getText().toString());
				lng = Double.parseDouble(mSpotLng.getText().toString());
			} catch (NumberFormatException ex) {
				lat = 0.0;
				lng = 0.0;
			}
			mListener.onLatLngCheck(lat, lng);
		}
	}
	
	public void onSaveEditClicked(View view) {
		validateListener();
		if (mListener != null) {
			LocationInfo loc = new LocationInfo();
			loc.setName(mSpotName.getText().toString());
			loc.setDesc(mSpotDesc.getText().toString());
			loc.setType(mSpotType.getSelectedItem().toString());
			// TODO: Only set this if the lat & lng are visible for override
			loc.setLatLng(Float.parseFloat(mSpotLat.getText().toString()),
						  Float.parseFloat(mSpotLng.getText().toString()));
			loc.setColor(1);
			loc.setShow(1);
			
			mListener.onSaveEdit(mListIdxId, loc);
		}
	}

	public void onDeleteEditClicked(View view) {
		if (mListIdxId != 0) {
			validateListener();
			
			if (mListener != null) {
				mListener.onDeleteEdit(mListIdxId);
			}		
		}
	}
	
	public void onCancelEditClicked(View view) {
		validateListener();
		
		if (mListener != null) {
			mListener.onCancelEdit();
		}
	}
}
