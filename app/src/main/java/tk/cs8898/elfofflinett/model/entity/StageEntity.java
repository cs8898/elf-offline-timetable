package tk.cs8898.elfofflinett.model.entity;

import java.util.List;

public class StageEntity {
    private String name;
    private String colorA;
    private String colorB;
    private List<ActEntity> acts;

    public StageEntity(String name,String colorA, String colorB, List<ActEntity> acts) {
        this.name = name;
        this.acts = acts;
        this.colorA = colorA;
        this.colorB = colorB;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorA() {
        return colorA;
    }

    public void setColorA(String colorA) {
        this.colorA = colorA;
    }

    public String getColorB() {
        return colorB;
    }

    public void setColorB(String colorB) {
        this.colorB = colorB;
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
