package tk.cs8898.elfofflinett.model.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tk.cs8898.elfofflinett.model.bus.BusProvider;
import tk.cs8898.elfofflinett.model.bus.messages.MessageDatasetChanged;
import tk.cs8898.elfofflinett.model.entity.ActEntity;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.model.entity.StageEntity;

public class MarkedActsService {
    private static final String PREFERENCES_NAME = "tk.cs8898.elfofflinett.preferences";
    private static final String MARKED_NAME = "marked";

    private static final Object actsLock = new Object();

    private static List<InternalActEntity> acts;

    private static Set<String> getMarkedStrings() {
        Set<String> marked = new HashSet<>();
        synchronized (actsLock) {
            if (acts != null)
                for (InternalActEntity act : acts) {
                    if (act.isMarked()) {
                        marked.add(act.toString());
                    }
                }
        }
        return marked;
    }

    public static Collection<InternalActEntity> getActs() {
        synchronized (actsLock) {
            if (acts != null)
                return acts;
        }
        return new ArrayList<>();
    }

    public static Collection<InternalActEntity> getMarked() {
        HashSet<InternalActEntity> marked = new HashSet<>();
        synchronized (actsLock) {
            if (acts != null) {
                for (InternalActEntity act : acts) {
                    if (act.isMarked())
                        marked.add(act);
                }
            }
        }
        return marked;
    }

    public static void setAllActs(Context context, StageEntity[] stages, boolean update) {
        //saveMarks(context);
        int i = 0;
        synchronized (actsLock) {
            acts = new ArrayList<>();
            for (StageEntity stage : stages) {
                for (ActEntity act : stage.getActs()) {
                    acts.add(new InternalActEntity(stage, act, i++));
                }
            }
        }
        loadMarks(context);
        if (update) {
            BusProvider.getInstance().post(new MessageDatasetChanged(MarkedActsService.class));
        }
    }

    public static void saveMarks(Context context) {
        saveMarks(context, false);
    }

    @SuppressLint("ApplySharedPref")
    public static void saveMarks(Context context, boolean force) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit().putStringSet(MARKED_NAME, getMarkedStrings());
        if (force)
            editor.commit();
        else
            editor.apply();
    }

    private static void loadMarks(Context context) {
        if (acts != null) {
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            Set<String> markSet = preferences.getStringSet(MARKED_NAME, new HashSet<String>());
            for (String marked : markSet) {
                //Log.d("MarkedActsService", "Marking " + marked);
                synchronized (actsLock) {
                    for (InternalActEntity act : acts) {
                        if (act.toString().equals(marked)) {
                            act.setMarked(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static InternalActEntity findAct(long id) {
        synchronized (actsLock) {
            if (acts != null)
                for (InternalActEntity act : acts) {
                    if (act.getId() == id) {
                        return act;
                    }
                }
            return null;
        }
    }

    public static InternalActEntity findAct(String actString) {
        synchronized (actsLock) {
            if (acts != null)
                for (InternalActEntity act : acts) {
                    if (act.toString().equals(actString)) {
                        return act;
                    }
                }
        }
        return null;
    }

    public static Set<String> getLocations() {
        HashSet<String> locations = new HashSet<>();

        synchronized (actsLock) {
        if (acts != null)
                for (InternalActEntity act : acts) {
                    locations.add(act.getLocation());
                }
        }
        return locations;
    }

    public static Calendar getMinDate() {
        Calendar min = null;

        synchronized (actsLock) {
            for (InternalActEntity act : acts) {
                if (min == null || act.getTime().before(min)) {
                    min = act.getTime();
                }
            }
        }
        return min;
    }

    public static Calendar getMaxDate() {
        Calendar max = null;

        synchronized (actsLock) {
            for (InternalActEntity act : acts) {
                if (max == null || act.getEnd().after(max)) {
                    max = act.getEnd();
                }
            }
        }
        return max;
    }
}
