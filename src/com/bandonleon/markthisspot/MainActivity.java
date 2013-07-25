package com.bandonleon.markthisspot;

import com.bandonleon.markthisspot.DetailsFragment.Mode;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MainActivity extends FragmentActivity implements SpotsFragment.OnSpotListener,
															  MarkFragment.OnSpotEditListener {

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
    MapFragment mMapFragment = null;
    MarkFragment mMarkFragment = null;
    DetailsFragment mDetailsFragment = null;
    
	public MainActivity() {
		mID++;
		Log.w("MainActivity", "Constructor " + mID);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// setContentView(R.layout.test);
		setContentView(R.layout.spots_list);
		mContainer = (LinearLayout) findViewById(R.id.fragment_container);
				
		if (mSpotsFragment == null) {
			mSpotsFragment = new SpotsFragment();
		/*
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.fragment_container, sf);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		*/
		}
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

    private void showDetails(long rowId) {
    	assert(mDualPane);
    	/*
        // Check what fragment is currently shown, replace if needed.
        DetailsFragment details = (DetailsFragment)
                getFragmentManager().findFragmentById(R.id.details);
		*/
		if (mMapFragment == null) {
			mMapFragment = new MapFragment();
		}
		assert(mMapFragment != null);
		
		if (mMarkFragment == null) {
			Log.w("MainActivity", "Constructing MarkFragment");
			mMarkFragment = new MarkFragment();
	    	mMarkFragment.setOnSpotEditListener(this);
		}
		assert(mMarkFragment != null);
		
		if (mDetailsFragment == null) {
			Log.w("MainActivity", "Constructing DetailsFragment");
			mDetailsFragment = new DetailsFragment();
			mDetailsFragment.setMapFragment(mMapFragment);
			mDetailsFragment.setMarkFragment(mMarkFragment);
			
			LinearLayout listContainer = new LinearLayout(this);
			// listContainer.setWeightSum(1.0f);
			listContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
			listContainer.setId(CONTAINER_LIST_ID);
			
			{
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				// ft.add(R.id.fragment_container, mSpotsFragment);
				ft.add(CONTAINER_LIST_ID, mSpotsFragment);
				// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
			
			mContainer.addView(listContainer);

//-			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			{
				LinearLayout detailsContainer = new LinearLayout(this);
				// detailsContainer.setWeightSum(2.0f);
				detailsContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2.0f));
				detailsContainer.setId(CONTAINER_DETAILS_ID);
				
				{ 
					// Execute a transaction, replacing any existing fragment
					// with this one inside the frame.
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					// ft.replace(R.id.details, mDetailsFragment);
					// ft.add(R.id.fragment_container, mDetailsFragment);
					ft.add(CONTAINER_DETAILS_ID, mDetailsFragment);
					// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
				}
				
				mContainer.addView(detailsContainer);
				if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.hide(mDetailsFragment);
					ft.commit();
					mContainer.setVisibility(View.GONE);
				}
			}
		}
		
		Log.w("MainActivity", "showDetails() mDetailsFragment is " + (mDetailsFragment != null ? "not null, " : "null, ") + "and mMarkFragment is " + (mMarkFragment != null ? "not null." : "null."));
		assert(mMarkFragment != null && mDetailsFragment != null);
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
