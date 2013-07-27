package com.bandonleon.markthisspot;

import android.content.ContentValues;
import android.os.Bundle;

/******************************************************************************
 * 
 * @author 		Dom Bhuphaibool
 * 				dombhuphaibool@yahoo.com
 * 
 * Created: 	19 July 2013
 * Modified:	27 July 2013
 *
 * Description: 
 * Model representation of the location information
 * 
 *****************************************************************************/
public class LocationInfo {
	private static long mLocId = 0;
	private static final String DEFAULT_NAME = "New Location ";

	public static final String CURR_LOC_NAME = "Current location"; 
	public static final String CURR_LOC_DESC = "Current location"; 
	private static final double	DEFAULT_LAT = 1.0;
	private static final double	DEFAULT_LNG = 1.0;
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
	private double mLat;
	private double mLng;
	private int mColor;
	private int mShow;
	
	public LocationInfo() {
		mName = "";
		mDesc = "";
		mType = DEFAULT_TYPE;
		mLat = DEFAULT_LAT;
		mLng = DEFAULT_LNG;
		mColor = DEFAULT_COLOR;
		mShow = DEFAULT_SHOW;
	}

	public void setName(String n) { mName = n; }
	public void setDesc(String d) { mDesc = d; }
	public void setType(String t) { mType = t; }
	public void setLatLng(double lat, double lng) { mLat = lat; mLng = lng; }
	public void setColor(int c) { mColor = c; }
	public void setShow(int s) { mShow = s; }
	
	public String getName() { return mName; }
	public String getDesc() { return mDesc; }
	public String getType() { return mType; }
	public double getLat() { return mLat; }
	public double getLng() { return mLng; }
	public int getColor() { return mColor; }
	public int getShow() { return mShow; }	
	
	/*
	 * Utility method for return ContentValues so that
	 * the client can easily pass it to insert or update 
	 * the database.
	 */
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
        values.put(SpotsContentProvider.KEY_NAME, 
        	mName.isEmpty() ? DEFAULT_NAME + String.valueOf(++mLocId) : mName);
        // If it's for 'Current location', do not include a location type
        values.put(SpotsContentProvider.KEY_TYPE, mName.equals(CURR_LOC_NAME) ? "" : mType);
        values.put(SpotsContentProvider.KEY_DESC, mDesc);
        values.put(SpotsContentProvider.KEY_LAT, String.valueOf(mLat));
        values.put(SpotsContentProvider.KEY_LNG, String.valueOf(mLng));
        values.put(SpotsContentProvider.KEY_COLOR, String.valueOf(mColor));
        values.put(SpotsContentProvider.KEY_SHOW, String.valueOf(mShow));
        return values;
	}
	
	public void saveState(Bundle stateInstance) {
		stateInstance.putSerializable(SpotsContentProvider.KEY_NAME, mName);
		stateInstance.putSerializable(SpotsContentProvider.KEY_DESC, mDesc);
		stateInstance.putSerializable(SpotsContentProvider.KEY_TYPE, mType);
		stateInstance.putSerializable(SpotsContentProvider.KEY_LAT, mLat);
		stateInstance.putSerializable(SpotsContentProvider.KEY_LNG, mLng);
		stateInstance.putSerializable(SpotsContentProvider.KEY_COLOR, mColor);
		stateInstance.putSerializable(SpotsContentProvider.KEY_SHOW, mShow);
	}
	
	public void loadState(Bundle stateInstance) {
		mName = (String) stateInstance.getSerializable(SpotsContentProvider.KEY_NAME);
		mDesc = (String) stateInstance.getSerializable(SpotsContentProvider.KEY_DESC);
		mType = (String) stateInstance.getSerializable(SpotsContentProvider.KEY_TYPE);
		Double d1 = (Double) stateInstance.getSerializable(SpotsContentProvider.KEY_LAT);
		Double d2 = (Double) stateInstance.getSerializable(SpotsContentProvider.KEY_LNG);
		mLat = d1;
		mLng = d2;
		mColor = (Integer) stateInstance.getSerializable(SpotsContentProvider.KEY_COLOR);
		mShow = (Integer) stateInstance.getSerializable(SpotsContentProvider.KEY_SHOW);		
	}
}
