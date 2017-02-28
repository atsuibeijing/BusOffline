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
import android.widget.Toast;

public class MapResultList extends Activity{
	private BusDbAdapter dbHelper;
	private Cursor busCursor;
	private double latitude;
	private double longitude;
	private String busNo,busStopName,end,end1;
	private int count;
	private static final String ACTION_SHOW_POINTS = "com.example.busoffline.SHOW_POINTS";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.result_layout);
		TextView busTitle=(TextView) findViewById(R.id.titleLabel);
		
		//android.os.Debug.waitForDebugger();
		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		ListView busList = (ListView) findViewById(R.id.searchNumberListView);
		if (ACTION_SHOW_POINTS.equalsIgnoreCase(queryAction)) {	
			dbHelper = new BusDbAdapter(this);
			dbHelper.open();			
			int[] coordArr = queryIntent.getIntArrayExtra("coordinate");
			//int[] coordArr = {22453624,114166920,22451858,114169896};
			busCursor=dbHelper.fetchAllBusStopsByCoordinate(coordArr);
			count = busCursor.getCount();
			if(count>200){
				Toast.makeText(this, "Too many records", Toast.LENGTH_LONG).show();
				busCursor = null;
			}
			if(count==0) Toast.makeText(this, "Not found", Toast.LENGTH_LONG).show();			
			busTitle.setText("Search Result --> "+Integer.toString(count));
			
			String[] from = new String[]{"end","route_no","stop_name"};
			int[] to = new int[]{R.id.end,R.id.route_no,R.id.stop_name};
			SimpleCursorAdapter busAdapter = new SimpleCursorAdapter(this,R.layout.result_list_item3,busCursor,from,to,0);
			busList.setAdapter(busAdapter);	
		}
		
		OnItemClickListener listener1 = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				boolean flag=false;
				int start=0;
				Intent localIntent = new Intent("com.robert.maps.action.SHOW_POINTS");
				Bundle localBundle = new Bundle();	
				ArrayList<String> locations = new ArrayList<String>();	
				busCursor = (Cursor) parent.getItemAtPosition(position);
				end1 = busCursor.getString(busCursor.getColumnIndexOrThrow("end"));
				for(int i=0;i<count;i++){					
					busCursor = (Cursor) parent.getItemAtPosition(i);
					end = busCursor.getString(busCursor.getColumnIndexOrThrow("end"));					
					if (end.equals(end1)){
						if(!flag) start=i;
						latitude = busCursor.getDouble(busCursor.getColumnIndexOrThrow("lat"));
						longitude = busCursor.getDouble(busCursor.getColumnIndexOrThrow("lng"));
						busStopName=(busCursor.getString(busCursor.getColumnIndexOrThrow("stop_name"))).replace("車站: ", "");
						busNo=busCursor.getString(busCursor.getColumnIndexOrThrow("route_no")).replace("車號: ", "");
						
//						RMAP
//						final String [] fields = it.next().split(";");
//						String locns = "", title = "", descr = "";
//						if(fields.length>0) locns = fields[0];
//						if(fields.length>1) title = fields[1];
//						if(fields.length>2) descr = fields[2];		
						
						locations.add(Double.toString(latitude) + "," + Double.toString(longitude) + ";" +busNo+";"+busStopName);
						flag=true;
					}else{						
						if(flag){
							Collections.swap(locations, position-start, locations.size() - 1);
							break;
						}
					}
				}
				
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
