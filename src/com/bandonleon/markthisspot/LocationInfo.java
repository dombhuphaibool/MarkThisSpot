package com.bandonleon.markthisspot;

import android.content.ContentValues;

/******************************************************************************
 * 
 * @author Dom Bhuphaibool
 *
 * Model representation of the location information
 * 
 *****************************************************************************/
public class LocationInfo {
	private static final String DEFAULT_NAME = "New Location";

	private static final String CURR_LOC_NAME = "Current location"; 
	private static final String CURR_LOC_DESC = "Current location"; 
	private static final int	CURR_LOC_TYPE = 1;
	private static final float	CURR_LOC_LAT = 0.0f;
	private static final float	CURR_LOC_LNG = 0.0f;
	private static final int	CURR_LOC_COLOR = 1;
	private static final int	CURR_LOC_SHOW = 1;

	public static final LocationInfo CURR_LOC = new LocationInfo();
	static {
		CURR_LOC.setName(CURR_LOC_NAME);
		CURR_LOC.setDesc(CURR_LOC_DESC);
		CURR_LOC.setType(CURR_LOC_TYPE);
		CURR_LOC.setLatLng(CURR_LOC_LAT, CURR_LOC_LNG);
		CURR_LOC.setColor(CURR_LOC_COLOR);
		CURR_LOC.setShow(CURR_LOC_SHOW);
	}
	
	private String mName;
	private String mDesc;
	private int mType;
	private float mLat;
	private float mLng;
	private int mColor;
	private int mShow;
	
	public LocationInfo() {
		mName = DEFAULT_NAME;
		mType = 0;
		mLat = 0.0f;
		mLng = 0.0f;
		mColor = 0;
		mShow = 0;
	}

	public void setName(String n) { mName = n; }
	public void setDesc(String d) { mDesc = d; }
	public void setType(int t) { mType = t; }
	public void setLatLng(float lat, float lng) { mLat = lat; mLng = lng; }
	public void setColor(int c) { mColor = c; }
	public void setShow(int s) { mShow = s; }
	
	public String getName() { return mName; }
	public String getDesc() { return mDesc; }
	public int getType() { return mType; }
	public float getLat() { return mLat; }
	public float getLng() { return mLng; }
	public int getColor() { return mColor; }
	public int getShow() { return mShow; }	
	
	/*
	 * Utility method for return ContentValues so that
	 * the client can easily pass it to insert or update 
	 * the database.
	 */
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
        values.put(SpotsContentProvider.KEY_NAME, mName);
        values.put(SpotsContentProvider.KEY_TYPE, String.valueOf(mType));
        values.put(SpotsContentProvider.KEY_DESC, mDesc);
        values.put(SpotsContentProvider.KEY_LAT, String.valueOf(mLat));
        values.put(SpotsContentProvider.KEY_LNG, String.valueOf(mLng));
        values.put(SpotsContentProvider.KEY_COLOR, String.valueOf(mColor));
        values.put(SpotsContentProvider.KEY_SHOW, String.valueOf(mShow));
        return values;
	}
}
