Planner Plus
----------
Planner Plus is a simple android planner application that allows you create "Events" for a certain times and dates.
Current features include:
    
  - Creating an Event (with time/event Conflict checking)
  - Creating a Todo
  - Editing and removing created Events
  - Easy Clearing all of Events
  - SQLite Database Storage
  - Future: Cloud Storage (Parse)
  - Future: Class Planner

Dependencies
----------
Projects for these libraries need to be imported and set as library for main project:

	-	ActionBarSherlock
	-	HoloColorPicker
	
Problems/Issues
----------
	Application Crashes on Start!
	-	First try: aSQLiteManager by Andsen (Google Playstore). Using the app, navigate to "Events.db" which should be in the SDCard. Open table "usrevents". Clear the table. Try to start Planner.
	-	Still crashing: FileExpert by GeekSoft (Google Playstore). Using the app, navigate to SDCArd. Find Events.db and EventsJournal.db(if exists). Delete both database files. Try to start Planner.
	-	Last resort: Uninstall Planner Plus. Make sure to hit "remove all data" before uninstalling.