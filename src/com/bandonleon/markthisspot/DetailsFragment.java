package com.bandonleon.markthisspot;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsFragment extends Fragment {

	public enum Mode { Display, Edit };
	
	private Mode mMode;
	private TextView mSpotName;
	private TextView mSpotDesc;
	private View mMarkFragmentContainer;
	private MapFragment mMapFragment = null;
	private MarkFragment mMarkFragment = null;

	public DetailsFragment() {
		Log.w("DetailsFragment", "Constructor");
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		/*
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
		*/
		
        mMode = Mode.Display;

        View rootView = inflater.inflate(R.layout.spot_detail, container, false);
        
        mSpotName = (TextView) rootView.findViewById(R.id.spot_name);
        mSpotDesc = (TextView) rootView.findViewById(R.id.spot_desc);        
        mSpotName.setTextSize(getResources().getDimension(R.dimen.textsize));
        mSpotDesc.setTextSize(getResources().getDimension(R.dimen.textsize));

        mMarkFragmentContainer = rootView.findViewById(R.id.spot_edit_container);
        
        mMarkFragment = (MarkFragment) getFragmentManager().findFragmentById(R.id.spot_edit);
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_frame);
  
        if (mMarkFragment == null) {
        	mMarkFragment = new MarkFragment();
        	addFragment(R.id.spot_edit, mMarkFragment);        	
        }

        if (mMapFragment == null) {
        	mMapFragment = new MapFragment();
        	addFragment(R.id.map_frame, mMapFragment);
        }

		return rootView;
    }

	/*
	 * Helper method to add a nested fragment to this fragment
	 */
	public void addFragment(int id, Fragment f) {
		if (f != null) {
			// Execute a transaction, replacing any existing fragment
			// with this one inside the frame.
			// *** Note *** We call the new getChildFragmentManager() to
			// nest a fragment (MapFragment or MarkFragment) within our 
			// fragment (DetailsFragment)
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			ft.replace(id, f);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		}		
	}

    // Order is:
    //		onAttach(), onCreate(), *=> onCreateView(), onActivityCreated(), onStart(), onResume()
    //		- Fragment is Active -
    //		onPause(), onStop(), onDestroyView() *=>, onDestroy(), onDetach()
    //		- Fragment is Destroyed - 
    //
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO:
        // showEdit is causing a crash if we rotate the screen from protrait to landscape & vice versa
        // showEdit(false);
    }
    
    public MapFragment getMapFragment() { return mMapFragment; }
    /*
	public void setMapFragment(MapFragment mapFragment) { mMapFragment = mapFragment; }
	public void setMarkFragment(MarkFragment markFragment) { mMarkFragment = markFragment; }
    */
    
    /*
     * List fragment's list view index id starts at 1. *** Note that it's 1-based ***
     * Therefore an id of 0 means that we have a new location item (eg, we are in 
     * the process of adding a new location). We should then get the current location
     *  and populate the info accordingly.
     */
    public void updateInfo(long id) {
    	if (id == 0) {
    		// New location => get current location...
    		// TODO: need to implement this...
    		LocationInfo loc = new LocationInfo();
        	if (mSpotName != null)
        		mSpotName.setText(loc.getName());
        	if (mSpotDesc != null)
        		mSpotDesc.setText(loc.getDesc());
        	if (mMarkFragment != null)
        		mMarkFragment.updateInfo(id, loc);
        		
    		return;
    	}
    	
    	Activity activity = getActivity();
    	if (activity != null) {
	        Cursor c = activity.getContentResolver().query(
	        	Uri.withAppendedPath(SpotsContentProvider.CONTENT_URI, String.valueOf(id)),
	        	SpotsContentProvider.PROJECTION_ALL, "", null, null);
	        if (c != null && c.getCount() > 0) {
	        	c.moveToFirst();
	        	LocationInfo loc = new LocationInfo();
	        	loc.setName(c.getString(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_NAME)));
	        	loc.setDesc(c.getString(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_DESC)));
	        	loc.setType(c.getString(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_TYPE)));
	        	loc.setLatLng(c.getFloat(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_LAT)),
	        		c.getFloat(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_LNG)));
	        	loc.setColor(c.getInt(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_COLOR)));
	        	loc.setShow(c.getInt(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_SHOW)));
	        	
	        	if (mSpotName != null)
	        		mSpotName.setText(loc.getName());
	        	if (mSpotDesc != null)
	        		mSpotDesc.setText(loc.getDesc());
	        	if (mMarkFragment != null)
	        		mMarkFragment.updateInfo(id, loc);
	        	if (mMapFragment != null)
	        		mMapFragment.animateCamera(loc.getLat(), loc.getLng());
	        }
    	}
    }
    
    public void checkLatLng(float lat, float lng) {
    	if (mMapFragment != null)
    		mMapFragment.animateCamera(lat, lng);
    }
    
    public void setMode(Mode mode) {
		Log.d("Details Fragment", "setMode(" + mode + "), current mode is " + mMode);
//-    	if (mode != mMode) {
        	int vis = (mode == Mode.Display) ? View.VISIBLE : View.GONE;
        	if (mSpotName != null)
        		mSpotName.setVisibility(vis);
        	if (mSpotDesc != null)
        		mSpotDesc.setVisibility(vis);        	
    		showEdit(mode == Mode.Edit);
    		mMode = mode;
//-    	}
    }
    
    private void showEdit(boolean show) {
		FragmentTransaction ft = getChildFragmentManager().beginTransaction();
		if (show) {
			// TODO: fix animation...
			// ft.setCustomAnimations(R.animator.slide_down, R.animator.slide_up);
			ft.show(mMarkFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		} else {
			ft.hide(mMarkFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		}
		ft.commit();
		
		Activity activity = getActivity();
		if (activity != null) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
			boolean override = sharedPref.getBoolean(SettingsActivity.KEY_LATLNG_OVERRIDE, false);
			mMarkFragment.showLatLngOverride(override);
		}
		
		mMarkFragmentContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }    
}
