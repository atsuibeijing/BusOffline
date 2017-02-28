package com.example.busoffline;

import java.util.ArrayList;



import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ResultListActivity extends Activity{
	private BusDbAdapter dbHelper;
	private Cursor busCursor;
	private double latitude;
	private double longitude;
	private String busType;
	private String busNo;
	private String busStopName;
	private long busID;
	private int count;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.result_layout);
		
		Bundle v0 = this.getIntent().getExtras();
		busID=v0.getLong("ID");
		
		busType=v0.getString("TYPE");
		busNo=v0.getString("NO");
		TextView busTitle=(TextView) findViewById(R.id.titleLabel);
		busTitle.setText(busType+" --> "+busNo);

		dbHelper = new BusDbAdapter(this);
		dbHelper.open();		
		busCursor=dbHelper.fetchAllBusStopsByNo(String.valueOf(busID));
		String[] from = new String[]{"stop_seq","stop_name"};
		int[] to = new int[]{R.id.stop_seq, R.id.stop_name};
		SimpleCursorAdapter busAdapter = new SimpleCursorAdapter(this,R.layout.result_list_item2,busCursor,from,to,0);
		ListView busList = (ListView) findViewById(R.id.searchNumberListView);		
		busList.setAdapter(busAdapter);	
		count = busList.getCount();

		OnItemClickListener listener1 = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {				
				Intent localIntent = new Intent("com.robert.maps.action.SHOW_POINTS");
				Bundle localBundle = new Bundle();	
				ArrayList<String> locations = new ArrayList<String>();
				for(int i=0;i<count;i++){					
					busCursor = (Cursor) parent.getItemAtPosition(i);
					latitude = busCursor.getDouble(busCursor.getColumnIndexOrThrow("lat"));
					longitude = busCursor.getDouble(busCursor.getColumnIndexOrThrow("lng"));
					busStopName=busCursor.getString(busCursor.getColumnIndexOrThrow("stop_name"));
					locations.add(Double.toString(latitude) + "," + Double.toString(longitude) + ";" +"第 "+Integer.toString(i+1)+" 站;"+busStopName);
				}
				Collections.swap(locations, position, locations.size() - 1);
				
				localBundle.putStringArrayList("locations", locations);
				localIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				localIntent.putExtras(localBundle);
				startActivity(localIntent);
			}
		};
		busList.setOnItemClickListener(listener1);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(dbHelper != null)
		{
			dbHelper.close();
		}
	}	
}
