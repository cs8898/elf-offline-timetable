package tk.cs8898.elfofflinett.services.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import tk.cs8898.elfofflinett.model.Common;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.ActEntity;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.model.entity.StageEntity;
import tk.cs8898.elfofflinett.testabstraction.TestContext;
import tk.cs8898.elfofflinett.testabstraction.TestHelper;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class NotificationLogicTest {
    private Calendar startOfEvents;
    private Calendar endOfEvents;
    private Set<String> marked;
    private List<InternalActEntity> allActs;

    @Parameter
    public int SDK_INT;
    private int OLD_SDK_INT = Build.VERSION.SDK_INT;

    @Parameters(name = "SKD_{0}")
    public static Collection<Integer> data(){
        return Arrays.asList(16,24,26,28);
    }

    @Before
    public void initMarkedActsService(){
        try {
            TestHelper.setFinalStatic(Build.VERSION.class.getField("SDK_INT"),SDK_INT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("PRINT_OUTPUT","false"); //DISABLE THE LOG OUTPUT
        startOfEvents = Calendar.getInstance(Locale.GERMANY);
        startOfEvents.set(Calendar.YEAR,2019);
        startOfEvents.set(Calendar.MONTH,Calendar.JANUARY);
        startOfEvents.set(Calendar.DAY_OF_MONTH,15);
        startOfEvents.set(Calendar.HOUR_OF_DAY,0);
        startOfEvents.set(Calendar.MINUTE,0);
        startOfEvents.set(Calendar.SECOND,0);
        startOfEvents.set(Calendar.MILLISECOND,0);

        endOfEvents = (Calendar) startOfEvents.clone();
        endOfEvents.set(Calendar.DAY_OF_MONTH,21);

        allActs = new ArrayList<>();
        marked = new HashSet<>();

        // Build The Stage Array
        StageEntity[] stages = {
                new StageEntity("Test0","#aa0000", "#ff0000", new ArrayList<>()),
                new StageEntity("Test1","#00aa00", "#00ff00",new ArrayList<>()),
                new StageEntity("Test2","#0000aa", "#0000ff",new ArrayList<>()),
        };
        for(int d = 15; d <= 20; d++) {
            for (int h = 1; h <= 23; h++) {
                stages[0].getActs().add(new ActEntity("January " + d + ", 2019 " + String.format(Locale.GERMAN, "%02d", h) + ":00", "0-" + d + "-" + h, "January " + d + ", 2019 " + String.format(Locale.GERMAN, "%02d", h) + ":45"));
                stages[1].getActs().add(new ActEntity("January " + d + ", 2019 " + String.format(Locale.GERMAN, "%02d", h) + ":15", "1-" + d + "-" + h, "January " + d + ", 2019 " + String.format(Locale.GERMAN, "%02d", h) + ":45"));
                stages[2].getActs().add(new ActEntity("January " + d + ", 2019 " + String.format(Locale.GERMAN, "%02d", h) + ":30", "2-" + d + "-" + h, "January " + (d+(h==23?1:0)) + ", 2019 " + String.format(Locale.GERMAN, "%02d", (h+1)%24) + ":00"));
            }
        }
        // END Build The Stage Array

        // Generate The Marked Set
        for (StageEntity stage : stages) {
            for (ActEntity act : stage.getActs()) {
                InternalActEntity internalActEntity = new InternalActEntity(stage, act);
                allActs.add(internalActEntity);
            }
        }
        for(int i = 0; i < 100; i++){
            int offset = 5;
            do {
                offset = (offset+3*i)%allActs.size();
            }while(allActs.get(offset).isMarked());
            marked.add(allActs.get(offset).toString());
            allActs.get(offset).setMarked(true);
        }
        // END Generate The Marked Set

        TestContext context = TestContext.getInstance();
        SharedPreferences prefs = context.getSharedPreferences("foo",42);
        prefs.edit().putStringSet(Common.PREF_MARKED_NAME,marked).apply();

        MarkedActsService.setAllActs(context,stages,false);
    }

    @Test
    public void areActsAvailable(){
        assertFalse("Acts aren't Empty",MarkedActsService.getActs().isEmpty());
        assertEquals("All Acts added",allActs.size(),MarkedActsService.getActs().size());
        assertEquals("All Marks added",marked.size(),MarkedActsService.getMarked().size());
        MarkedActsService.getMarked().forEach(
                e->assertTrue(e.toString()+" is really marked",marked.contains(e.toString()))
        );
    }

    @Test
    public void findNextTriggerTime() {
        //Context context = TestContext.getInstance();
        Calendar notBefore = (Calendar) startOfEvents.clone();
        Calendar minTime = (Calendar) endOfEvents.clone();
        for (InternalActEntity act: allActs){
            if(act.isMarked() && act.getTime().before(minTime) && act.getTime().after(notBefore)){
                minTime = act.getTime();
            }
        }
        //SharedPreferences prefs = context.getSharedPreferences("foo",42);
        //prefs.edit().putInt(Common.PREF_NOTIFICATION_EARLIER_TIME_NAME,0).apply(); //DISABLED EARLIER TRIGGER
        long initTime = NotificationLogic.findNextTriggerTime(startOfEvents,startOfEvents,startOfEvents,0);
        assertEquals("Same Init value hardcoded",1547518500000L,initTime);
        assertEquals("Same Init value calc", minTime.getTimeInMillis(), initTime);
        long initEarlierTime = NotificationLogic.findNextTriggerTime(startOfEvents, startOfEvents, startOfEvents, 12345);
        assertEquals("Same Init Earlier Value", minTime.getTimeInMillis()-12345,initEarlierTime);

        minTime = (Calendar) endOfEvents.clone();
        notBefore = (Calendar) allActs.get(11).getEnd().clone();
        notBefore.add(Calendar.MINUTE, -2);
        for (InternalActEntity act: allActs){
            if(act.isMarked() && act.getEnd().before(minTime) && act.getEnd().after(notBefore)){
                minTime = act.getEnd();
            }
        }
        long someEndTime = NotificationLogic.findNextTriggerTime(notBefore,notBefore,notBefore,0);
        assertEquals("Some End Time", minTime.getTimeInMillis(),someEndTime);
    }

    @Test
    public void getCurrentActs() {
    }

    @Test
    public void getUpcomingActs() {
    }

    @After
    public void resetSDK(){
        try {
            TestHelper.setFinalStatic(Build.VERSION.class.getField("SDK_INT"),OLD_SDK_INT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}