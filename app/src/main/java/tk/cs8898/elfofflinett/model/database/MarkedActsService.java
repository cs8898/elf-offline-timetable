package tk.cs8898.elfofflinett.model.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import tk.cs8898.elfofflinett.model.bus.BusProvider;
import tk.cs8898.elfofflinett.model.bus.messages.MessageDatasetChanged;
import tk.cs8898.elfofflinett.model.entity.ActEntity;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.model.entity.StageEntity;

import static tk.cs8898.elfofflinett.model.Common.PREFERENCES_NAME;
import static tk.cs8898.elfofflinett.model.Common.PREF_MARKED_NAME;

public class MarkedActsService {

    public static final Object actsLock = new Object();

    private static List<InternalActEntity> acts;

    private static Set<String> getMarkedStrings() {
        Set<String> marked = new HashSet<>();
        synchronized (actsLock) {
            if (acts != null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return acts.parallelStream()
                            .filter(InternalActEntity::isMarked)
                            .map(InternalActEntity::toString)
                            .collect(Collectors.toSet());
                } else {
                    for (InternalActEntity act : acts) {
                        if (act.isMarked()) {
                            marked.add(act.toString());
                        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return acts.parallelStream()
                            .filter(InternalActEntity::isMarked)
                            .collect(Collectors.toSet());
                } else {
                    for (InternalActEntity act : acts) {
                        if (act.isMarked())
                            marked.add(act);
                    }
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
        Log.d(MarkedActsService.class.getSimpleName(), "Saving " + getMarkedStrings().size() + "Marks Forced: " + force);
        if (getActs().size() == 0) {
            Log.d(MarkedActsService.class.getSimpleName(), "Ignoring Save, empty Event List");
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit().putStringSet(PREF_MARKED_NAME, getMarkedStrings());
        if (force)
            editor.commit();
        else
            editor.apply();
    }

    private static void loadMarks(Context context) {
        if (acts != null) {
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            Set<String> markSet = preferences.getStringSet(PREF_MARKED_NAME, new HashSet<String>());

            assert markSet != null;
            Log.d(MarkedActsService.class.getSimpleName(), "Loaded " + markSet.size() + " Marks from Prefs");
            synchronized (actsLock) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    acts.parallelStream()
                            .filter(e -> markSet.contains(e.toString()))
                            .forEach(e -> e.setMarked(true));
                } else {
                    //Log.d("MarkedActsService", "Marking " + marked);
                    for (InternalActEntity act : acts) {
                        if (markSet.contains(act.toString())) {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return acts.parallelStream()
                            .filter(e -> e.getId() == id)
                            .findFirst().orElse(null);
                } else {
                    for (InternalActEntity act : acts) {
                        if (act.getId() == id) {
                            return act;
                        }
                    }
                }
        }
        return null;
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
        synchronized (actsLock) {
            if (acts != null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return acts.parallelStream()
                            .map(InternalActEntity::getLocation)
                            .collect(Collectors.toSet());
                } else {
                    HashSet<String> locations = new HashSet<>();
                    for (InternalActEntity act : acts) {
                        locations.add(act.getLocation());
                    }
                    return locations;
                }
        }
        return new HashSet<>();
    }

    public static Set<StageEntity> getStages() {

        synchronized (actsLock) {
            if (acts != null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return acts.parallelStream()
                            .map(InternalActEntity::getStage)
                            .collect(Collectors.toSet());
                } else {
                    HashSet<StageEntity> stages = new HashSet<>();
                    for (InternalActEntity act : acts) {
                        stages.add(act.getStage());
                    }
                    return stages;
                }
        }
        return new HashSet<>();
    }

    public static Calendar getMinDate() {
        Calendar min = null;

        synchronized (actsLock) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                InternalActEntity act = acts.parallelStream().min((a, b) -> a.getTime().compareTo(b.getTime())).orElseGet(null);
                if (act != null)
                    min = act.getTime();
            } else {
                for (InternalActEntity act : acts) {
                    if (min == null || act.getTime().before(min)) {
                        min = act.getTime();
                    }
                }
            }
        }
        return min;
    }

    public static Calendar getMaxDate() {
        Calendar max = null;

        synchronized (actsLock) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                InternalActEntity act = acts.parallelStream().max((a, b) -> a.getEnd().compareTo(b.getEnd())).orElseGet(null);
                if (act != null)
                    max = act.getEnd();
            } else {
                for (InternalActEntity act : acts) {
                    if (max == null || act.getEnd().after(max)) {
                        max = act.getEnd();
                    }
                }
            }
        }
        return max;
    }
}
