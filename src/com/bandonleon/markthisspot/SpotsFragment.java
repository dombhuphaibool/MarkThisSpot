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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

/******************************************************************************
 * 
 * @author Dom Bhuphaibool
 *
 * This is the List fragment. Data is retrieved from a SQLite database via
 * a Loader and LoadManager. A content provider encapsulates the database
 * logic (see SpotsContentProvider). We then use a ContentResolver to
 * access teh ContentProvider.
 * 
 ******************************************************************************/
public class SpotsFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private static final int DELETE_ID = Menu.FIRST;
	
	private static final String[] PROJECTION_SPOTS = new String[] { SpotsContentProvider.KEY_NAME, SpotsContentProvider.KEY_TYPE };
	
	// The loader's unique id. Loader ids are specific to the Activity or
	// Fragment in which they reside.
	private static final int LOADER_ID = 1;

	// The activity that contains our fragment
	private Activity mActivity;
	
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
	 
	// The adapter that binds our data to the ListView
	private SimpleCursorAdapter mAdapter;
	  
	// private static int mNoteNumber = 1;
	private ContentResolver mContentResolver;
    
	public interface OnSpotListener {
		public void onSpotSelected(long id);
		public void onSpotCreate();
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
        try {
        	mListener = (OnSpotListener) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement OnSpotListener");
        }
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//-        setHasOptionsMenu(true);
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

        // Configuration config = getResources().getConfiguration();
        // if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {}

        registerForContextMenu(getListView());		// For long pressed => delete notes ?    	
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    // ------------------------------------------------------------------------
    // Options menu callbacks 
    // - used for creating new notes
    // ------------------------------------------------------------------------    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//-        menu.add(0, CREATE_ID, 0, R.string.menu_create);
        // inflater.inflate(R.string.menu_insert, menu);	should we call this also?
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//-    	switch (item.getItemId()) {
//-    		case CREATE_ID:
    			// createNote();
//-    			mListener.onSpotCreate();
//-    			return true;
//-    	}
    	
        return super.onOptionsItemSelected(item);
    }
    
    // ------------------------------------------------------------------------
    // Context menu callbacks 
    // - used for deleting new notes
    // ------------------------------------------------------------------------    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.menu_delete);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case DELETE_ID:
    			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    			mContentResolver.delete(Uri.withAppendedPath(SpotsContentProvider.CONTENT_URI, String.valueOf(info.id)), "", null);
    			reloadListView();
    			return true;
    			
    		default:
    	}
    	
		return super.onContextItemSelected(item);
	}

    // ------------------------------------------------------------------------
    // ListActivity callback 
    // - when a user selects an item in the ListView
    // ------------------------------------------------------------------------    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Uri debugUri = Uri.withAppendedPath(SpotsContentProvider.CONTENT_URI, String.valueOf(id));
        // Log.d("Mark this spot", "onListItemClicked() uri is " + debugUri.toString());

    	mListener.onSpotSelected(id);
        
        /*
        Intent intent = new Intent(this, NoteEdit.class);
        intent.putExtra(NotesContentProvider.KEY_ROWID, id);
        startActivityForResult(intent, ACTIVITY_EDIT);
        */
        
        //--------------------------------------------------------------------
        // The NoteEdit class now retrieves the data directly...
        /*
        Cursor c = mContentResolver.query(Uri.withAppendedPath(NotesContentProvider.CONTENT_URI, String.valueOf(id)), 
        								  PROJECTION_ALL, "", null, null);
        if (c != null) {
            Intent intent = new Intent(this, NoteEdit.class);
            intent.putExtra(NotesContentProvider.KEY_ROWID, id);
            intent.putExtra(NotesContentProvider.KEY_TITLE, 
            				c.getString(c.getColumnIndexOrThrow(NotesContentProvider.KEY_TITLE)));
            intent.putExtra(NotesContentProvider.KEY_BODY,
    						c.getString(c.getColumnIndexOrThrow(NotesContentProvider.KEY_BODY)));
            startActivityForResult(intent, ACTIVITY_EDIT);
        }
     	*/
        
        // ---------------------------------------------------------------------
        // An alternative is to reuse a saved cursor...
        // If this works, it would be more efficient!
        /*
        if (mNotesCursor != null) {
        	Cursor c = mNotesCursor;
        	c.moveToPosition(position);
        	Intent i = new Intent(this, NoteEdit.class);
        	i.putExtra(NotesContentProvider.KEY_ROWID, id);
        	i.putExtra(NotesContentProvider.KEY_TITLE, c.getString(
        	        c.getColumnIndexOrThrow(NotesContentProvider.KEY_TITLE)));
        	i.putExtra(NotesContentProvider.KEY_BODY, c.getString(
        	        c.getColumnIndexOrThrow(NotesContentProvider.KEY_BODY)));
        	startActivityForResult(i, ACTIVITY_EDIT);
        }
        */
    }
    
    // ------------------------------------------------------------------------
    // Helper method to reload the ListView
    // ------------------------------------------------------------------------        
    public void reloadListView() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    // ------------------------------------------------------------------------
    // Loader callbacks
    // ------------------------------------------------------------------------
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	CursorLoader loader = new CursorLoader(mActivity, SpotsContentProvider.CONTENT_URI, PROJECTION_SPOTS, null, null, null);
    	// Log.d("Notepadv1", "Creating loader id: " + id + ", content_uri: " + SpotsContentProvider.CONTENT_URI);
    	return loader;
    }
    
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    	// Log.d("Notepadv1", "onLoadFinished(), loader id: " + loader.getId() + ", count: " + cursor.getCount());
    	
    	// A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
          case LOADER_ID:
            // The asynchronous load is complete and the data
            // is now available for use. Only now can we associate
            // the queried Cursor with the SimpleCursorAdapter.
            mAdapter.swapCursor(cursor);
            // mNotesCursor = cursor;
            break;
        }
        // The listview now displays the queried data.
    }
    
    public void onLoaderReset(Loader<Cursor> loader) {
    	// For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        mAdapter.swapCursor(null);
        // mNotesCursor = null;
    }
}
