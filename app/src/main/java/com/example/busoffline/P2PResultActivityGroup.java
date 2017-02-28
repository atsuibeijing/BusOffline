package com.example.busoffline;



import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class P2PResultActivityGroup extends Activity{
	private BusDbAdapter dbHelper;
	private Cursor busCursor;
	private String busType;
	private String busNo;
	private long busID;
	
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
		busCursor=dbHelper.fetchAllRoutesByNo(String.valueOf(busID));
		String[] from = new String[]{"start","end"};
		int[] to = new int[]{R.id.start, R.id.end};
		SimpleCursorAdapter busAdapter = new SimpleCursorAdapter(this,R.layout.result_list_item,busCursor,from,to,0);
		ListView busList = (ListView) findViewById(R.id.searchNumberListView);
		busList.setAdapter(busAdapter);		

		OnItemClickListener listener1 = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				busID=id;
				showResult();
			}
		};
		busList.setOnItemClickListener(listener1);
	}

	private void showResult(){
	      Intent localIntent = new Intent();
	      localIntent.setClass(this, ResultListActivity.class);
	      Bundle localBundle = new Bundle();
	      localBundle.putLong("ID", busID);
	      localBundle.putString("TYPE", busType);
	      localBundle.putString("NO", busNo);
	      localIntent.putExtras(localBundle);
	      startActivity(localIntent);		
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
