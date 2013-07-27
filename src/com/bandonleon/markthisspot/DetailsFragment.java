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
import android.widget.Toast;

/******************************************************************************
 * 
 * @author 		Dom Bhuphaibool
 * 				dombhuphaibool@yahoo.com
 * 
 * Created: 	19 July 2013
 * Modified:	27 July 2013
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

	private static final String KEY_LIST_IDX_ID = "ListIdxId";
	private static final String KEY_MODE = "DisplayMode";
	public enum Mode { Display, Edit };
	
	private long mListIdxId = 0;
	private Mode mMode;
	private TextView mSpotName;
	private TextView mSpotDesc;
	private View mMarkFragmentContainer;
	private MapFragment mMapFragment = null;
	private MarkFragment mMarkFragment = null;
	private LocationInfo mLoc = null;
	
    public MapFragment getMapFragment() { return mMapFragment; }
	public DetailsFragment() {
		Log.w("DetailsFragment", "Constructor");
	}

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
			mMapFragment.setMapListener(this);

		if (savedInstanceState != null) {
			mListIdxId = (Long) savedInstanceState.getSerializable(KEY_LIST_IDX_ID);
			mMode = (Mode) savedInstanceState.getSerializable(KEY_MODE);
			if (mLoc == null)
				mLoc = new LocationInfo();
			mLoc.loadState(savedInstanceState);			
			
			setMode(mMode);
			updateUI();
		}
		
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
        	if (mSpotName != null)
        		mSpotName.setText(mLoc.getName());
        	if (mSpotDesc != null)
        		mSpotDesc.setText(mLoc.getDesc());
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
    	mListIdxId = id;
    	if (id == 0) {
    		mLoc = (mMapFragment != null) ? 
    				mMapFragment.getCurrLocation() : new LocationInfo();
    		updateUI();
    		return;
    	}
    	
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
        		// Hack - since this method is a callback
        		// TODO: Find a nicer way to do this...
        		activity.onSaveEdit(1, loc);    			
    		}
    	}

    	// Now that we're connected, get the current location...
		if (mLoc == null)
			mLoc = mMapFragment.getCurrLocation();

    	/*
    	if (mMapFragment != null && mLoc != null)
    		mMapFragment.animateCamera(mLoc.getLat(), mLoc.getLng());
    	*/
    }
    
    public void onMapDisconnected() {
    	
    }
    
    public void onMapClick(double lat, double lng) {
    	Toast.makeText(getActivity(), "onMapClick", Toast.LENGTH_SHORT).show();
    }
    
    public void onMapLongClick(double lat, double lng) {
    	Toast.makeText(getActivity(), "onMapLongClick", Toast.LENGTH_SHORT).show();    	
    }
    
    public void onCameraChange(double lat, double lng) {
    	Toast.makeText(getActivity(), "onCameraChange", Toast.LENGTH_SHORT).show();    	
    }
}
