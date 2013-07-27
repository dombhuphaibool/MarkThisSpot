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
	private static final float	DEFAULT_LAT = 0.0f;
	private static final float	DEFAULT_LNG = 0.0f;
	private static final int	DEFAULT_COLOR = 1;
	private static final int	DEFAULT_SHOW = 1;
	private static String 		DEFAULT_TYPE = "";

	public static void setDefaultType(String type) { DEFAULT_TYPE = type; }
	public static final LocationInfo CURR_LOC = new LocationInfo();
	static {
		CURR_LOC.setName(CURR_LOC_NAME);
		CURR_LOC.setDesc(CURR_LOC_DESC);
	}
	
	private String mName;
	private String mDesc;
	private String mType;
	private float mLat;
	private float mLng;
	private int mColor;
	private int mShow;
	
	public LocationInfo() {
		mName = DEFAULT_NAME;
		mType = DEFAULT_TYPE;
		mLat = DEFAULT_LAT;
		mLng = DEFAULT_LNG;
		mColor = DEFAULT_COLOR;
		mShow = DEFAULT_SHOW;
	}

	public void setName(String n) { mName = n; }
	public void setDesc(String d) { mDesc = d; }
	public void setType(String t) { mType = t; }
	public void setLatLng(float lat, float lng) { mLat = lat; mLng = lng; }
	public void setColor(int c) { mColor = c; }
	public void setShow(int s) { mShow = s; }
	
	public String getName() { return mName; }
	public String getDesc() { return mDesc; }
	public String getType() { return mType; }
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
        values.put(SpotsContentProvider.KEY_TYPE, mType);
        values.put(SpotsContentProvider.KEY_DESC, mDesc);
        values.put(SpotsContentProvider.KEY_LAT, String.valueOf(mLat));
        values.put(SpotsContentProvider.KEY_LNG, String.valueOf(mLng));
        values.put(SpotsContentProvider.KEY_COLOR, String.valueOf(mColor));
        values.put(SpotsContentProvider.KEY_SHOW, String.valueOf(mShow));
        return values;
	}
}
