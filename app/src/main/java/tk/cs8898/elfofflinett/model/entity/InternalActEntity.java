package tk.cs8898.elfofflinett.model.entity;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import tk.cs8898.elfofflinett.R;

public class InternalActEntity {
    //July 5, 2018 15:45
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.US);

    private ActEntity act;
    private StageEntity stage;
    private boolean marked;
    private long id;

    public InternalActEntity(StageEntity stage, ActEntity act, long id) {
        this(stage, act, id, false);
    }

    public InternalActEntity(StageEntity stage, ActEntity act, long id, boolean marked) {
        this.stage = stage;
        this.act = act;
        this.id = id;
        this.marked = marked;
    }

    public ActEntity getAct() {
        return act;
    }

    public void setAct(ActEntity act) {
        this.act = act;
    }

    public StageEntity getStage() {
        return stage;
    }

    public void setStage(StageEntity stage) {
        this.stage = stage;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public boolean isMarked() {
        return marked;
    }

    public Calendar getTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"),Locale.GERMANY);
        try {
            cal.setTime(dateFormat.parse(this.act.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return cal;
    }

    public Calendar getEnd() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"),Locale.GERMANY);
        try {
            cal.setTime(dateFormat.parse(this.act.getEnd()));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return cal;
    }

    @Override
    public String toString() {
        return this.stage.toString() + ";" + this.act.toString();
    }

    public String getName() {
        return this.act.getAct();
    }

    public String getLocation() {
        return this.stage.getName();
    }

    public int getColor(Context context) {
        int color;
        
        switch (getLocation()) {
            case "Mainstage":
                if (isMarked())
                    color = ContextCompat.getColor(context, R.color.mainstage);
                else
                    color = ContextCompat.getColor(context, R.color.mainstage_alt);
                break;
            case "Club Circus":
                if (isMarked())
                    color = ContextCompat.getColor(context, R.color.clubcircous);
                else
                    color = ContextCompat.getColor(context, R.color.clubcircous_alt);
                break;
            case "Heineken Starclub":
                if (isMarked())
                    color = ContextCompat.getColor(context, R.color.heineken);
                else
                    color = ContextCompat.getColor(context, R.color.heineken_alt);
                break;
            case "Q-Dance":
                if (isMarked())
                    color = ContextCompat.getColor(context, R.color.qdance);
                else
                    color = ContextCompat.getColor(context, R.color.qdance_alt);
                break;
            default:
                color = Color.BLACK;
        }
        return color;
    }

    public void setId(long id){
        this.id = id;
    }

    public long getId(){
        return id;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
