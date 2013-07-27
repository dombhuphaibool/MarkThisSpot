package com.bandonleon.markthisspot;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/******************************************************************************
 * 
 * @author 		Dom Bhuphaibool
 * 				dombhuphaibool@yahoo.com
 * 
 * Created: 	19 July 2013
 * Modified:	27 July 2013
 *
 * Description:
 * This class extends the ContentProvider logic, encapsulating the data 
 * model underneath. The current implementation is to use an SQLite database
 * to store the data. Access to the data is then exposed via the 
 * ContentProvider apis. The client can use the ContentResolver apis to talk
 * with Android who will then connect it to our ContentProvider
 * (SpotsContentProvider).
 * 
 ******************************************************************************/
public class SpotsContentProvider extends ContentProvider {
	public static final String AUTHORITY = "com.bandonleon.markthisspot.provider";
	public static final String SPOTS_PATH = "spots";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SPOTS_PATH);
	
	private static final String MIME_TYPE_DIR = "vnd.android.cursor.dir/vnd.com.bandonleon.markthisspot.provider." + SPOTS_PATH;
	private static final String MIME_TYPE_ITEM = "vnd.android.cursor.item/vnd.com.bandonleon.markthisspot.provider." + SPOTS_PATH;

	// Helper constants for use with UriMatcher
	private static final UriMatcher URI_MATCHER;
	private static final int URI_SPOTS_DIR = 1;
	private static final int URI_SPOTS_ITEM = 2;
	
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, SPOTS_PATH, URI_SPOTS_DIR);
		URI_MATCHER.addURI(AUTHORITY, SPOTS_PATH + "/#", URI_SPOTS_ITEM);
	}
	
	// Database related stuff
    public static final String KEY_ROWID = BaseColumns._ID;
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DESC = "desc";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_COLOR = "color";
    public static final String KEY_SHOW = "show";
    
	public static final String[] PROJECTION_ALL = new String[] 
		{ KEY_ROWID, KEY_NAME, KEY_TYPE, KEY_DESC, KEY_LAT, KEY_LNG, KEY_COLOR, KEY_SHOW };
    
    private static final String TAG = "SpotsContentProvider";
    private DatabaseHelper mDbHelper = null;
    private SQLiteDatabase mDb = null;

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "spots";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE + 
        " (" + KEY_ROWID + " integer primary key autoincrement, " +
        	   KEY_NAME + " text not null, " +
        	   KEY_TYPE + " text not null, " +
        	   KEY_DESC + " text, " +
        	   KEY_LAT + " real not null, " + 
        	   KEY_LNG + " real not null, " +
        	   KEY_COLOR + " integer not null, " + 
        	   KEY_SHOW + " integer not null);";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS spots");
            onCreate(db);
        }
    }
    
	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate()");
		
		if (mDbHelper == null) {
			mDbHelper = new DatabaseHelper(getContext()); 	// (mCtx);
			if (mDbHelper != null)
				mDb = mDbHelper.getWritableDatabase();
		}
		
		return(mDb != null);
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		switch(URI_MATCHER.match(uri)) {
			case URI_SPOTS_DIR:
				Log.d(TAG, "query(URI_SPOTS_DIR)");
				return mDb.query(DATABASE_TABLE, PROJECTION_ALL, selection, selectionArgs, null, null, sortOrder);
				
			case URI_SPOTS_ITEM:
				String qualifier = KEY_ROWID + "=" + uri.getLastPathSegment();  // should this be =?
				// selectionArgs = new String[] { uri.getLastPathSegment() };
				selection = TextUtils.isEmpty(selection) ? qualifier : qualifier + " AND " + selection; 
				Log.d(TAG, "query(URI_SPOTS_ITEM) " + selection);

				Cursor c = mDb.query(true, DATABASE_TABLE, PROJECTION_ALL, 
									 selection, selectionArgs, null, null, null, null);
				if (c != null)
					c.moveToFirst();
				return c;
				
			default:
				Log.e(TAG, "Unsupported URI in query() : " + uri);				
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}		
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (URI_MATCHER.match(uri) != URI_SPOTS_DIR) {
			Log.e(TAG, "Unsupported URI in insert() : " + uri);							
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		long rowId = mDb.insert(DATABASE_TABLE, null, values);		// Returns -1 if error occurs
		if (rowId > -1) {
			Uri itemUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(itemUri, null);
			return itemUri;
		}
			
		Log.e(TAG, "Could not insert() with URI : " + uri);
		return null;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int updateCount = 0;
		
		switch(URI_MATCHER.match(uri)) {
			case URI_SPOTS_DIR:
				break;
				
			case URI_SPOTS_ITEM:
					String qualifier = KEY_ROWID + "=" + uri.getLastPathSegment();
					selection = TextUtils.isEmpty(selection) ? qualifier : qualifier + " AND " + selection; 
				break;
				
			default:
				Log.e(TAG, "Unsupported URI in update() : " + uri);				
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		updateCount = mDb.update(DATABASE_TABLE, values, selection, selectionArgs);
		if (updateCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return updateCount;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int deleteCount = 0;
		
		switch(URI_MATCHER.match(uri)) {
			case URI_SPOTS_DIR:
				break;
				
			case URI_SPOTS_ITEM:
					String qualifier = KEY_ROWID + "=" + uri.getLastPathSegment();
					selection = TextUtils.isEmpty(selection) ? qualifier : qualifier + " AND " + selection; 
				break;
				
			default:
				Log.e(TAG, "Unsupported URI in delete() : " + uri);				
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		deleteCount = mDb.delete(DATABASE_TABLE, selection, selectionArgs);
		if (deleteCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return deleteCount;		
	}
	
	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case URI_SPOTS_DIR:
				return MIME_TYPE_DIR;
			case URI_SPOTS_ITEM:
				return MIME_TYPE_ITEM;
			default:
				Log.e(TAG, "Unsupported URI getType() : " + uri);
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}
}
