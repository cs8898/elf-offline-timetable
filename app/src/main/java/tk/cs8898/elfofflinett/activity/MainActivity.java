package tk.cs8898.elfofflinett.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.SeekBar;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.model.bus.BusProvider;
import tk.cs8898.elfofflinett.model.bus.messages.MessageDatasetChanged;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.model.entity.StageEntity;
import tk.cs8898.elfofflinett.services.FetchTimeTableService;
import tk.cs8898.elfofflinett.services.NotificationService;

import static tk.cs8898.elfofflinett.model.Common.ACTIVITY_VIEW_ALL;
import static tk.cs8898.elfofflinett.model.Common.ACTIVITY_VIEW_MARKED;
import static tk.cs8898.elfofflinett.model.Common.PREFERENCES_NAME;
import static tk.cs8898.elfofflinett.model.Common.PREF_AUTOSTARTED_NAME;
import static tk.cs8898.elfofflinett.model.Common.PREF_NOTIFICATION_EARLIER_TIME_NAME;
import static tk.cs8898.elfofflinett.model.Common.PREF_NOTIFICATION_LAST_SHOWN_ONSTAGE_TIME_NAME;
import static tk.cs8898.elfofflinett.model.Common.PREF_NOTIFICATION_LAST_SHOWN_UPCOMING_TIME_NAME;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private WeekView mWeekView;

    private String currentView;
    private SubMenu filterMenu;

    private Set<String> filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //STUFF DONE For Week View
        mWeekView = findViewById(R.id.my_weekview);

        MyWeekViewListener mWeekViewListener = new MyWeekViewListener();
        mWeekView.setOnEventClickListener(mWeekViewListener);
        mWeekView.setMonthChangeListener(mWeekViewListener);
        mWeekView.setEventLongPressListener(mWeekViewListener);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> goToToday());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currentView = navigationView.getMenu().findItem(R.id.nav_home).isChecked() ? ACTIVITY_VIEW_MARKED : ACTIVITY_VIEW_ALL;

        filters = new HashSet<>();
        filterMenu = navigationView.getMenu().findItem(R.id.nav_filter_menu).getSubMenu();
        //NotificationService.startActionInitNotification(this);
        NotificationService.scheduleInitNotification(this);
        //fetch the timeTable from storage, if there is none its going to fetch it online anyways
        //FetchTimeTableService.startActionFetchTimetable(this, true, true);
        //FetchTimeTableService.scheduleFetchTimetable(this,true,true);
    }

    @Override
    public void onStart() {
        super.onStart();
        //NotificationService.startActionTriggerNotification(this,Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"),Locale.GERMANY).getTimeInMillis());
        goToToday();
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        SharedPreferences prefs = getSharedPreferences(PREFERENCES_NAME,MODE_PRIVATE);
        boolean autoStarted = prefs.getBoolean(PREF_AUTOSTARTED_NAME,false);
        if(!autoStarted){
            Log.d("MainActivity","Was not Autostarted Need To Start services now");
            getSharedPreferences(PREFERENCES_NAME,MODE_PRIVATE).edit()
                    .remove(PREF_NOTIFICATION_LAST_SHOWN_ONSTAGE_TIME_NAME)
                    .remove(PREF_NOTIFICATION_LAST_SHOWN_UPCOMING_TIME_NAME)
                    .apply();
            try {
                FetchTimeTableService.startActionFetchTimetable(this, true, true);
                NotificationService.startActionInitNotification(this);
            } catch (IllegalStateException e) {
                Log.d("MainActivity", "The App is in Background Using Scheduler Now");
                FetchTimeTableService.scheduleFetchTimetable(this, true, true);
                NotificationService.scheduleInitNotification(this);
            }
        }else{
            Log.d("MainActivity","I Was Autostarted, so i wont init the Notifications, but will trigger a redraw");
            //BusProvider.getInstance().post(new MessageDatasetChanged(Object.class));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(MarkedActsService.class.getSimpleName(), "MAIN TRIGGER ON PAUSE");
        MarkedActsService.saveMarks(getApplicationContext(), true);
        BusProvider.getInstance().unregister(this);
    }
    
    @Override
    public void onDestroy() {
        Log.d("MainActivity", "Here is the Destroy!!!");
        getSharedPreferences(PREFERENCES_NAME,MODE_PRIVATE).edit()
                .putBoolean(PREF_AUTOSTARTED_NAME,false)
                .commit();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            FetchTimeTableService.startActionFetchTimetable(this);
            return true;
        } else if (id == R.id.action_change_daycount) {
            mWeekView.setNumberOfVisibleDays(mWeekView.getNumberOfVisibleDays() % 3 + 1);
        } else if (id == R.id.action_set_notification_earlier_time) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final SeekBar bar = new SeekBar(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bar.setMin(0);
            }
            bar.setMax(30);
            int earlier_time = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(PREF_NOTIFICATION_EARLIER_TIME_NAME, 0);
            bar.setProgress(earlier_time);

            builder.setView(bar);
            builder.setCancelable(true);
            builder.setTitle(String.format(Locale.GERMAN, "Notification %d minutes before start.", earlier_time));
            builder.setPositiveButton("Apply", (dialog, which) -> getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putInt(PREF_NOTIFICATION_EARLIER_TIME_NAME,
                    bar.getProgress()).apply());
            final AlertDialog dialog = builder.show();
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    dialog.setTitle(String.format(Locale.GERMAN, "Notification %d minutes before start.", progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                currentView = ACTIVITY_VIEW_MARKED;
                mWeekView.notifyDatasetChanged();
                break;
            case R.id.nav_all:
                currentView = ACTIVITY_VIEW_ALL;
                mWeekView.notifyDatasetChanged();
                break;
            case R.id.nav_licence:
                Intent settingIntent = new Intent(this, LicenseActivity.class);
                startActivity(settingIntent);
                break;
            default:
                String filterStageTitle = item.getTitle().toString();
                if (filters.contains(filterStageTitle)) {
                    filters.remove(filterStageTitle);
                    item.setIcon(R.drawable.ic_check_box);
                } else {
                    filters.add(filterStageTitle);
                    item.setIcon(R.drawable.ic_check_box_outline);
                }
                mWeekView.notifyDatasetChanged();
                return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class MyWeekViewListener implements MonthLoader.MonthChangeListener, WeekView.EventClickListener, WeekView.EventLongPressListener {

        @Override
        public void onEventClick(WeekViewEvent event, RectF eventRect) {
            if (ACTIVITY_VIEW_ALL.equals(currentView)){
                toggleEvent(event);
            }
        }

        @Override
        public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
            toggleEvent(event);
        }

        private void toggleEvent(WeekViewEvent event) {
            InternalActEntity act = MarkedActsService.findAct(event.getIdentifier());
            if (act != null) {
                act.setMarked(!act.isMarked());
                mWeekView.notifyDatasetChanged();
                MarkedActsService.saveMarks(getApplicationContext());
                NotificationService.startActionInitNotification(getApplicationContext());
            }
        }

        @Override
        public List<? extends WeekViewEvent> onMonthChange(final int newYear, final int newMonth) {
            //return null;
            //List<WeekViewEvent> events = getEvents(newYear, newMonth);
            populateFilters();
            Collection<InternalActEntity> acts;
            switch (currentView) {
                case ACTIVITY_VIEW_MARKED:
                    //Log.d("MainActivity", "Marked View");
                    acts = MarkedActsService.getMarked();
                    break;
                case ACTIVITY_VIEW_ALL:
                    //Log.d("MainActivity", "All View");
                    acts = MarkedActsService.getActs();
                    break;
                default:
                    Log.e("MainActivity", "No Current view was set");
                    acts = new ArrayList<>();
                    break;
            }

            //Log.d("MainActivity", "Redrawing with " + acts.size() + " Acts");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //Log.d("MainActivity", "USING STREAM");
                return acts.parallelStream()
                        .filter(e -> !filters.contains(e.getLocation()) &&
                                newYear == e.getTime().get(Calendar.YEAR) &&
                                newMonth - 1 == e.getTime().get(Calendar.MONTH))
                        .map(this::buildEvent).collect(Collectors.toList());
            } else {
                List<WeekViewEvent> eventsList = new ArrayList<>();
                for (InternalActEntity act : acts) {
                    if (filters.contains(act.getLocation()))
                        continue;
                    Calendar actStart = act.getTime();
                    Calendar actEnd = act.getEnd();
                    if (actStart == null || actEnd == null)
                        continue;
                    if (actStart.get(Calendar.YEAR) == newYear && actStart.get(Calendar.MONTH) == newMonth - 1) {
                        //FILTERS
                        eventsList.add(buildEvent(act));
                    }
                }
                return eventsList;
            }
        }

        private WeekViewEvent buildEvent(InternalActEntity act) {
            WeekViewEvent event = new WeekViewEvent(act.toString(), act.getName(), act.getLocation(), act.getTime(), act.getEnd());
            event.setColor(act.getColor());
            event.setPriority(act.getLocation().hashCode());
            return event;
        }
    }

    private void populateFilters() {
        filterMenu.clear();
        //filters.clear();
        int i = 0;
        for (StageEntity stage : MarkedActsService.getStages()) {
            //filters.add(location);
            SpannableString location = new SpannableString(stage.getName());
            location.setSpan(new ForegroundColorSpan(Color.parseColor(stage.getColorA())), 0, location.length(), 0);
            MenuItem item = filterMenu.add(R.id.nav_filter_group, Menu.NONE, i++, location);
            //item.setCheckable(false);
            if (filters.contains(location.toString()))
                item.setIcon(R.drawable.ic_check_box_outline);
            else
                item.setIcon(R.drawable.ic_check_box);
        }
    }

    private void goToToday() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY);
        if (mWeekView.getMinDate() != null && now.before(mWeekView.getMinDate())) {
            mWeekView.goToDate(mWeekView.getMinDate());
        } else if (mWeekView.getMaxDate() != null && now.after(mWeekView.getMaxDate())) {
            mWeekView.goToDate(mWeekView.getMaxDate());
        } else {
            mWeekView.goToDate(now);
            mWeekView.goToHour(now.get(Calendar.HOUR_OF_DAY));
        }
    }

    @Subscribe
    public void onDatasetChanged(MessageDatasetChanged message) {
        if (!message.getOrigin().equals(MainActivity.class)) {
            runOnUiThread(() -> {
                mWeekView.setMinDate(MarkedActsService.getMinDate());
                mWeekView.setMaxDate(MarkedActsService.getMaxDate());
                mWeekView.invalidate();
                mWeekView.notifyDatasetChanged();
            });
        }
    }
}
