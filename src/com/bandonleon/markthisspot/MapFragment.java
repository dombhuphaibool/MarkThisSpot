package com.bandonleon.markthisspot;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment 					   
						 implements GooglePlayServicesClient.ConnectionCallbacks,
						 			GooglePlayServicesClient.OnConnectionFailedListener {

	private static final String CURR_POS_LAT = "curr_lat";
	private static final String CURR_POS_LNG = "curr_lng";
	private static final LatLng US_CENTER = new LatLng(38.5, -99.6);
	private LatLng mCurrPos = null;
	
	//-	private TextView mLatLng;
	private SupportMapFragment mSMapFragment = null;
	private GoogleMap mMap;		// *** Note: Do not hold on to any objects obtained from GoogleMap
								// as this is owned by the view SupportMapFragment and will cause
								// memory leaks, etc. (so don't hold onto markers, etc).
	
	private boolean mAllowOverrideLatLng = false;
	private int mMapType = 1;
	
	// Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;
    private Location mCurrentLocation;

    private Activity mActivity;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	// Don't think that there's a need to call super...
    	// super.onCreateView(inflater, container, savedInstanceState);
    	
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
        
		return inflater.inflate(R.layout.map, container, false);
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

        mActivity = getActivity();
        assert(mActivity != null);

        /*
        mSMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.add(R.id.map, mSMapFragment);
        ft.commit();
        */
        FragmentManager fm = getChildFragmentManager();
        mSMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (mSMapFragment == null) {
            mSMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, mSMapFragment).commit();
        }
        assert(mSMapFragment != null);
        
		// Load the default preference values if necessary
		PreferenceManager.setDefaultValues(mActivity, R.xml.preferences, false);
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
		String mapType = sharedPref.getString(SettingsActivity.KEY_PREF_MAPTYPE, "1");
		mMapType = Integer.valueOf(mapType);
		
//-        mLatLng = (TextView) findViewById(R.id.latlng);
		
        mLocationClient = new LocationClient(mActivity, this, this);
        
        mCurrPos = (savedInstanceState != null) ? new LatLng(savedInstanceState.getDouble(CURR_POS_LAT),
        													 savedInstanceState.getDouble(CURR_POS_LNG)) : US_CENTER;
        
    }
    
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mCurrPos != null) {
			outState.putDouble(CURR_POS_LAT, mCurrPos.latitude);
			outState.putDouble(CURR_POS_LNG, mCurrPos.longitude);
		}
	}
	
	@Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
        
		// mMap = ((SupportMapFragment) ((FragmentActivity) mActivity).getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap = mSMapFragment.getMap();
		if (mMap != null) {
			mMap.setMapType(mMapType);
			mMap.setMyLocationEnabled(true);
			mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrPos));
			
		}        
    }
	
	@Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }	
    
	public void saveSettings(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String mapType = sharedPref.getString(SettingsActivity.KEY_PREF_MAPTYPE, "1");
		int newMapType = Integer.valueOf(mapType);
		if (mMap != null && mMapType != newMapType) {
			mMapType = newMapType;
			mMap.setMapType(mMapType);
		}
	}
    
	/*
	 * Helper method to determine if the device is geo-location enabled
	 */
    private boolean isServicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        
        // If Google Play services is available
        if (resultCode == ConnectionResult.SUCCESS) {
            // In debug mode, log the status
            Log.d(MainActivity.APPTAG, "Google Play services is available.");
            return true;
        // Google Play services was not available for some reason
        } else {
        	showErrorDialog(resultCode);
            return false;
        }
    }
	
	/*
	 * Button callback method
	 */
	public void onGetLocationClicked(View view) {
		if (isServicesConnected() && mLocationClient != null) {
			if (mLocationClient.isConnected()) {
				mCurrentLocation = mLocationClient.getLastLocation();
				if (mCurrentLocation != null) {
//-					mLatLng.setText(LocationUtils.getLatLng(this, mCurrentLocation));
			        
					if (mMap != null) {
						LatLng pos = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
						mCurrPos = pos;
						mMap.addMarker(new MarkerOptions().position(pos).title("Marker"));
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos,14), 2500, null);
						
						/*
						CameraPosition cameraPosition = new CameraPosition.Builder()
					    .target(pos) 			    // Sets the center of the map to Mountain View
					    .zoom(17)                   // Sets the zoom
					    .bearing(90)                // Sets the orientation of the camera to east
					    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
					    .build();                   // Creates a CameraPosition from the builder
						mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
						*/
					}
				} else {
					Toast.makeText(mActivity, "Please enable Wi-Fi & mobile network location", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
    /*
     * GooglePlayServicesClient.ConnectionCallbacks
     */
    @Override
    public void onConnected(Bundle bundle) {
    	Toast.makeText(mActivity, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
    	Toast.makeText(mActivity, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }
	
    /*
     * GooglePlayServicesClient.OnConnectionFailedListener
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	showErrorDialog(connectionResult.getErrorCode());
    }
    
    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    
    /*
     * Helper method to show error dialog
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            mActivity,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getFragmentManager(), MainActivity.APPTAG);
        }
    }    
}
