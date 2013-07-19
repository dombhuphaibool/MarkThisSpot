package com.bandonleon.markthisspot;

import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
// import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity 
					   implements GooglePlayServicesClient.ConnectionCallbacks,
						  	 	  GooglePlayServicesClient.OnConnectionFailedListener {

	private static final String CURR_POS_LAT = "curr_lat";
	private static final String CURR_POS_LNG = "curr_lng";
	private static final LatLng US_CENTER = new LatLng(38.5, -99.6);
	private LatLng mCurrPos = null;
	
	private TextView mLatLng;
	private GoogleMap mMap;		// *** Note: Do not hold on to any objects obtained from GoogleMap
								// as this is owned by the view SupportMapFragment and will cause
								// memory leaks, etc. (so don't hold onto markers, etc).
	
	
	// Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
        mLatLng = (TextView) findViewById(R.id.latlng);
        mLocationClient = new LocationClient(this, this, this);
        
        mCurrPos = (savedInstanceState != null) ? new LatLng(savedInstanceState.getDouble(CURR_POS_LAT),
        													 savedInstanceState.getDouble(CURR_POS_LNG)) : US_CENTER;
        
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		if (mMap != null) {
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			mMap.setMyLocationEnabled(true);
			mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrPos));
			
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mCurrPos != null) {
			outState.putDouble(CURR_POS_LAT, mCurrPos.latitude);
			outState.putDouble(CURR_POS_LNG, mCurrPos.longitude);
		}
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }
	
	@Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * Helper method to determine if the device is geo-location enabled
	 */
    private boolean isServicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        // If Google Play services is available
        if (resultCode == ConnectionResult.SUCCESS) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, "Google Play services is available.");
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
					mLatLng.setText(LocationUtils.getLatLng(this, mCurrentLocation));
			        
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
					Toast.makeText(this, "Please enable Wi-Fi & mobile network location", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
    /*
     * GooglePlayServicesClient.ConnectionCallbacks
     */
    @Override
    public void onConnected(Bundle bundle) {
    	Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
    	Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
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
            this,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
        }
    }    
}
