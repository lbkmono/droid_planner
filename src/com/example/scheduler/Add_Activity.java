package com.example.scheduler;


/* Color Selector Imports */
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

/* Cloud Based Imports */

import com.parse.Parse;
import com.parse.ParseAnalytics;

/* Java Base Imports */
import java.util.Calendar;

/* ABS Based Imports */
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;

/* Android based Imports */
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Add_Activity  extends SherlockFragmentActivity {
	
	final static int TEN_MINUTES = 10;
	final static int ZERO = 0;
	private final static String NO_OVERLAP = "N";
	
	private SQL_DataSource datasource;
	
	private Cal_Date sel_CD;
	
	/* Color Selector Resources */
	protected SVBar svBar;
	protected OpacityBar opBar;
	protected ColorPicker c_Picker;
	
	/* Necessary Data for Resources */
	protected EditText name_et;
	protected EditText desc_et;
	
	/* Time Based Data Resources*/
	protected TimePicker start_tp;
	protected TimePicker end_tp;
	protected DatePicker r_dp;
	
	/* Button Based Resources*/
	protected ToggleButton alarm_tb;
	protected Button creation_b;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initalizeLayout();
		add_Event_Listener();
	}
	
	/* Configures everything Visual*/
	protected void initalizeLayout()
	{
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.creation_activity);
		config_resources();
		//config_actionbar();
	}
	
	protected void config_resources()
	{
		Bundle Schedule_Date = getIntent().getExtras();
		sel_CD = Schedule_Date.getParcelable(Schedule.SELECT_KEY);
		
		/* Layout Configuration */
		
		name_et = (EditText) findViewById(R.id.et_name);
		desc_et = (EditText) findViewById(R.id.et_desc);
		
		start_tp = (TimePicker) findViewById(R.id.time_start);
		start_tp.setIs24HourView(false);
		end_tp = (TimePicker) findViewById(R.id.time_end);
		end_tp.setIs24HourView(false);
		
		r_dp = (DatePicker) findViewById(R.id.date_sel);
		alarm_tb = (ToggleButton) findViewById(R.id.ce_alarm);
		creation_b = (Button) findViewById(R.id.ce_create); 
		
		c_Picker = (ColorPicker) findViewById(R.id.picker);
		svBar = (SVBar) findViewById(R.id.svbar);
		opBar = (OpacityBar) findViewById(R.id.opacitybar);
		
		c_Picker.setOldCenterColor(getResources().getColor(R.color.White));
		c_Picker.addOpacityBar(opBar);
		c_Picker.addSVBar(svBar);
		
		r_dp.updateDate(sel_CD.get_year(), sel_CD.get_month(), sel_CD.get_day());
		
		/* Set Default End Time to 1 hr ahead of current Time if it doesn't Pass into Next Day */
		final Calendar c = Calendar.getInstance();
		if(c.get(Calendar.HOUR_OF_DAY) < 23)
		{
			end_tp.setCurrentHour(c.get(Calendar.HOUR_OF_DAY) + 1);
		}
		
		/* SQL Configuration */
		datasource = new SQL_DataSource(this);
		datasource.open();
		
	}
	
	/* ActionBar Configuration */
	protected void config_actionbar()
	{
		/*
		 *  Functionality is not necessary but may make app look nicer later on
		 * 
		 * ActionBar ab = getSupportActionBar();
		 * ab.setDisplayShowTitleEnabled(false); 
		 * ab.setDisplayShowHomeEnabled(false);
		 * LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	     * View v =inflater.inflate(R.layout.top_bar_view, null, true);
	     * ab.setCustomView(v);
		 * ab.setDisplayShowCustomEnabled(true);
		*/
	}
	
	protected void add_Event_Listener() {
		creation_b.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View view) {
				/* Checks for Empty name as it is a Requirement */
			if(name_et.getText().toString().trim().length() > 0)
			{
				add_event();
			}
			else
			{
				name_et.requestFocus();
				Toast.makeText(Add_Activity.this,"Event cannot have an Empty Name!",
                        Toast.LENGTH_SHORT).show();
			}
		  } 
		}
	); 
	}
	

	protected void add_event()
	{
		Event temp = new Event();
		Date time = new Date();
		temp.setName(name_et.getText().toString());
		temp.setDescription(desc_et.getText().toString());
		
		int s_hour = start_tp.getCurrentHour();
		int s_min = start_tp.getCurrentMinute();
		String start = "" + s_hour + minutes(s_min);
		time.setStartTime(Integer.parseInt(start));
		
		int e_hour = end_tp.getCurrentHour();
		int e_min = end_tp.getCurrentMinute();
		String end = "" + e_hour + minutes(e_min);
		time.setEndTime(Integer.parseInt(end));
		
		time.setDay(r_dp.getDayOfMonth());
		time.setMth(r_dp.getMonth());
		time.setYr(r_dp.getYear());
		
		temp.setAlarm(check_toggle());
		temp.setDate(time);
		
		temp.setColor(c_Picker.getColor());
		
		if(!timeIssues(time))
		{
			/* SQL_Database Code */
			datasource.createEvent(temp);
			/* Return to Primary Activity*/
			finish();
		}
	}
	
	protected String minutes(int min)
	{
		String time;
		if(min < TEN_MINUTES)
		{
			time = "" + ZERO + min;
			return time; 
		}
		else
		{
			time = "" + min;
			return time;
		}
	}
	
	protected String check_toggle()
	{
		if(alarm_tb.isChecked())
		{
			return "Y";
		}
		else
		{
			return "N";
		}
	}
	
	@Override
	protected void onResume()
	{
		datasource.open();
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		datasource.close();
		super.onPause();
	}
	
	protected boolean timeIssues(Date d)
	{
		if(d.getStartTime() == d.getEndTime())
		{
			focus_Time();
			Toast.makeText(Add_Activity.this,"Starting time cannot equal end time!",
                    Toast.LENGTH_SHORT).show();
			return true;
		}
		else if(d.getStartTime() > d.getEndTime())
		{
			focus_Time();
			Toast.makeText(Add_Activity.this,"Event cannot start after it ends!",
                    Toast.LENGTH_SHORT).show();
			return true;
		}
		else if(datasource.overlapExists(d) != NO_OVERLAP)
		{
			focus_Time();
			Toast.makeText(Add_Activity.this,"Event time conflicts with an Event: " + datasource.overlapExists(d),
                    Toast.LENGTH_LONG).show();
			return true;
		}
		return false;
	}
	
	protected void focus_Time()
	{
		start_tp.requestFocus();
	}
	
}
