package tk.cs8898.elfofflinett.model.entity;

import java.util.List;

public class StageEntity {
    private String name;
    private List<ActEntity> acts;

    public StageEntity(String name, List<ActEntity> acts) {
        this.name = name;
        this.acts = acts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ActEntity> getActs() {
        return acts;
    }

    public void setActs(List<ActEntity> acts) {
        this.acts = acts;
    }

    public String toString(){
        return this.name;
    }
}
