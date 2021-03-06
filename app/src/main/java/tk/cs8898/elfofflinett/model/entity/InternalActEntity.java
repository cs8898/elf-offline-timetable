package tk.cs8898.elfofflinett.model.entity;

import android.graphics.Color;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class InternalActEntity {
    //July 5, 2018 15:45
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.US);

    private ActEntity act;
    private StageEntity stage;
    private boolean marked;
    private final Calendar time;
    private final Calendar end;

    public InternalActEntity(StageEntity stage, ActEntity act) {
        this(stage, act, false);
    }

    public InternalActEntity(StageEntity stage, ActEntity act, boolean marked) {
        this.stage = stage;
        this.act = act;
        this.marked = marked;
        this.time = getCalendarTime();
        this.end = getCalendarEnd();
        Log.d("InternalActEntityConst",this.act.getAct()+" "+this.act.getTime()+" -> "+time.getTimeInMillis());
        if(end.before(time))
            throw new IllegalArgumentException("End is Before Start "+this.toString());
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

    private Calendar getCalendarTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"),Locale.GERMANY);
        try {
            cal.setTime(dateFormat.parse(this.act.getTime()));
        } catch (Exception e) {
            //e.printStackTrace();
            //Log.d("InternalActEntry", this.act.getAct()+" Time is "+this.act.getTime());
            cal.setTimeInMillis(0);
            //return null;
        }
        return cal;
    }
    public Calendar getTime(){
        return time;
    }
    private Calendar getCalendarEnd() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"),Locale.GERMANY);
        try {
            cal.setTime(dateFormat.parse(this.act.getEnd()));
        } catch (Exception e) {
            e.printStackTrace();
            cal.setTimeInMillis(0);
            //return null;
        }
        return cal;
    }
    public Calendar getEnd(){
        return end;
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

    public int getColor() {
        if(isMarked()){
            return Color.parseColor(getStage().getColorA());
        }else{
            return Color.parseColor(getStage().getColorB());
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
