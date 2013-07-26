package com.bandonleon.markthisspot;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
    
	public void setMapFragment(MapFragment mapFragment) { mMapFragment = mapFragment; }
    public MapFragment getMapFragment() { return mMapFragment; }
	public void setMarkFragment(MarkFragment markFragment) { mMarkFragment = markFragment; }
    
    public void displaySpot(long id) {
    	Activity activity = getActivity();
    	if (activity != null) {
	        Cursor c = activity.getContentResolver().query(Uri.withAppendedPath(SpotsContentProvider.CONTENT_URI, String.valueOf(id)), 
					  							  		   SpotsContentProvider.PROJECTION_ALL, "", null, null);
	        if (c != null) {
	        	String name = c.getString(c.getColumnIndexOrThrow(SpotsContentProvider.KEY_NAME));
	        	if (mSpotName != null)
	        		mSpotName.setText(name);
	        }
    	}
    }
    
    public void setMode(Mode mode) {
		Log.d("Details Fragment", "setMode(" + mode + "), current mode is " + mMode);
//-    	if (mode != mMode) {
        	int vis = (mode == Mode.Display) ? View.VISIBLE : View.GONE;
        	mSpotName.setVisibility(vis);
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
		mMarkFragmentContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }    
}
