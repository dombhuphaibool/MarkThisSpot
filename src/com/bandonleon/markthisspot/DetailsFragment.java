package com.bandonleon.markthisspot;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
// import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// import android.widget.Toast;

/******************************************************************************
 * 
 * @author 		Dom Bhuphaibool
 * 				dombhuphaibool@yahoo.com
 * 
 * Created: 	19 July 2013
 * Modified:	30 July 2013
 *
 * Description: 
 * The fragment which contains the all of the spot's detail.
 * In 'VIEW' mode, it shows the name and description with the MapFragment
 * sandwiched in the middle. In 'EDiT' mode, it shows all the details 
 * that we can edit. The editable details in implemented as a fragment
 * called MarkFragment
 * 
 *****************************************************************************/
public class DetailsFragment extends Fragment 
							 implements MapFragment.OnMapListener {

    // IDs for storing tmp data (for orientation change, etc)
	private static final String KEY_LIST_IDX_ID = "ListIdxId";
	private static final String KEY_MODE = "DisplayMode";

	// The two display modes we can be in.
	public enum Mode { Display, Edit };
	private Mode mMode;

	// This should correspond to the currently selected ListView item's ID.
	// TODO: We should probably find a way to keep it only in one place so 
	// there is less chance of it being mismatched. Currently, ListFragment
	// does not know about DetailsFragment, and vice versa. An option is to
	// store it in MainActivity as both will know about MainActivity.
	private long mListIdxId = 0;

	// UI member variables...
	// private TextView mSpotName;
	// private TextView mSpotDesc;
	private View mMarkFragmentContainer = null;
	private MarkFragment mMarkFragment = null;
	private MapFragment mMapFragment = null;
	private LocationInfo mLoc = null;
	
    public MapFragment getMapFragment() { return mMapFragment; }
	// public DetailsFragment() { Log.w("DetailsFragment", "Constructor"); }

	/*
	 * Fragment Life cylce is as follows:
	 * 
	 * - Fragment is added/replaced -
	 * 
	 *   onAttach()
	 *   onCreate()
	 *   onCreateView()		<--------|
	 *   onActivityCreated()         |
	 *   onStart()                   |
	 *   onResume()                  |
	 *                               |
	 * - Fragment is active -        |
	 *                               |
	 *   onPause()                   |
	 *   onStop()                    |
	 *   onDestroyview()	---------|
	 *   onDestroy()
	 *   onDetach()
	 *   
	 * - Fragment is Destroyed - 
	 */
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
        mMode = Mode.Display;

        View rootView = inflater.inflate(R.layout.spot_detail, container, false);
        
        /*
        mSpotName = (TextView) rootView.findViewById(R.id.spot_name);
        mSpotDesc = (TextView) rootView.findViewById(R.id.spot_desc);        
        mSpotName.setTextSize(getResources().getDimension(R.dimen.textsize));
        mSpotDesc.setTextSize(getResources().getDimension(R.dimen.textsize));
		*/
        
        mMarkFragmentContainer = rootView.findViewById(R.id.spot_edit_container);

        // It's very important the we call getChildFragmentManager() instead
        // of getFragmentManager(). This is because we are trying to find
        // nested fragments, and *NOT* siblings fragments. Anytime dealing with
        // nested fragments, use getChildFragmentManager()! :)
        mMarkFragment = (MarkFragment) getChildFragmentManager().findFragmentById(R.id.spot_edit);
        mMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map_frame);

        if (mMarkFragment == null) {
        	mMarkFragment = new MarkFragment();
        	addFragment(R.id.spot_edit, mMarkFragment);        	
        }

        if (mMapFragment == null) {
        	mMapFragment = new MapFragment();
        	addFragment(R.id.map_frame, mMapFragment);
        }

		if (mMapFragment != null)
			mMapFragment.addMapListener(this);

		if (savedInstanceState != null) {
			mListIdxId = (Long) savedInstanceState.getSerializable(KEY_LIST_IDX_ID);
			mMode = (Mode) savedInstanceState.getSerializable(KEY_MODE);
			if (mLoc == null)
				mLoc = new LocationInfo();
			mLoc.loadState(savedInstanceState);			
			
			updateUI();
		}
		setMode(mMode);
		
		return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
	@Override 
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();

		// At this point, all UI member variables should be valid!
		assert(mMarkFragmentContainer != null);
		assert(mMarkFragment != null);
		assert(mMapFragment != null);
	}
	
	// Used to record transient states. eg, when the orientation changes, 
	// user clicks home button, or user clicks current tasks button. It seems
	// that onPause() is always called (and in the above scenarios), onSaveInstanceState()
	// gets called afterwards. Persistent data should be saved in onPause().
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_LIST_IDX_ID, mListIdxId);
        outState.putSerializable(KEY_MODE, mMode);
        assert(mLoc != null);
        if (mLoc == null)
        	mLoc = new LocationInfo();
        mLoc.saveState(outState);
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

	/*
	 * Helper method to update all the UI widgets
	 */
    private void updateUI() {
    	if (mLoc != null) {
    		/*
        	if (mSpotName != null)
        		mSpotName.setText(mLoc.getName());
        	if (mSpotDesc != null)
        		mSpotDesc.setText(mLoc.getDesc());
        	*/
    		// TODO: We may want to optimize this by only updating the 
    		// MarkerFragment if it is visible. To do this we must ensure
    		// that all calls to updateInfo() in MainActivity is made
    		// *AFTER* a call to setMode(EDIT). Currently, it's made before
    		// so we need to change & clean that up...
        	if (mMarkFragment != null)
        		mMarkFragment.updateInfo(mListIdxId, mLoc);
    	}
    }
    
    /*
     * List fragment's list view index id starts at 1. *** Note that it's 1-based ***
     * Therefore an id of 0 means that we have a new location item (eg, we are in 
     * the process of adding a new location). We should then get the current location
     *  and populate the info accordingly.
     */
    public void updateInfo(long id) {
    	// id of 0 means we don't have anything selected in the ListView.
    	// 		=> SpotsContentProvider.NEW_LOC_ID
    	// id of 1 means we have the 'Current Location' selected.
    	//		=> SpotsContentProvider.CURR_LOC_ID
    	// In either of these cases, do not populate the UI fields, 
    	// and update the latitude and longitude to be our current location.
    	if (id <= SpotsContentProvider.CURR_LOC_ID) {
    		// Only clear the UI info if it was previously populated by a 
    		// valid location. If we are currently in new location context
    		// mListIdxId == 0, don't re-populate the UI. Use case is when 
    		// the user is moving the map around to find the new location point.
    		if (id == SpotsContentProvider.CURR_LOC_ID || id != mListIdxId) {
    			mListIdxId = id;
	    		mLoc = (mMapFragment != null) ? 
	    			   ((id == SpotsContentProvider.CURR_LOC_ID) ?
	    					   mMapFragment.getCurrLocation() : 
	    					   mMapFragment.getCurrMapLocation()) 
	    		       : new LocationInfo();
	    		updateUI();
	    		
	        	if (mMapFragment != null)
	        		mMapFragment.animateCamera(mLoc.getLat(), mLoc.getLng());
    		}
    		return;
    	}
    	
    	mListIdxId = id;
    	Activity activity = getActivity();
    	if (activity != null) {
	        Cursor c = activity.getContentResolver().query(
	        	Uri.withAppendedPath(SpotsContentProvider.CONTENT_URI, String.valueOf(id)),
	        	SpotsContentProvider.PROJECTION_ALL, "", null, null);
	        if (c != null && c.getCount() > 0) {
	        	c.moveToFirst();
	        	mLoc = new LocationInfo();
	        	mLoc.setName(c.getString(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_NAME)));
	        	mLoc.setDesc(c.getString(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_DESC)));
	        	mLoc.setType(c.getString(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_TYPE)));
	        	mLoc.setLatLng(c.getFloat(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_LAT)),
	        		c.getFloat(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_LNG)));
	        	mLoc.setColor(c.getInt(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_COLOR)));
	        	mLoc.setShow(c.getInt(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_SHOW)));

	        	updateUI();
	        	
	        	if (mMapFragment != null)
	        		mMapFragment.animateCamera(mLoc.getLat(), mLoc.getLng());
	        }
    	}
    }
    
    public void checkLatLng(double lat, double lng) {
    	if (mMapFragment != null)
    		mMapFragment.animateCamera(lat, lng);
    }

    public Mode getMode() { return mMode; }
    public void setMode(Mode mode) {
    	// TODO: Should we check if the mode is not the same for optimization?
    	// if (mode != mMode) {
			
    	/*
        	int vis = (mode == Mode.Display) ? View.VISIBLE : View.GONE;
        	if (mSpotName != null)
        		mSpotName.setVisibility(vis);
        	if (mSpotDesc != null)
        		mSpotDesc.setVisibility(vis);        	
        */
    	showEdit(mode == Mode.Edit);
    	mMode = mode;   		
    	// }
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
    
    /*
     * MapFragment.OnMapListener callbacks
     */
    public void onMapConnected() {
		// Ensure that the first item (index "1" - *note* that index
		// for querying is 1-based and *NOT* 0-based) in the List fragment 
		// is the current position. For now just check if index 1 
		// exists. The first time the app is installed it will note be.
		// After which, we'll add logic so that this item cannot be 
		// deleted.
    	if (mMapFragment != null) {
    		MainActivity activity = (MainActivity) getActivity();
    		if (activity != null) {
        		LocationInfo loc = mMapFragment.getCurrLocation();
        		loc.setName(LocationInfo.CURR_LOC_NAME);
        		loc.setDesc(LocationInfo.CURR_LOC_DESC);
        		activity.updateCurrentLocation(loc);    			
    		}
    	}

    	// Now that we're connected, get the current location...
		if (mLoc == null)
			mLoc = mMapFragment.getCurrLocation();
    }
    
    public void onMapDisconnected() {}
    
    public void onMapClick(double lat, double lng) {
    	// Toast.makeText(getActivity(), "onMapClick", Toast.LENGTH_SHORT).show();
    }
    
    public void onMapLongClick(double lat, double lng) {
    	// Toast.makeText(getActivity(), "onMapLongClick", Toast.LENGTH_SHORT).show();    	
    }
    
    public void onCameraChange(double lat, double lng) {
    	if (mMarkFragment != null)
    		mMarkFragment.updateLatLng(lat, lng);
    	
    	// Toast.makeText(getActivity(), "onCameraChange", Toast.LENGTH_SHORT).show();    	
    }
    
    public void onMarkerClick(long id) {}
    public void onInfoWindowClick(long id) {}
}
