package com.bandonleon.markthisspot;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bandonleon.markthisspot.DetailsFragment.Mode;

/******************************************************************************
 * 
 * @author 		Dom Bhuphaibool
 * 				dombhuphaibool@yahoo.com
 * 
 * Created: 	19 July 2013
 * Modified:	27 July 2013
 *
 * Description: 
 * Main Activity of the application.
 ******************************************************************************/
public class MainActivity extends FragmentActivity implements SpotsFragment.OnSpotListener,
															  MarkFragment.OnSpotEditListener,
															  MapFragment.OnMapListener {

    // Debugging tag for the application
    public static final String APPTAG = "MarkThisSpot";
    // Id for storing temp data (for orientation change, etc)
    private static final String CURR_SELID = "curSelId";
    private static final String LAST_SP_MODE = "lastSinglePaneMode";
    
	private static final int ACTIVITY_SETTINGS = 1;
	private static final int ACTIVITY_MARK = 2;

	private static final int CONTAINER_DETAILS_ID = 9999;
	
	private enum ViewMode { DualPane, List, Details }
	
	// In Portrait orientation, we only show the ListView and in Landscape
	// orientation, we show the dual pane view. We need to keep track of
	// the currently selected item in the ListView because if the user is 
	// viewing it in Portrait and then decide to change the orientation to
	// Landscape, we need to display the currently selected item's details
	// when onActivityCreated() gets called again (during the creation of
	// the Landscape orientation).
    ViewMode mViewMode;
    ViewMode mLastSinglePaneMode;
    long mCurrSelectedId = 0;
    
    LinearLayout mContainer = null;
    SpotsFragment mSpotsFragment = null;
    DetailsFragment mDetailsFragment = null;
    
	/*
	 * Activity life cycle
	 * 
	 *                        - Activity launched -
	 *                                 v
	 *         ------------------> onCreate()
	 *         |                   onStart()  <--------- onRestart()
	 *         |                   onResume() <-------        ^
	 *         |                       v             |        |
	 * - App process killed - - Activity running -   |        |
	 *         ^                       v             |        |
	 *         |-----------------  onPause()  --------        |
	 *         ------------------  onStop()  ------------------
	 *                             onDestroy()
	 *                                 v
	 *                         - Activity shutdown -
	 *                         
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LocationInfo.setDefaultType(getResources().getString(R.string.loc_type_default));

		setContentView(R.layout.spots_list);
		mContainer = (LinearLayout) findViewById(R.id.fragment_container);
		
		// If we are changing orientation (Portrait to Landscape & vice versa),
		// then Android will recreate the fragments for us. Therefore, we must
		// check here to see if the fragments already exist. If not, then the
		// activity is launched from scratch, so create the fragments ourselves.
		// 
		// *** Note: For some reason, the fragment's container does not get 
		// saved nor recreated by Android. Android only recreates the fragment
		// back, so we must recreate the fragment's container view ourselves.
		// If we don't the fragment will not have a container and we'll get a
		// nasty exception. 
		//
		// TODO: Research this topic more when we have time... 
		//
		FragmentManager sfm = getSupportFragmentManager();
		// mSpotsFragment = (SpotsFragment) sfm.findFragmentById(CONTAINER_LIST_ID);
		mDetailsFragment = (DetailsFragment) sfm.findFragmentById(CONTAINER_DETAILS_ID);
		
		if (mDetailsFragment != null) {
			loadFragment(LoadMode.REPLACE, CONTAINER_DETAILS_ID, mDetailsFragment, 2.0f);
		} else {
			mDetailsFragment = new DetailsFragment();
			loadFragment(LoadMode.ADD, CONTAINER_DETAILS_ID, mDetailsFragment, 2.0f);
		}
		
		mViewMode = (getResources().getConfiguration().orientation == 
					 Configuration.ORIENTATION_LANDSCAPE) ? ViewMode.DualPane
													 	  : ViewMode.List;

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurrSelectedId = savedInstanceState.getLong(CURR_SELID, 0);
            mLastSinglePaneMode = (ViewMode) savedInstanceState.getSerializable(LAST_SP_MODE);
            // If we are in single pane mode, restore to the last single pane mode
            if (mViewMode != ViewMode.DualPane)
            	mViewMode = mLastSinglePaneMode;
        } else {
        	mLastSinglePaneMode = ViewMode.List;
        }
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(CURR_SELID, mCurrSelectedId);
        outState.putSerializable(LAST_SP_MODE, 
        	(mViewMode == ViewMode.DualPane) ? mLastSinglePaneMode : mViewMode);
    }
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (mDetailsFragment != null) {
			MapFragment mf = mDetailsFragment.getMapFragment();
			if (mf != null)
				mf.addMapListener(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		FragmentManager sfm = getSupportFragmentManager();
		mSpotsFragment = (SpotsFragment) sfm.findFragmentById(android.R.id.list);

		setSinglePaneMode(mViewMode);
	}
	
	private void setSinglePaneMode(ViewMode mode) {
		switch (mode) {
			case DualPane:
				
			break;
			
			case List:
			{
				// This is the << on the top-left of the action bar
				getActionBar().setDisplayHomeAsUpEnabled(false);
				getActionBar().setHomeButtonEnabled(false);
				
				mSpotsFragment.getView().setLayoutParams(new LinearLayout.LayoutParams(
											ViewGroup.LayoutParams.MATCH_PARENT, 
											ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.show(mSpotsFragment);
				ft.hide(mDetailsFragment);
				ft.commit();
				mViewMode = ViewMode.List;
			}
			break;
			
			case Details:
			{
				// This is the << on the top-left of the action bar
				getActionBar().setDisplayHomeAsUpEnabled(true);
				getActionBar().setHomeButtonEnabled(true);
				
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.hide(mSpotsFragment);
				ft.show(mDetailsFragment);
				ft.commit();
				mViewMode = ViewMode.Details;
			}
			break;
		}
	}
	
	/*
	 * This method will update the latitude and longitude of the first
	 * item in the ListView ('Current Location') 
	 */
	public void updateCurrentLocation(LocationInfo loc) {
		saveLocation(1, loc, true);
	}
	
	/*
	 * Helper method to save the location. First we will check if the location
	 * ID exists in the database. If so, we update the data. If not, we 
	 * perform an insert into the database. 
	 * 
	 * A return of 0 indicated that neither update nor insert was performed.
	 * A return of id > 0 means that a successful update or insert was performed.
	 */
	private long saveLocation(long id, LocationInfo loc, boolean updateIfExist) {
		long savedId = 0;
        Cursor c = getContentResolver().query(
        	Uri.withAppendedPath(SpotsContentProvider.CONTENT_URI, String.valueOf(id)),
			SpotsContentProvider.PROJECTION_ALL, "", null, null);
    		// To search for name="Current location" use the following...
    		// (SpotsContentProvider.CONTENT_URI, SpotsContentProvider.PROJECTION_ALL,
    		//  SpotsContentProvider.KEY_NAME + "=?", new String[] {"Current location"}, null);
        
        // Not really sure if it's okay if the cursor returned is null.
        // From experience if a row doesn't exist, a valid cursor is returned,
        // but the count is 0. 
        //
        // TODO: revisit this to make sure what are the ramification if the
        // cursor returned is null...
        if (c == null || c.getCount() == 0) {
        	Uri newUri = getContentResolver().insert(SpotsContentProvider.CONTENT_URI, 
        											 loc.getContentValues());
        	try {
        		savedId = Long.parseLong(newUri.getLastPathSegment());
        	} catch (NumberFormatException ex) {
        		savedId = 0;
        	}
        } else if (updateIfExist) {
    		int rowsUpdated = getContentResolver().update(
        		Uri.withAppendedPath(SpotsContentProvider.CONTENT_URI, String.valueOf(id)),
        		loc.getContentValues(), null, null);
    		if (rowsUpdated > 0)
    			savedId = id;
    		/*
    		if (c.moveToFirst()) {
	        	int nameColIdx = c.getColumnIndex(SpotsContentProvider.KEY_NAME);
	        	String name = c.getString(nameColIdx);
	        	String n = "  " + name;
        	}
        	*/
        }
        return savedId;
	}
	
	private enum LoadMode { ADD, REPLACE };
	private void loadFragment(LoadMode mode, int id, Fragment f, float weight) {
		if (mContainer != null) {
			LinearLayout fragContainer = new LinearLayout(this);
			fragContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight));
			fragContainer.setId(id);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			// Explicit check just in case we add more load modes...
			if (mode == LoadMode.ADD)
				ft.add(id, f);
			else if (mode == LoadMode.REPLACE)
				ft.replace(id, f);
			// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			mContainer.addView(fragContainer);
		}
	}
/*
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    
	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	    }
	}
*/
	private void setDetailsFragmentMode(Mode mode) {
		// Enable/Disbale the home icon in the action bar
		if (mViewMode == ViewMode.DualPane) {
			boolean enabled = (mode == Mode.Edit);
			getActionBar().setDisplayHomeAsUpEnabled(enabled);
			getActionBar().setHomeButtonEnabled(enabled);
		}
				
		// Show the MarkerFragment within the DetailsFragment
		if (mDetailsFragment != null)
			mDetailsFragment.setMode(mode);
	}
	
	@Override
    public void onBackPressed() {
    	if (mDetailsFragment != null) {
    		if (mDetailsFragment.getMode() == Mode.Edit) {
    			setDetailsFragmentMode(Mode.Display);
    			
    			// Remove tmp marker if there was one
    			MapFragment mapFragment = mDetailsFragment.getMapFragment();
    			if (mapFragment != null)
    				mapFragment.removeTmpMarker();
    			return;
    		}
    	}
    	
		if (mViewMode == ViewMode.Details) {
			setSinglePaneMode(ViewMode.List);
			return;
		}
		
    	super.onBackPressed();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// *** Note that we assume that in ViewMode.List, the
				// home icon (eg, the << icon on the top-left of the 
				// action bar) is never visible. And in DualPane mode,
				// the home icon is only visible if we are in EDIT mode
				// in the DetailsFragment. By this logic, just simulate
				// as if the user presses the back button. (same logic).
				onBackPressed();
				return true;
				
			case R.id.menu_markit:
				// Toast.makeText(this, "Mark this spot!", Toast.LENGTH_SHORT).show();
				// Intent markIntent = new Intent(this, MarkActivity.class);
				// startActivityForResult(markIntent, ACTIVITY_MARK);
				if (mDetailsFragment != null) {
					mDetailsFragment.updateInfo(mCurrSelectedId);
					setDetailsFragmentMode(Mode.Edit);
					
					// Create a marker for the new location
					if (mCurrSelectedId == 0) {
						MapFragment mapFragment = mDetailsFragment.getMapFragment();
						if (mapFragment != null)
							mapFragment.addTmpMarker();
					}
				}
				return true;
			
			case R.id.action_settings:
				// Toast.makeText(this, "Show Settings", Toast.LENGTH_SHORT).show();
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				startActivityForResult(settingsIntent, ACTIVITY_SETTINGS);
				return true;
			
			default:
				// do nothing
		}
		
		return super.onOptionsItemSelected(item);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
        	case ACTIVITY_SETTINGS:
        		MapFragment mapFragment = mDetailsFragment.getMapFragment();
        		if (mapFragment != null)
        			mapFragment.saveSettings(this);
        		break;

        	case ACTIVITY_MARK:
        		
        		break;
        		
        	default:
        		break;
        }
    }    
    
    /*
     * MarkFragment.OnSpotEditListener callbacks
     */
	public void onSaveEdit(long id, LocationInfo loc) {
		loc.validateName();
		long savedId = saveLocation(id, loc, true);
		// TODO:
		// Should we do a stricter check to see if 
		// (id == 0 && savedId > 0) just to be safe?
		if (savedId > 0) {
			// TODO: We need to find a way to also highlight the
			// the ListFragment's ListView...
			// *** TODO *** TODO *** TODO ***
			mCurrSelectedId = savedId;
		}

		// The location's name and type may have changed, 
		// reload the ListFragment.
		if (mSpotsFragment != null)
			mSpotsFragment.reloadListView();
		
		setDetailsFragmentMode(Mode.Display);
		
		if (mDetailsFragment != null) {
			MapFragment mapFragment = mDetailsFragment.getMapFragment();
			if (mapFragment != null) {
				if (id != 0) {
					mapFragment.updateMarker(id, loc);				
				} else if (mCurrSelectedId > 0) { 
					// (id == 0 && mCurrSelectedId > 0)
					mapFragment.convertTmpMarker(mCurrSelectedId, loc);
				}
			}
		}		
	}

	public void onCancelEdit() {
		/*
		if (mSpotsFragment != null)
			mSpotsFragment.reloadListView();
		*/
		
		setDetailsFragmentMode(Mode.Display);

		// Remove tmp marker if there was one
		MapFragment mapFragment = mDetailsFragment.getMapFragment();
		if (mapFragment != null)
			mapFragment.removeTmpMarker();
	}

	public void onLatLngCheck(double lat, double lng) {
		if (mDetailsFragment != null)
			mDetailsFragment.checkLatLng(lat, lng);
	}

	/*
	 * MapFragment.OnMapListener callbacks
	 */
	public void onMapConnected() {}
	public void onMapDisconnected() {}
	public void onMapClick(double lat, double lng) {}

	public void onMapLongClick(double lat, double lng) {
		// *** Note ***
		// No need to invalidate mCurrSelectedId or call 
		// DetailsFragment.updateInfo(), because the cameraChange
		// event from calling MapFragment.animateCamera() will 
		// invalidate mCurrSelectedId and call DetailsFragment.updateInfo()
		// for us.
		mShowEditOnCamChange = true;
	}

	// If we selected a new location in the SpotsFragment (ListView),
	// the camera will change position to that location. Therefore,
	// ignore the first camera change event, because we do not want
	// to clear the current list view selection.
	//
	// Ignore the first camera change event as it's delivered
	// from initialization and orientation change, because we 
	// need to maintain the mCurrSelectedId to be valid after
	// orientation changes.
	private boolean mIgnoreCamChange = true;
	private boolean mShowEditOnCamChange = false;
	public void onCameraChange(double lat, double lng) {
		if (!mIgnoreCamChange && mSpotsFragment != null) {
			// Hide the previous marker info window if it's showing
			if (mDetailsFragment != null && mCurrSelectedId != 0) {
				MapFragment mapFragment = mDetailsFragment.getMapFragment();
				if (mapFragment != null) {
					mapFragment.activateMarker(mCurrSelectedId, false);
				
					// Create a marker for the new location
					if (mDetailsFragment.getMode() == Mode.Edit)
						mapFragment.addTmpMarker(lat, lng);
				}
			}
			
			// Clear the List fragment's ListView
			mSpotsFragment.clearCurrListSelection();
			mCurrSelectedId = 0;

			// Clear the Detail fragment
			if (mDetailsFragment != null && mDetailsFragment.isVisible()) {
				mDetailsFragment.updateInfo(mCurrSelectedId);
				// When the user performs a long click on the map, we need
				// to put the DetailFragment into EDIT mode after the
				// camera change event.
				if (mShowEditOnCamChange) {
					setDetailsFragmentMode(Mode.Edit);

					// Create a marker for the new location
					MapFragment mapFragment = mDetailsFragment.getMapFragment();
					if (mapFragment != null)
						mapFragment.addTmpMarker(lat, lng);

					mShowEditOnCamChange = false;
				}
			}
		}
		mIgnoreCamChange = false;
	}
	
	public void onMarkerClick(long id) {
		// TODO: We need to find a way to also highlight the
		// the ListFragment's ListView...
		// *** TODO *** TODO *** TODO ***
		mCurrSelectedId = id;
		if (mDetailsFragment != null)
			mDetailsFragment.updateInfo(mCurrSelectedId);
		
		mIgnoreCamChange = true;
	}
	
	public void onInfoWindowClick(long id) {
		mCurrSelectedId = id;
		if (mDetailsFragment != null) {
			mDetailsFragment.updateInfo(mCurrSelectedId);
			setDetailsFragmentMode(Mode.Edit);
		}
		
		mIgnoreCamChange = true;
	}
	
	/*
	 * SpotsFragment.OnSpotsListener callbacks
	 */
	public void onSpotSelected(long id) {
		mCurrSelectedId = id;
		mIgnoreCamChange = true;
		
		if (mViewMode == ViewMode.List)
			setSinglePaneMode(ViewMode.Details);

		if (mDetailsFragment != null) {
			mDetailsFragment.updateInfo(mCurrSelectedId);
			MapFragment mapFragment = mDetailsFragment.getMapFragment();
			if (mapFragment != null)
				mapFragment.activateMarker(id, true);
		}
	}
	public void onSpotCreate() {}
	
	public void onSpotDeleted(long id) {
		if (mDetailsFragment != null) {
			// If we are deleting a location that we are editing,
			// go to DISPLAY mode in DetailsFragment.
			if (id == mCurrSelectedId && mDetailsFragment.getMode() == Mode.Edit)
				mDetailsFragment.setMode(Mode.Display);
				
			MapFragment mapFragment = mDetailsFragment.getMapFragment();
			if (mapFragment != null)
				mapFragment.removeMarker(id);
		}
	}
}
