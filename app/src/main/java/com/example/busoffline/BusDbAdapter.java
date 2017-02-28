package com.example.busoffline;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.io.File;
import java.util.List;

public class BusDbAdapter {
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
//	private static final String DATABASE_NAME = "/mnt/sdcard/rmaps/data/bus.db";
	private static String DATABASE_NAME = "";
	private static int DATABASE_VERSION = 1;
	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}
	}

	public BusDbAdapter(Context Ctx) {
		super();
		this.mCtx = Ctx;
		String aFileName="/MyMaps/bus.sqlitedb";
		String SQLFileName="";
		if(Build.VERSION.SDK_INT >= 19 && Build.SERIAL != "unknown") {
			File[] v5 = Ctx.getExternalFilesDirs(null);
			if(v5.length > 0) {
				for(int v0 = v5.length-1; v0 >= 0; --v0) {
					File v1 = v5[v0];
					if(v1 != null) {
						String v1_1 = v1.getAbsolutePath();  // "/storage/14DC-DF82/Android/data/com.speedsoftware.explorer/files"
						int v2 = v1_1.indexOf("/Android/");
						if(v2 >= 0) {
							v1_1 = v1_1.substring(0, v2);  // "/storage/14DC-DF82"
						}
						if(new File(v1_1+aFileName).exists()){
							SQLFileName=v1_1+aFileName;
							DATABASE_NAME = SQLFileName;
							break;
						}
					}
				}
				if(SQLFileName==""){
					StorageUtils.makeTextAndShow(Ctx, aFileName + " is not found!");
				}
			}
		}else {
			String internalSDCard="";
			List<StorageUtils.StorageInfo> aList=StorageUtils.getStorageList();
			for(StorageUtils.StorageInfo SQLPath:aList){
				if(!SQLPath.getDisplayName().contains("Internal")){
					if(new File(SQLPath.path+aFileName).exists()){
						SQLFileName=SQLPath.path+aFileName;
						DATABASE_NAME = SQLFileName;
						break;
					}
				}else{
					internalSDCard=SQLPath.path;
				}
			}
			if(SQLFileName==""){
				SQLFileName=internalSDCard+aFileName;
				if(!new File(SQLFileName).exists()){
					StorageUtils.makeTextAndShow(Ctx, aFileName + " is not found!");
				}else{
					DATABASE_NAME = SQLFileName;
				}
			}
		}

/*		String internalSDCard="";
		List<StorageUtils.StorageInfo> aList=StorageUtils.getStorageList();
		for(StorageUtils.StorageInfo SQLPath:aList){
			if(!SQLPath.getDisplayName().contains("Internal")){
				if(new File(SQLPath.path+aFileName).exists()){
					SQLFileName=SQLPath.path+aFileName;
					DATABASE_NAME = SQLFileName;
					break;
				}
			}else{
				internalSDCard=SQLPath.path;
			}
		}
		if(SQLFileName==""){
			SQLFileName=internalSDCard+aFileName;
			if(!new File(SQLFileName).exists()){
				StorageUtils.makeTextAndShow(Ctx, aFileName + " is not found!");
			}else{
				DATABASE_NAME = SQLFileName;
			}
		}*/
	}

	public BusDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
//		mDb = mDbHelper.getWritableDatabase();
		mDb = mDbHelper.getReadableDatabase();
		return this;
	}

	public void close() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}

	public Cursor fetchAllBuses() {
		Cursor mCursor = mDb
				.rawQuery(
						"select id as _id,route_no,type,description from BUS_LIST order by route_no",
						null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchAllRoutesByNo(String busID) throws SQLException {
		Cursor mCursor = null;
		String astrArgs[] = { busID };
		mCursor = mDb.rawQuery(
				"select id as _id,start,end from BUS_ROUTE where PATH=?",
				astrArgs);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchAllBusStopsByNo(String busID) throws SQLException {
		Cursor mCursor = null;
		String astrArgs[] = { busID };
		mCursor = mDb.rawQuery("select PATH as _id,stop_seq,stop_name,lat,lng from BUS_STOP where PATH=?",
				astrArgs);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchAllBusStopsByCoordinate(int[] coordArr) throws SQLException {
		Cursor mCursor = null;
		String astrArgs[] = { ""+coordArr[0]/1E6,""+coordArr[1]/1E6,""+coordArr[2]/1E6,""+coordArr[3]/1E6 };
//		mCursor = mDb.rawQuery("select A.PATH as _id,A.[stop_name]||\"(\"||A.[stop_seq]||\") \" as stop_name,A.[lat],A.[lng],C.[type]||\" : \"||B.[start] ||\" - \"||B.[end] as description,C.[route_no] from BUS_STOP A inner join [BUS_ROUTE] B on B.[ID]=A.[PATH] inner join [BUS_LIST] C on C.[ID] = B.[PATH] where lat<? and lng>? and lat>? and lng<? order by C.[route_no],[description]",
//				astrArgs);		
//		mCursor = mDb.rawQuery("select A.PATH as _id,A.[stop_name]||\"(\"||A.[stop_seq]||\") \" as stop_name,A.[lat],A.[lng],C.[type],B.[end],C.[route_no] from BUS_STOP A inner join [BUS_ROUTE] B on B.[ID]=A.[PATH] inner join [BUS_LIST] C on C.[ID] = B.[PATH] where lat<? and lng>? and lat>? and lng<? order by [end],[lat],[type],[route_no]",
//				astrArgs);		
		mCursor = mDb.rawQuery("select A.PATH as _id,A.[lat],A.[lng],\"車站: \"||A.[stop_name] as [stop_name],\"前往: \"||B.[end] as [end],\"車號: \"||group_concat(C.[route_no]) as [route_no] from BUS_STOP A inner join [BUS_ROUTE] B on B.[ID]=A.[PATH] inner join [BUS_LIST] C on C.[ID] = B.[PATH] where lat<? and lng>? and lat>? and lng<? group by [end],[lat] order by [end],[lat]",
				astrArgs);
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor fetchBusesByQuery(String inputText) throws SQLException {
		Cursor mCursor = null;
		if (inputText == null || inputText.length() == 0) {
			mCursor = mDb
					.rawQuery(
							"select id as _id,route_no,type,description from BUS_LIST order by route_no",
							null);
		} else {			
			//String astrArgs[] = { "%" + inputText + "%" };
			String astrArgs[] = { inputText + "%" };
			int ascii=inputText.charAt(0);
			if (ascii<256){
				mCursor = mDb.rawQuery("select id as _id,route_no,type,description from BUS_LIST where route_no like ? order by route_no",
						astrArgs);				
			}
			else
			{
				mCursor = mDb.rawQuery(
						"SELECT [BUS_LIST].id as _id,[BUS_LIST].route_no,[BUS_LIST].type,[BUS_LIST].description FROM [BUS_LIST]  INNER JOIN [BUS_ROUTE] ON [BUS_LIST].[ID] = [BUS_ROUTE].[PATH] INNER JOIN [BUS_STOP] ON [BUS_ROUTE].[ID] = [BUS_STOP].[PATH] WHERE [BUS_STOP].[stop_name] like ?  GROUP BY [BUS_LIST].[type],[BUS_LIST].[route_no]",	
						astrArgs);
			}
		}
		
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
}
