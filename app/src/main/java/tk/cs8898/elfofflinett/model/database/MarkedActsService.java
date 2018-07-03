package tk.cs8898.elfofflinett.model.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.alamkanak.weekview.WeekView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tk.cs8898.elfofflinett.model.entity.ActEntity;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.model.entity.StageEntity;

public class MarkedActsService {
    private static final String PREFERENCES_NAME = "tk.cs8898.elfofflinett.preferences";
    private static final String MARKED_NAME = "marked";

    private static WeekView weekView;
    private static Map<String, InternalActEntity> actMap;
    private static List<InternalActEntity> acts;

    public static Set<String> getMarkedString() {
        Set<String> marked = new HashSet<>();
        if (acts != null)
            for (InternalActEntity act : acts) {
                if (act.isMarked()) {
                    marked.add(act.toString());
                }
            }
        return marked;
    }

    public static Collection<InternalActEntity> getActs() {
        if (acts != null)
            return acts;
        return new ArrayList<>();
    }

    public static Collection<InternalActEntity> getMarked() {
        HashSet<InternalActEntity> marked = new HashSet<>();
        if (acts != null) {
            for (InternalActEntity act : acts) {
                if (act.isMarked())
                    marked.add(act);
            }
        }
        return marked;
    }

    public static void setAllActs(Context context, StageEntity[] stages) {
        //saveMarks(context);
        int i = 0;
        acts = new ArrayList<>();
        for (StageEntity stage : stages) {
            for (ActEntity act : stage.getActs()) {
                acts.add(new InternalActEntity(stage, act, i++));
            }
        }
        loadMarks(context);
        weekView.notifyDatasetChanged();
    }

    public static void saveMarks(Context context){
        saveMarks(context,false);
    }

    public static void saveMarks(Context context, boolean force) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit().putStringSet(MARKED_NAME, getMarkedString());
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
                Log.d("MarkedActsService","Marking "+marked);
                for(InternalActEntity act: acts){
                    if(act.toString().equals(marked)) {
                        act.setMarked(true);
                        break;
                    }
                }
            }
        }
    }

    public static void setWeekView(WeekView weekView) {
        MarkedActsService.weekView = weekView;
    }

    public static InternalActEntity findAct(long id) {
        if (acts != null)
            for (InternalActEntity act : acts) {
                if (act.getId() == id) {
                    return act;
                }
            }
        return null;
    }

    public static Set<String> getLocations() {
        HashSet<String> locations = new HashSet<>();
        if (acts != null)
            for (InternalActEntity act : acts) {
                locations.add(act.getLocation());
            }
        return locations;
    }
}
