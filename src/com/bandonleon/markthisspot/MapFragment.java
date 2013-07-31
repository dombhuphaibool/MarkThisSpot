package com.bandonleon.markthisspot;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/******************************************************************************
 * 
 * @author 		Dom Bhuphaibool
 * 				dombhuphaibool@yahoo.com
 * 
 * Created: 	19 July 2013
 * Modified:	30 July 2013
 *
 * Description: 
 * Map fragment is the fragment to view and control Google Map v2.0
 * This fragment encapsulates all Google Map's apis. User of MapFragment 
 * should implement MapFragment.OnMapListener interface to receive all 
 * callbacks.
 * 
 *****************************************************************************/
public class MapFragment extends Fragment 					   
						 implements LoaderCallbacks<Cursor>,
						 			GooglePlayServicesClient.ConnectionCallbacks,
						 			GooglePlayServicesClient.OnConnectionFailedListener,
						 			GoogleMap.OnCameraChangeListener,
						 			GoogleMap.OnMapLongClickListener,
						 			GoogleMap.OnMarkerClickListener,
						 			GoogleMap.OnInfoWindowClickListener {

	// The loader's unique id. Loader ids are specific to the Activity or
	// Fragment in which they reside.
	private static final int LOADER_ID = 2;
	
    // IDs for storing tmp data (for orientation change, etc)
	private static final String CURR_POS_LAT = "curr_lat";
	private static final String CURR_POS_LNG = "curr_lng";
	private static final String CURR_ZOOM = "curr_zoom";
		
	// Use GoogleMap.getMinZoomLevel() and GoogleMap.getMaxZoomLevel() to 
	// find zoom limits. The initial level is arbitrary as it seems that this
	// zoom level is pretty good for what we want to start with.
	//
	// TODO:
	// Not sure if we want to always zoom to the default zoom level when the 
	// user selects a location of keep the current zoom level.
	private static final float INITIAL_ZOOM = 8;
	private static final float DEFAULT_ZOOM = 14;

	// Keep track of the current position, zoom level, and map type.
	// This is so we can restore the value after an orientation change,
	// or a pause/resume event in Android.
	private LatLng mCurrPos = null;
	private float mCurrZoom = -1.0f;
	private int mMapType = 1;
	
	// *** Note: Do not hold on to any objects obtained from GoogleMap
	// as this is owned by the view SupportMapFragment and will cause
	// memory leaks, etc. (so don't hold onto markers, etc). ***
	private GoogleMap mMap;		

	// Really we should be keeping the marker's ID (eg, Marker.getId())
	// and then query the Marker from the GoogleMap object by the ID
	// string. But alas, Google Maps V2 does not provide a way for us
	// to retrieve the Marker object by its String ID, so we must keep
	// a reference to it. The mTmpMarker is created to represent the 
	// current new location being created. This will then be converted
	// to a normal Marker when the user decides to save the changes.
	// The hash maps also keep references to Markers for the above reason
	// instead of keeping the Marker's string ID. The hash maps are used
	// to find Markers by location ID (or ListView item ID) and vice versa.
	private Marker mTmpMarker = null;
	private HashMap<Long, Marker> mLocIdToMarker = new HashMap<Long, Marker>();
	private HashMap<String, Long> mMarkerIdToLocId = new HashMap<String, Long>();	
	
	// GeoLocation stuff. Stores the location client.
    private LocationClient mLocationClient = null;

    // UI member variables: 
    // Actual Google map fragment and MainActivity
	private SupportMapFragment mSMapFragment = null;
    private Activity mActivity = null;

    // Allow clients to be notified when events occur in the MapFragment
	public interface OnMapListener {
		public void onMapConnected();
		public void onMapDisconnected();
		public void onMapClick(double lat, double lng);
		public void onMapLongClick(double lat, double lng);
		public void onCameraChange(double lat, double lng);
		public void onMarkerClick(long id);
		public void onInfoWindowClick(long id);
	}
	private ArrayList<OnMapListener> mMapListeners = new ArrayList<OnMapListener>();
	public void addMapListener(OnMapListener l) {
		// Check to see if the listener is already registered
		for (OnMapListener ml : mMapListeners)
			if (l == ml)
				return;
		mMapListeners.add(l);
	}
	// *Note* Android reconstruct fragments (per orientation change)
	// via the default constructor. Do not override constructor or if so,
	// we must supply a default constructor.
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
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
		
        mLocationClient = new LocationClient(mActivity, this, this);
        
        if (savedInstanceState != null) {
        	mCurrZoom = (Float) savedInstanceState.getFloat(CURR_ZOOM);
        	mCurrPos = new LatLng(savedInstanceState.getDouble(CURR_POS_LAT),
					 			  savedInstanceState.getDouble(CURR_POS_LNG));
        } 
        
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }
    
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putFloat(CURR_ZOOM, mCurrZoom);
		if (mCurrPos != null) {
			outState.putDouble(CURR_POS_LAT, mCurrPos.latitude);
			outState.putDouble(CURR_POS_LNG, mCurrPos.longitude);
		}
	}
	
	@Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        if (mLocationClient != null)
        	mLocationClient.connect();
        
        assert(mSMapFragment != null);
        if (mSMapFragment == null)
        	return;
        
        mMap = mSMapFragment.getMap();
		if (mMap != null) {
			mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
				@Override
				public void onMapClick(LatLng loc) {
					for (OnMapListener ml : mMapListeners)
						ml.onMapClick(loc.latitude, loc.longitude); 
				}
			});
			mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {				
				@Override
				public void onMarkerDragStart(Marker marker) {}

				@Override
				public void onMarkerDragEnd(Marker marker) {
					LatLng pos = marker.getPosition();
					animateCamera(pos.latitude, pos.longitude);
				}
				
				@Override
				public void onMarkerDrag(Marker marker) {}
			});
			mMap.setOnMapLongClickListener(this);
			mMap.setOnCameraChangeListener(this);
			mMap.setOnMarkerClickListener(this);
			mMap.setOnInfoWindowClickListener(this);
			mMap.setMapType(mMapType);
			mMap.setMyLocationEnabled(true);
		}        
    }
	
	@Override
    public void onStop() {
		// Get rid of all GoogleMap's Marker references as 
		// GoogleMap handles its lifecycle and we do not want
		// to leak memory since we are disconnecting.
		clearAllMarkers(false);
		
        // Disconnecting the client invalidates it.
		if (mLocationClient != null)
			mLocationClient.disconnect();
        super.onStop();
    }	

	@Override
	public void onResume() {
		super.onResume();
		
		// At this point all of these member variables should be valid!
		assert(mLocationClient != null);
		assert(mSMapFragment != null);
		assert(mActivity != null);
		assert(mMap != null);
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
        
        if (resultCode == ConnectionResult.SUCCESS) {
            // Google Play services is available!!! :)
            // In debug mode, log the status
            Log.d(MainActivity.APPTAG, "Google Play services is available.");
            return true;
        } else {
            // Google Play services was not available for some reason
        	showErrorDialog(resultCode);
            return false;
        }
    }
	
    /*
     * Public method to animate the camera
     */
    public void animateCamera(double lat, double lng) {
    	setCamera(true, lat, lng, DEFAULT_ZOOM);
    }
    
    /*
     * Helper method to animate or move the camera
     */
    private void setCamera(boolean animate, double lat, double lng, float zoom) {
		if (isServicesConnected() && mLocationClient != null) {
			if (mLocationClient.isConnected() && mMap != null) {
				LatLng pos = new LatLng(lat, lng);
				mCurrPos = pos;
				mCurrZoom = zoom;
				CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(pos, zoom);
				if (animate)
					mMap.animateCamera(cam, 2500, null);
				else
					mMap.moveCamera(cam);
			}
		}    	
    }

    /*
     * Helper method for adding marker. By calling this method
     * directly, it is assumed that the caller has already checked
     * to see if isServicesConnected(), and that mLocationClient
     * is valid and mLocationClient.isConnected(), and that
     * the GoogleMap mMap object is valid
     */
    private void addMarkerNoCheck(long id, LocationInfo loc, boolean showInfo) {
    	String snippet = (id == 1) ? "" : loc.getType() + ":\n\r" + loc.getDesc();
		Marker marker = mMap.addMarker(new MarkerOptions()
							.position(new LatLng(loc.getLat(), loc.getLng()))
							.title(loc.getName())
							.snippet(snippet));
    	if (marker != null) {
    		mLocIdToMarker.put(Long.valueOf(id), marker);
    		mMarkerIdToLocId.put(marker.getId(), Long.valueOf(id));
    		if (showInfo)
    			marker.showInfoWindow();
    	}
    }
    
    public void addMarker(long id, LocationInfo loc, boolean showInfo) {
		if (isServicesConnected() && mLocationClient != null) {
			if (mLocationClient.isConnected() && mMap != null) {
				addMarkerNoCheck(id, loc, showInfo);
			}
		}
    }
    
    public void addTmpMarker() {
    	addTmpMarker(mCurrPos.latitude, mCurrPos.longitude);
    }
    
    public void addTmpMarker(double lat, double lng) {
		if (isServicesConnected() && mLocationClient != null) {
			if (mLocationClient.isConnected() && mMap != null) {
				removeTmpMarker();
				mTmpMarker = mMap.addMarker(new MarkerOptions()
							.position(new LatLng(lat, lng))
							.title("New Location")
							.draggable(true)
							.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
			}
		}    	
    }

    public void convertTmpMarker(long id, LocationInfo loc) {
    	if (mTmpMarker != null) {
    		/*
    		removeTmpMarker();
    		addMarker(id, loc, true);
    		*/
    		mTmpMarker.setDraggable(false);
    		mTmpMarker.setIcon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    		updateMarkerInfo(mTmpMarker, id, loc);
    		mLocIdToMarker.put(Long.valueOf(id), mTmpMarker);
    		mMarkerIdToLocId.put(mTmpMarker.getId(), Long.valueOf(id));
    		
    		// Don't hold on to the tmp marker refrence now that we
    		// have it in the hash map
    		mTmpMarker = null;    		
    	}    	
    }

    public void removeTmpMarker() {
    	if (mTmpMarker != null) {
    		mTmpMarker.remove();
    		mTmpMarker = null;
    	}
    }
    
    public void activateMarker(long id, boolean activate) {
    	Marker marker = mLocIdToMarker.get(Long.valueOf(id));
    	if (marker != null) {
    		if (activate)
    			marker.showInfoWindow();
    		else
    			marker.hideInfoWindow();
    	}
    }

    private void updateMarkerInfo(Marker marker, long id, LocationInfo loc) {
    	if (marker != null) {
    		marker.hideInfoWindow();
        	marker.setTitle(loc.getName());
        	String snippet = (id == 1) ? "" 
        			: loc.getType() + ":\n\r" + loc.getDesc();
        	marker.setSnippet(snippet);
        	marker.setPosition(new LatLng(loc.getLat(), loc.getLng()));
        	marker.showInfoWindow();
    	}
    }
    
    public void updateMarker(long id, LocationInfo loc) {
    	Marker marker = mLocIdToMarker.get(Long.valueOf(id));
    	updateMarkerInfo(marker, id, loc);
    }
    
    public void removeMarker(long id) {
    	Marker marker = mLocIdToMarker.remove(Long.valueOf(id));
    	if (marker != null) {
    		mMarkerIdToLocId.remove(marker.getId());
    		marker.remove();
    	}
    }

    public void clearAllMarkers(boolean remove) {
		// Get rid of all GoogleMap's Marker references as 
		// GoogleMap handles its lifecycle and we do not want
		// to leak memory since we are disconnecting.
    	if (remove) {
    		for (Marker marker : mLocIdToMarker.values()) {
    			if (marker != null)
    				marker.remove();
    		}    			
    	}
		mLocIdToMarker.clear();
		mMarkerIdToLocId.clear();
		mTmpMarker = null;
    }
    
    public LocationInfo getCurrMapLocation() {
    	LocationInfo loc = new LocationInfo();
    	loc.setLatLng(mCurrPos.latitude, mCurrPos.longitude);
    	return loc;
    }

    public LocationInfo getCurrLocation() {
    	LocationInfo loc = new LocationInfo();
		if (isServicesConnected() && mLocationClient != null) {
			if (mLocationClient.isConnected()) {
				Location currLocation = mLocationClient.getLastLocation();
				if (currLocation != null) {
					loc.setLatLng(currLocation.getLatitude(),
								  currLocation.getLongitude());
				}
			}
		}
    	return loc;
    }
    
	/*
	 * Button callback method - This method is no longer in use, we should
	 * delete this soon...
	 */
	public void onGetLocationClicked(View view) {
		if (isServicesConnected() && mLocationClient != null) {
			if (mLocationClient.isConnected()) {
				Location currLocation = mLocationClient.getLastLocation();
				if (currLocation != null) {
					// mLatLng.setText(LocationUtils.getLatLng(this, mCurrentLocation));
			        
					if (mMap != null) {
						LatLng pos = new LatLng(currLocation.getLatitude(), currLocation.getLongitude());
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
	 * GoogleMap.OnCameraChangeListener callback
	 */
	@Override
	public void onCameraChange(CameraPosition pos) {
		mCurrPos = pos.target;
		mCurrZoom = pos.zoom;
		
		for (OnMapListener ml : mMapListeners)
			ml.onCameraChange(pos.target.latitude, pos.target.longitude);
	}

	/*
	 * GoogleMap.OnMapLongClickListener callback
	 */
	@Override
	public void onMapLongClick(LatLng loc) {
		animateCamera(loc.latitude, loc.longitude);
		for (OnMapListener ml : mMapListeners)
			ml.onMapLongClick(loc.latitude, loc.longitude); 
	}

	/*
	 * GoogleMap.OnMarkerClickListener callback
	 */
	@Override
	public boolean onMarkerClick(Marker marker) {
    	// Toast.makeText(mActivity, "Marker Click: " + marker.getId(), Toast.LENGTH_SHORT).show();

		// Cannot compare with ==, must use marker's ID 
		// to match as it's the way GoogleMap works...
		if (mTmpMarker != null && mTmpMarker.getId().equals(marker.getId())) {
			LatLng pos = marker.getPosition();
			animateCamera(pos.latitude, pos.longitude);
		} else {
			removeTmpMarker();
			Long id = mMarkerIdToLocId.get(marker.getId());
			if (id != null) {
				for (OnMapListener ml : mMapListeners)
					ml.onMarkerClick(id.longValue());
			}
		}
		
		// return false stating that we are not consuming the event
		// so that the default behavior can execute.
		return false;
	}

	/*
	 * GoogleMap.OnMarkerDragListener callback
	 */
	/*
	public void onMarkerDragStart(Marker marker) {}
	public void onMarkerDrag(Marker marker) {}
	public void onMarkerDragEnd(Marker marker) {}
	*/
	
	/*
	 * GoogleMap.OnInfoWindowClickListener callback
	 */
	@Override
	public void onInfoWindowClick(Marker marker) {
    	// Toast.makeText(mActivity, "Info Window Click: " + marker.getId(), Toast.LENGTH_SHORT).show();
		
		if (mTmpMarker == null || !mTmpMarker.getId().equals(marker.getId())) {
			Long id = mMarkerIdToLocId.get(marker.getId());
			if (id != null) {
				for (OnMapListener ml : mMapListeners)
					ml.onInfoWindowClick(id.longValue());
			}
		}
	}
	
    /*
     * GooglePlayServicesClient.ConnectionCallbacks
     */
    @Override
    public void onConnected(Bundle bundle) {
    	// Set initial camera position and zoom factor to the the current
    	// location
    	if (mCurrZoom < 0.0f)
    		mCurrZoom = INITIAL_ZOOM;
    	if (mCurrPos == null) {
	    	LocationInfo loc = getCurrLocation();
	    	mCurrPos = new LatLng(loc.getLat(), loc.getLng());
    	}
    	setCamera(false, mCurrPos.latitude, mCurrPos.longitude, mCurrZoom);
    	
		// *** Add markers for all locations now that we are connected ***
        // Initialize the Loader with id '2' and callbacks to us.
        // If the loader doesn't already exist, one is created. Otherwise,
        // the already created Loader is reused. In either case, the
        // LoaderManager will manage the Loader across the Activity/Fragment
        // lifecycle, will receive any new loads once they have completed,
        // and will report this new data back to us.
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        
		for (OnMapListener ml : mMapListeners)
    		ml.onMapConnected();
    	Toast.makeText(mActivity, R.string.connected, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
		for (OnMapListener ml : mMapListeners)
    		ml.onMapDisconnected();
    	Toast.makeText(mActivity, R.string.disconnected, Toast.LENGTH_SHORT).show();
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
    
    /**************************************************************************
     * Loader callbacks
     * 
     * LoaderManager.initLoader() and LoaderManager.restartLoader() will 
     * eventually cause onCreateLoader() callback to be called to return 
     * a loader.
     *************************************************************************/
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	CursorLoader loader = new CursorLoader(getActivity(), 
    			SpotsContentProvider.CONTENT_URI, 
    			SpotsContentProvider.PROJECTION_ALL, null, null, null);
    	return loader;
    }
    
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {    	
    	// A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
          case LOADER_ID:
      		if (isServicesConnected() && mLocationClient != null &&
      			mLocationClient.isConnected() && mMap != null) {
      			
	            // The asynchronous load is complete and the data
	            // is now available for use.
	        	assert(cursor != null);
	        	if (cursor != null) {
	        		if (cursor.moveToFirst()) {
	        			// Remove all previous markers!
	        			clearAllMarkers(true);
	        			
	        			int idColIdx = cursor.getColumnIndexOrThrow(SpotsContentProvider.KEY_ROWID);
	        			int nameColIdx = cursor.getColumnIndexOrThrow(SpotsContentProvider.KEY_NAME);
	        			int descColIdx = cursor.getColumnIndexOrThrow(SpotsContentProvider.KEY_DESC);
	        			int typeColIdx = cursor.getColumnIndexOrThrow(SpotsContentProvider.KEY_TYPE);
	        			int latColIdx = cursor.getColumnIndexOrThrow(SpotsContentProvider.KEY_LAT);
	        			int lngColIdx = cursor.getColumnIndexOrThrow(SpotsContentProvider.KEY_LNG);
	        			LocationInfo loc = new LocationInfo();
	        			do {
	        				loc.setName(cursor.getString(nameColIdx));
	        				loc.setDesc(cursor.getString(descColIdx));
	        				loc.setType(cursor.getString(typeColIdx));
	        				loc.setLatLng(cursor.getDouble(latColIdx),
	        							  cursor.getDouble(lngColIdx));
	        				addMarkerNoCheck(cursor.getLong(idColIdx), loc, false);
	        			} while (cursor.moveToNext());        			
	        		}
	        	}
      		}
            break;
        }
    }
    
    public void onLoaderReset(Loader<Cursor> loader) {
    	// For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        
    	// mAdapter.swapCursor(null);
    }
}
