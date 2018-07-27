package tk.cs8898.elfofflinett.model.entity;

import android.graphics.Color;

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

    public int getColor() {
        if(isMarked()){
            return Color.parseColor(getStage().getColorA());
        }else{
            return Color.parseColor(getStage().getColorB());
        }
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
