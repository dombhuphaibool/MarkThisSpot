package com.bandonleon.markthisspot;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

/******************************************************************************
 * Activity that controls setting the application's settings (preferences)
 * 
 *****************************************************************************/
public class SettingsActivity extends Activity {

	public static final String KEY_PREF_MAPTYPE = "pref_maptype";

	/**************************************************************************
	 * Settings fragment
	 * 
	 *************************************************************************/
	public static class SettingsFragment extends PreferenceFragment 
										 implements OnSharedPreferenceChangeListener {
		// Store map type strings
		private String mMapTypeArray[];

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			// Load the preferences from the XML resource
			addPreferencesFromResource(R.xml.preferences);
			mMapTypeArray = getResources().getStringArray(R.array.pref_maptype_entries);
			setMapTypeSummary();
		}

		@Override
		public void onResume() {
		    super.onResume();
		    getPreferenceScreen().getSharedPreferences()
		    		.registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
		    super.onPause();
		    getPreferenceScreen().getSharedPreferences()
		    		.unregisterOnSharedPreferenceChangeListener(this);
		}	

		/*
		 * Helper method to update the map type string
		 */
		private void setMapTypeSummary() {
			Preference mapTypePref = findPreference(KEY_PREF_MAPTYPE);
			int mapTypeIdx = Integer.valueOf(getPreferenceScreen()
					.getSharedPreferences().getString(KEY_PREF_MAPTYPE, "1")) - 1;
			mapTypePref.setSummary(mMapTypeArray[mapTypeIdx]);			
		}
		
		/*
		 * OnSharedPreferenceChangeListener callback
		 */
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(KEY_PREF_MAPTYPE)) {
				setMapTypeSummary();
			}
		}
	}
	
	/**************************************************************************
	 * SettingsActivity methods
	 * 
	 *************************************************************************/
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Display fragment as the main content
		getFragmentManager().beginTransaction()
							.replace(android.R.id.content, new SettingsFragment())
							.commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case android.R.id.home:
	    		// TODO: Don't forget to call setResult() if we started this 
	    		// activity via startActivityForResults()
	    		finish();
	    		return true;
	    	
	    	default: 
	    		return super.onOptionsItemSelected(item);  
	    }
	}
}
