package com.example.busoffline;



import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NumberSearchActivity extends Activity {
	private BusDbAdapter dbHelper;
	private Cursor busCursor;
	private SimpleCursorAdapter busAdapter;
	private String busType;
	private String busNo;
	private long busID;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		setContentView(R.layout.number_search_layout);
		dbHelper = new BusDbAdapter(this);
		try {
			dbHelper.open();
		}catch (Exception e){
			System.exit(10);
		}
		
		busCursor=dbHelper.fetchAllBuses();
		String[] from = new String[]{"route_no","type","description"};
		int[] to = new int[]{R.id.route_no,R.id.type,R.id.description};
		busAdapter = new SimpleCursorAdapter(this,R.layout.list_item,busCursor,from,to,0);
		
		//
		ListView busList = (ListView) findViewById(R.id.searchNumberListView);
		busList.setAdapter(busAdapter);
		OnItemClickListener listener1 = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				busCursor = (Cursor) parent.getItemAtPosition(position);
				busType = busCursor.getString(busCursor.getColumnIndexOrThrow("type"));
				busNo = busCursor.getString(busCursor.getColumnIndexOrThrow("route_no"));
				busID=id;
				showResult();
			}
		};
		busList.setOnItemClickListener(listener1);
		//
		final EditText busEdit= (EditText) findViewById(R.id.search_NumberText);
		OnFocusChangeListener listener2=new OnFocusChangeListener(){
			 public final void onFocusChange(View arg4, boolean arg5){
			        if(arg5) {            
			            ((InputMethodManager) getSystemService("input_method")).showSoftInput(busEdit, 1);
			        }   

			 };
		};
		busEdit.setOnFocusChangeListener(listener2);
		
		//
		busEdit.addTextChangedListener(new TextWatcher() {           
            @Override  
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	busAdapter.getFilter().filter(s.toString());
            }  
              
            @Override  
            public void beforeTextChanged(CharSequence s, int start, int count,  
                    int after) {                  
            }  
              
            @Override  
            public void afterTextChanged(Editable s) {                                
            }  
        }); 
		busAdapter.setFilterQueryProvider(new FilterQueryProvider() {
		         public Cursor runQuery(CharSequence constraint) {
		        	 return dbHelper.fetchBusesByQuery(constraint.toString());
		         }
		     });
		
		//
		OnEditorActionListener listener3=new OnEditorActionListener(){
		    public final boolean onEditorAction(TextView arg4, int arg5, KeyEvent arg6) {
		        if(arg5 == 3) {
		        	((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(busEdit.getWindowToken(), 0);
		        }
		        return true;
		    }			
		};
		busEdit.setOnEditorActionListener(listener3);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		onPause();
		System.exit(10);
		return true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_option_menu, menu);
		return true;
	}
	private void showResult(){
	      Intent localIntent = new Intent();
	      localIntent.setClass(this, P2PResultActivityGroup.class);
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
