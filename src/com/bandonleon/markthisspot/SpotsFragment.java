package com.bandonleon.markthisspot;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

/******************************************************************************
 * 
 * @author 		Dom Bhuphaibool
 * 				dombhuphaibool@yahoo.com
 * 
 * Created: 	19 July 2013
 * Modified:	30 July 2013
 * 
 * Description:
 * This is the List fragment. Data is retrieved from a SQLite database via
 * a Loader and LoadManager. A content provider encapsulates the database
 * logic (see SpotsContentProvider). We then use a ContentResolver to
 * access teh ContentProvider.
 * 
 ******************************************************************************/
public class SpotsFragment extends ListFragment 
						   implements LoaderCallbacks<Cursor> {
	// IDs for context menu, invoked when the user performs a long press
	// on any ListView items.
    private static final int DELETE_ID = Menu.FIRST;
    private static final int CANCEL_ID = Menu.FIRST + 1;
    
    // The columns we are interested in to fill in our ListView
	private static final String[] PROJECTION_SPOTS = new String[] 
			{ SpotsContentProvider.KEY_NAME, SpotsContentProvider.KEY_TYPE };

	// IDs for storing tmp data (for orientation change, etc)
	private static final String CURR_LIST_POS = "currListPos";
	
	// The loader's unique ID. Loader IDs are specific to the Activity or
	// Fragment in which they reside.
	private static final int LOADER_ID = 1;

	// The activity that contains our fragment
	private Activity mActivity;
	private ContentResolver mContentResolver;
	
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
	 
	// The adapter that binds our data to the ListView
	private SimpleCursorAdapter mAdapter;
	  
	// Listener to notify when the user selects or delete an item from 
	// the ListView.
	public interface OnSpotListener {
		public void onSpotSelected(long id);
		public void onSpotDeleted(long id);
	}
	OnSpotListener mListener;
	
	/*
	 * Fragment Life cylce is as follows:
	 * 
	 * Fragment is added:
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
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
        mActivity = getActivity();
        if (mActivity != null) {
	        try {
	        	mListener = (OnSpotListener) activity;
	        } catch (ClassCastException e) {
	        	throw new ClassCastException(activity.toString() + 
	        						" must implement OnSpotListener");
	        }
        }
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] dataColumns = PROJECTION_SPOTS;
        int[] viewIDs = { R.id.row_item1, R.id.row_item2 };
     
        // Initialize the adapter. Note that we pass a 'null' Cursor as the
        // third argument. We will pass the adapter a Cursor only when the
        // data has finished loading for the first time (i.e. when the
        // LoaderManager delivers the data to onLoadFinished). Also note
        // that we have passed the '0' flag as the last argument. This
        // prevents the adapter from registering a ContentObserver for the
        // Cursor (the CursorLoader will do this for us!).
        mAdapter = new SimpleCursorAdapter(mActivity, R.layout.spot_row, null, dataColumns, viewIDs, 0);
     
        // Associate the (now empty) adapter with the ListView.
        setListAdapter(mAdapter);
     
        // The Activity (which implements the LoaderCallbacks<Cursor>
        // interface) is the callbacks object through which we will interact
        // with the LoaderManager. The LoaderManager uses this object to
        // instantiate the Loader and to notify the client when data is made
        // available/unavailable.
        mCallbacks = this;
     
        // Initialize the Loader with id '1' and callbacks 'mCallbacks'.
        // If the loader doesn't already exist, one is created. Otherwise,
        // the already created Loader is reused. In either case, the
        // LoaderManager will manage the Loader across the Activity/Fragment
        // lifecycle, will receive any new loads once they have completed,
        // and will report this new data back to the 'mCallbacks' object.
        getLoaderManager().initLoader(LOADER_ID, null, mCallbacks);
      
        mContentResolver = mActivity.getContentResolver();

        if (savedInstanceState != null)
        	mCurrListPos = savedInstanceState.getInt(CURR_LIST_POS, -1);
        
        ListView listView = getListView();
        if (listView != null) {
	        // For long pressed => delete item
	        registerForContextMenu(listView);
	    	// TODO: This doesn't seem to do anything. Remove??? 
	    	listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    	// listView.setSelector(android.R.color.darker_gray);
        }        
    }

    @Override
    public void onResume() {
    	super.onResume();
    	
    	// TODO: Find a way to highlight the selection after an orientaiton change.
    	/*
    	ListView listView = getListView();
    	if (listView != null && mCurrListPos > -1) {
    		View v = listView.getChildAt(mCurrListPos);
    		if (v != null) {
	    		v.setBackgroundColor(getResources().getColor(HIGHLIGHT_COLOR));
	        	mCurrListSelection = v;
    		}
    	}
    	*/
    	
    	// At this point, these member variables should be valid!
    	assert(mActivity != null);
    	assert(mContentResolver != null);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURR_LIST_POS, mCurrListPos);
    }
    
    // ------------------------------------------------------------------------
    // Options menu callbacks 
    // TODO: Delete... We no longer add items to the options menu from here...
    // ------------------------------------------------------------------------    
    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {    	
        return super.onOptionsItemSelected(item);
    }
    */
    
    // ------------------------------------------------------------------------
    // Context menu callbacks 
    // 
    // Implementation of long click to delete an item in the ListView. 
    // Long clicks invokes the context menu to pop up, which contains 
    // 2 items: Delete and Cancel.
    // ------------------------------------------------------------------------    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.menu_delete);
		menu.add(Menu.NONE, CANCEL_ID, Menu.NONE, R.string.menu_cancel);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case DELETE_ID:
    			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    			if (info.id > 1) {
    				deleteItem(info.id);
    			} else {
    				// TODO: Find a way to disable long click to delete on 
    				// 'Current Location' as it would be a much cleaner approach.
    		    	Toast.makeText(getActivity(), R.string.cannot_del_curr_loc, Toast.LENGTH_SHORT).show();
    			}
    			return true;
    			
    		default:
    	}
    	
		return super.onContextItemSelected(item);
	}

    /*
     * ListView highlighting logic. There are still some bugs...
     * 
     * TODO: Test this out thoroughly! Currently an item's view
     * seems to be reused among other items that are not currently 
     * visible. For example, if you have a long list, an item that 
     * is currently in view can be highlighted. Setting the background
     * on this view will also affect another item's view's background
     * that is not currently visible. If you scroll down, then you see
     * it's highlighted. I think Android does this for optimization
     * reason as the two views will never be visible at the same time.
     * 
     * TODO: We also need to find a way to highlight a ListView item
     * from just having the item's ID and not the actual view itself.
     * Is there a way to find the item's view by its ID? If not, we
     * need to find another way to highlight the ListView's item.
     */
    private int mCurrListPos = -1;
    private View mCurrListSelection = null;
    private static final int HIGHLIGHT_COLOR = R.color.listview_highlight;
    public void clearCurrListSelection() {
    	if (mCurrListSelection != null) {
    		mCurrListSelection.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    		mCurrListSelection = null;
    	}
    }

    /*
     * If the user deletes an item from outside of the ListFragment, we
     * need to be notified to keep the bookkeeping correct. Expose this
     * API so that the MainActivity can perform the bookkeeping and clean-up.
     * Current implementation allows the user to delete the item from
     * MarkFragment.
     */
    public void deleteItem(long id) {
    	assert(mContentResolver != null);
    	if (mContentResolver != null) {
			mContentResolver.delete(Uri.withAppendedPath(
								SpotsContentProvider.CONTENT_URI, 
								String.valueOf(id)), "", null);
			reloadListView();
	        if (mListener != null)
	        	mListener.onSpotDeleted(id);
    	}
    }
    
    // ------------------------------------------------------------------------
    // ListActivity callback 
    // - when a user selects an item in the ListView
    // ------------------------------------------------------------------------    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        // TODO: This doesn't really do anything because we are in
        // 'touch-mode'... We probably should delete the commented out code.
        // getListView().setItemChecked(position, true);
        // getListView().setSelection(position);
        if (v != mCurrListSelection) {
        	clearCurrListSelection();
            v.setBackgroundColor(getResources().getColor(HIGHLIGHT_COLOR));
        	mCurrListSelection = v;
        }
        mCurrListPos = position;

        if (mListener != null)
        	mListener.onSpotSelected(id);
    }
    
    // ------------------------------------------------------------------------
    // Helper method to reload the ListView
    // ------------------------------------------------------------------------        
    public void reloadListView() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    
    /**************************************************************************
     * Loader callbacks
     * 
     * LoaderManager.initLoader() and LoaderManager.restartLoader() will 
     * eventually cause onCreateLoader() callback to be called to return 
     * a loader.
     *************************************************************************/
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	CursorLoader loader = new CursorLoader(mActivity, SpotsContentProvider.CONTENT_URI, PROJECTION_SPOTS, null, null, null);
    	return loader;
    }
    
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    	// A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
          case LOADER_ID:
            // The asynchronous load is complete and the data
            // is now available for use. Only now can we associate
            // the queried Cursor with the SimpleCursorAdapter.
            mAdapter.swapCursor(cursor);
            break;
        }
        // The listview now displays the queried data.
    }
    
    public void onLoaderReset(Loader<Cursor> loader) {
    	// For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        mAdapter.swapCursor(null);
    }
}
