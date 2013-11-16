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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
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
	
	private final static int SUN = 0;
	private final static int MON = 1;
	private final static int TUE = 2;
	private final static int WED = 3;
	private final static int THU = 4;
	private final static int FRI = 5;
	private final static int SAT = 6;
	
	private final int MIN_TIME_DIGITS = 3;	
	private final static int MIN_DIGITS = 2;
	private final static int TEN_MINUTES = 10;
	private final static int ZERO = 0;
	private final static int SECOND = 1;
	private final static int THIRD = 2;
	private final static String NO_OVERLAP = "N";
	private final static long NONE_L = -1;

	private final static String CHECKED = "Y";
	
	private SQL_DataSource datasource;
	
	private Cal_Date sel_CD;
	private long b_id;
	
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
	
	/* Repetition Toggle Buttons */
	protected ToggleButton rp_sun;
	protected ToggleButton rp_mon;
	protected ToggleButton rp_tue;
	protected ToggleButton rp_wed;
	protected ToggleButton rp_thu;
	protected ToggleButton rp_fri;
	protected ToggleButton rp_sat;
	
	/* Repetition Module */
	private Repetition_Module Rep_Mod;
	
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
		/* SQL Configuration */
		datasource = new SQL_DataSource(this);
		datasource.open();
		
		Bundle Schedule_Date = getIntent().getExtras();
		b_id = Schedule_Date.getLong(Schedule.SELECT_ID_KEY);
		
		if(b_id == NONE_L){
			sel_CD = Schedule_Date.getParcelable(Schedule.SELECT_KEY);
		}
		
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
		
		rp_sun = (ToggleButton) findViewById(R.id.rp_sun);
		rp_mon = (ToggleButton) findViewById(R.id.rp_mon);
		rp_tue = (ToggleButton) findViewById(R.id.rp_tue);
		rp_wed = (ToggleButton) findViewById(R.id.rp_wed);
		rp_thu = (ToggleButton) findViewById(R.id.rp_thu);
		rp_fri = (ToggleButton) findViewById(R.id.rp_fri);
		rp_sat = (ToggleButton) findViewById(R.id.rp_sat);
		
		
		/* Set Default End Time to 1 hr ahead of current Time if it doesn't Pass into Next Day */
		final Calendar c = Calendar.getInstance();
		if(c.get(Calendar.HOUR_OF_DAY) < 23){
			end_tp.setCurrentHour(c.get(Calendar.HOUR_OF_DAY) + 1);
		}
		
		if(b_id == NONE_L){
			r_dp.updateDate(sel_CD.get_year(), sel_CD.get_month(), sel_CD.get_day());
		}
		check_EDIT_MODE();
	}
	
	private void check_EDIT_MODE()
	{
		if(b_id != NONE_L){
			creation_b.setText(getResources().getString(R.string.edt_ev));
			Event temp = datasource.getEvent(b_id);
			name_et.setText(temp.getName());
			desc_et.setText(temp.getDescription());
			start_tp.setCurrentHour(extract_HOUR(temp.GetStart()));
			start_tp.setCurrentMinute(extract_MINUTES(temp.GetStart()));
			end_tp.setCurrentHour(extract_HOUR(temp.GetEnd()));
			end_tp.setCurrentMinute(extract_MINUTES(temp.GetEnd()));
			r_dp.updateDate(temp.GetYear(), temp.GetMonth(), temp.GetDay());
			
			/* Repetition Module */
			Rep_Mod = new Repetition_Module();
			Rep_Mod.set_RepString(temp.getRep());
			
			rp_sun.setChecked(Rep_Mod.toggle_Check(SUN));
			rp_mon.setChecked(Rep_Mod.toggle_Check(MON));
			rp_tue.setChecked(Rep_Mod.toggle_Check(TUE));
			rp_wed.setChecked(Rep_Mod.toggle_Check(WED));
			rp_thu.setChecked(Rep_Mod.toggle_Check(THU));
			rp_fri.setChecked(Rep_Mod.toggle_Check(FRI));
			rp_sat.setChecked(Rep_Mod.toggle_Check(SAT));

			Rep_Mod = null; //Deallocate Module
			/* Rep Module*/
			
			if(temp.getAlarm().equals(CHECKED)) {
				alarm_tb.setChecked(true);
			} else {
				alarm_tb.setChecked(false);
			}
			c_Picker.setColor(temp.getColor());
		}
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
			if(name_et.getText().toString().trim().length() > 0){
				add_event();
			} else {
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
		
		/* Rep Mod Code */
		Rep_Mod = new Repetition_Module();
		
		Rep_Mod.add_ToggleButton(rp_sun);
		Rep_Mod.add_ToggleButton(rp_mon);
		Rep_Mod.add_ToggleButton(rp_tue);
		Rep_Mod.add_ToggleButton(rp_wed);
		Rep_Mod.add_ToggleButton(rp_thu);
		Rep_Mod.add_ToggleButton(rp_fri);
		Rep_Mod.add_ToggleButton(rp_sat);
		
		temp.set_Rep(Rep_Mod.get_RepString());
		
		Rep_Mod = null; // Deallocate
		
		/* Rep Mod Code*/
		
		if(!timeIssues(temp, b_id)){
			if(b_id != NONE_L){
				datasource.deleteEvent(b_id);
			}
			/* SQL_Database Code */
			datasource.createEvent(temp);
			/* Return to Primary Activity*/
			finish();
		}
	}
	
	protected int extract_MINUTES(int time)
	{
		String my_time = "" + time;
		String nu_time = my_time.length() > MIN_DIGITS ? 
				my_time.substring(my_time.length() - MIN_DIGITS) : my_time;
		return Integer.parseInt(nu_time);
	}
	
	protected int extract_HOUR(int time)
	{		
		String my_time = "" + time;
		if(my_time.length() <= MIN_DIGITS){
			return ZERO;
		} else if(my_time.length() == MIN_TIME_DIGITS){
			String nu_time = my_time.substring(ZERO, SECOND);
			return Integer.parseInt(nu_time);
		} else {
			String nu_time = my_time.substring(ZERO, THIRD);
			return Integer.parseInt(nu_time);
		}	
	}
	
	protected String minutes(int min)
	{
		String time;
		if(min < TEN_MINUTES){
			time = "" + ZERO + min;
			return time; 
		} else {
			time = "" + min;
			return time;
		}
	}
	
	protected String check_toggle()
	{
		if(alarm_tb.isChecked()){
			return "Y";
		} else {
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
	
	protected boolean timeIssues(Event d, long id)
	{
		if(d.GetDate().getStartTime() == d.GetDate().getEndTime()){
			focus_Time();
			Toast.makeText(Add_Activity.this,"Starting time cannot equal end time!",
                    Toast.LENGTH_SHORT).show();
			return true;
		} else if(d.GetDate().getStartTime() > d.GetDate().getEndTime()){
			focus_Time();
			Toast.makeText(Add_Activity.this,"Event cannot start after it ends!",
                    Toast.LENGTH_SHORT).show();
			return true;
		} else if(datasource.overlapExists(d, id) != NO_OVERLAP){
			focus_Time();
			Toast.makeText(Add_Activity.this,"Event time conflicts with an Event: " + datasource.overlapExists(d, id),
                    Toast.LENGTH_LONG).show();
			return true;
		}
		return false;
	}
	
	protected void focus_Time()
	{
		start_tp.requestFocus();
	}
	
	public void create_Alarm(int time, int id)
	{
//		/* Instantiate a Calendar */ 
//	    Calendar calendar = Calendar.getInstance();
//	    calendar.add(Calendar.SECOND, 5);
		
	    Intent AlarmIntent = new Intent(this, Receiver_Module.class);
	    PendingIntent DispIntent = PendingIntent.getBroadcast(this, id, AlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

	    /* Scheduling the Alarm to be triggered*/
	    AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
	    alarmManager.set(AlarmManager.RTC, time, DispIntent);
	}
	
	public void cancel_Alarm(int id)
	{
		/* Recreate the alarm creation data */
		Intent AlarmIntent = new Intent(this, Receiver_Module.class);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		PendingIntent DispIntent = PendingIntent.getBroadcast(this, id, AlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		/* Instead of setting an alarm, use cancel on the pending Intent*/
		alarmManager.cancel(DispIntent);
	}
	
}
