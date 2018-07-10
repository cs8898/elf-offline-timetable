package tk.cs8898.elfofflinett.activity;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

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

import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.model.bus.BusProvider;
import tk.cs8898.elfofflinett.model.bus.messages.MessageDatasetChanged;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.services.FetchTimeTableService;
import tk.cs8898.elfofflinett.services.NotificationService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String MARKED_VIEW = "tk.cs8898.elfofflinett.view.marked";
    private static final String ALL_VIEW = "tk.cs8898.elfofflinett.view.all";

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToToday();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currentView = navigationView.getMenu().findItem(R.id.nav_home).isChecked() ? MARKED_VIEW : ALL_VIEW;

        filters = new HashSet<>();
        filterMenu = navigationView.getMenu().findItem(R.id.nav_filter_menu).getSubMenu();
    }

    @Override
    public void onStart(){
        super.onStart();
        BusProvider.getInstance().register(this);
        FetchTimeTableService.startActionFetchTimetable(this);
        NotificationService.startActionInitNotification(this);
        goToToday();
    }

    @Override
    public void onPause(){
        BusProvider.getInstance().unregister(this);
        super.onPause();
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                currentView = MARKED_VIEW;
                mWeekView.notifyDatasetChanged();
                break;
            case R.id.nav_all:
                currentView = ALL_VIEW;
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
                    filters.add((String) item.getTitle());
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
            if (ALL_VIEW.equals(currentView))
                toggleEvent(event.getIdentifier());
        }

        @Override
        public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
            toggleEvent(event.getIdentifier());
        }

        private void toggleEvent(String identifier) {
            InternalActEntity act = MarkedActsService.findAct(identifier);
            if (act != null) {
                act.setMarked(!act.isMarked());
                mWeekView.notifyDatasetChanged();
                MarkedActsService.saveMarks(getApplicationContext());
                NotificationService.startActionInitNotification(getApplicationContext());
            }
        }

        @Override
        public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
            //return null;
            //List<WeekViewEvent> events = getEvents(newYear, newMonth);
            populateFilters();
            Collection<InternalActEntity> acts;
            switch (currentView) {
                case MARKED_VIEW:
                    //Log.d("MainActivity", "Marked View");
                    acts = MarkedActsService.getMarked();
                    break;
                case ALL_VIEW:
                    //Log.d("MainActivity", "All View");
                    acts = MarkedActsService.getActs();
                    break;
                default:
                    Log.e("MainActivity", "No Current view was set");
                    acts = new ArrayList<>();
                    break;
            }
            //Log.d("MainActivity", "Loading events for " + newYear + "-" + newMonth);
            List<WeekViewEvent> eventsList = new ArrayList<>();
            for (InternalActEntity act : acts) {
                Calendar actStart = act.getTime();
                Calendar actEnd = act.getEnd();
                if (actStart != null && actEnd != null) {
                    if (actStart.get(Calendar.YEAR) == newYear && actStart.get(Calendar.MONTH) == newMonth - 1) {
                        //FILTERS
                        if (filters.contains(act.getLocation()))
                            continue;
                        WeekViewEvent event = new WeekViewEvent(act.toString(), act.getName(), act.getLocation(), actStart, actEnd);
                        event.setColor(act.getColor(getApplicationContext()));
                        eventsList.add(event);
                    }
                }
            }
            return eventsList;
        }
    }

    private void populateFilters() {
        filterMenu.clear();
        //filters.clear();
        int i = 0;
        for (String location : MarkedActsService.getLocations()) {
            //filters.add(location);
            MenuItem item = filterMenu.add(R.id.nav_filter_group, Menu.NONE, i++, location);
            //item.setCheckable(false);
            if (filters.contains(location))
                item.setIcon(R.drawable.ic_check_box_outline);
            else
                item.setIcon(R.drawable.ic_check_box);
        }
    }

    private void goToToday(){
        Calendar now = Calendar.getInstance(Locale.GERMANY);
        if(mWeekView.getMinDate()!=null && now.before(mWeekView.getMinDate())){
            mWeekView.goToDate(mWeekView.getMinDate());
        }else if(mWeekView.getMaxDate()!=null && now.after(mWeekView.getMaxDate())){
            mWeekView.goToDate(mWeekView.getMaxDate());
        }else{
            mWeekView.goToDate(now);
            mWeekView.goToHour(now.get(Calendar.HOUR_OF_DAY));
        }
    }

    @Subscribe
    public void onDatasetChanged(MessageDatasetChanged message){
        if(!message.getOrigin().equals(MainActivity.class)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWeekView.setMinDate(MarkedActsService.getMinDate());
                    mWeekView.setMaxDate(MarkedActsService.getMaxDate());
                    mWeekView.invalidate();
                    mWeekView.notifyDatasetChanged();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        MarkedActsService.saveMarks(getApplicationContext(), true);
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }
}
