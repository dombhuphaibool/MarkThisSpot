package com.bandonleon.markthisspot;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bandonleon.markthisspot.DetailsFragment.Mode;

public class MainActivity extends FragmentActivity implements SpotsFragment.OnSpotListener,
															  MarkFragment.OnSpotEditListener {

    // Debugging tag for the application
    public static final String APPTAG = "MarkThisSpot";
    
	private static final int ACTIVITY_SETTINGS = 1;
	private static final int ACTIVITY_MARK = 2;

	private static final int CONTAINER_LIST_ID = 9998;
	private static final int CONTAINER_DETAILS_ID = 9999;
	
	private static int mID = 0;
	
	// In Portrait orientation, we only show the ListView and in Landscape
	// orientation, we show the dual pane view. We need to keep track of
	// the currently selected item in the ListView because if the user is 
	// viewing it in Portrait and then decide to change the orientation to
	// Landscape, we need to display the currently selected item's details
	// when onActivityCreated() gets called again (during the creation of
	// the Landscape orientation).
    boolean mDualPane;
    long mCurrSelectedId = -1;
    
    LinearLayout mContainer = null;
    SpotsFragment mSpotsFragment = null;
    DetailsFragment mDetailsFragment = null;
    
	public MainActivity() {
		mID++;
		Log.w("MainActivity", "Constructor " + mID);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		mSpotsFragment = (SpotsFragment) sfm.findFragmentById(CONTAINER_LIST_ID);
		mDetailsFragment = (DetailsFragment) sfm.findFragmentById(CONTAINER_DETAILS_ID);
		
/*		if (mSpotsFragment != null) {
			if (mListContainer == null) {
				mListContainer = new LinearLayout(this);
				mListContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
				mListContainer.setId(CONTAINER_LIST_ID);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(CONTAINER_LIST_ID, mSpotsFragment);
				ft.commit();
			}
			assert(mListContainer != null);
			mContainer.addView(mListContainer);
		}
*/		if (mDetailsFragment != null) {
			loadFragment(LoadMode.REPLACE, CONTAINER_DETAILS_ID, mDetailsFragment, 2.0f);
		} else {
			mDetailsFragment = new DetailsFragment();
			loadFragment(LoadMode.ADD, CONTAINER_DETAILS_ID, mDetailsFragment, 2.0f);
		}
		
//-r		if (mSpotsFragment == null) {
//-r			mSpotsFragment = new SpotsFragment();
//-r		}
    	// Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
//-        View detailsFrame = findViewById(R.id.details);
//-        mDualPane = (detailsFrame != null) && (detailsFrame.getVisibility() == View.VISIBLE);
		mDualPane = true;

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurrSelectedId = savedInstanceState.getLong("curSelId", -1);
        }
        
        if (mDualPane)
        	showDetails(0);
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
    private void showDetails(long rowId) {
    	assert(mDualPane);
    	/*
        // Check what fragment is currently shown, replace if needed.
        DetailsFragment details = (DetailsFragment)
                getFragmentManager().findFragmentById(R.id.details);
		*/

    	/*
		if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.hide(mDetailsFragment);
			ft.commit();
			mContainer.setVisibility(View.GONE);
		}
		*/
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
			case R.id.menu_markit:
				// Toast.makeText(this, "Mark this spot!", Toast.LENGTH_SHORT).show();
				// Intent markIntent = new Intent(this, MarkActivity.class);
				// startActivityForResult(markIntent, ACTIVITY_MARK);
				if (mDetailsFragment != null)
					mDetailsFragment.setMode(DetailsFragment.Mode.Edit);
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
	public void onSaveEdit(String name, int type, String desc) {
		// TODO: Save the edit
		if (mDetailsFragment != null)
			mDetailsFragment.setMode(Mode.Display);
	}

	public void onCancelEdit() {
		if (mDetailsFragment != null)
			mDetailsFragment.setMode(Mode.Display);
	}

	public void onLatLngOverride(float lat, float lng) {
		// TODO: Recenter the map
	}

	public int getID() { return mID; }
	
	public void onSpotSelected(long id) {}
	public void onSpotCreate() {}
}
