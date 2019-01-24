package tk.cs8898.elfofflinett.model.entity;

public class ActEntity {
    private String time;
    private String act;
    private String end;

    public ActEntity(String time, String act, String end) {
        this.time = time.trim().replaceAll("\"","");
        this.act = act;
        this.end = end.trim().replaceAll("\"","");
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time.trim().replaceAll("\"","");
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end.trim().replaceAll("\"","");
    }

    public String toString(){
           return this.time+";"+this.act+";"+this.end;
    }
}
