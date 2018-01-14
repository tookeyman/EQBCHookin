package com.geniuscartel.characters;

public class Stats {
    private int hp = 0, mana = 0, end = 0, hpmax = 0, endmax = 0, manamax = 0, level = 0, xp = 0;
    private Location pos;

    public void setHp(String hpString) {
        String[] couplet = hpString.split("=");
        String[] currMax = couplet[1].split("/");
        hp = Integer.parseInt(currMax[0]);
        hpmax = Integer.parseInt(currMax[1]);
    }

    public void setMana(String manaString) {
        String[] couplet = manaString.split("=");
        String[] currMax = couplet[1].split("/");
        if (currMax.length == 1 || currMax.length == 0) {
            mana = -1;
            manamax = -1;
        } else {
            mana = Integer.parseInt(currMax[0]);
            manamax = Integer.parseInt(currMax[1]);
        }
    }

    public void setEnd(String endString) {
        String[] couplet = endString.split("=");
        String[] currMax = couplet[1].split("/");
        end = Integer.parseInt(currMax[0]);
        endmax = Integer.parseInt(currMax[1]);
    }

    public void setLevel(String levelString) {
        level = 1;
    }

    public void setXp(String xpString) {
        xp = 1;
    }

    public void setLocation(String locString){
        String[] couplet = locString.split("=");
        String[] coords = couplet[1].split(":");
        Location loc = new Location(Float.parseFloat(coords[1]), Float.parseFloat(coords[0]), Float.parseFloat(coords[2]), Integer.parseInt(coords[3]));
        this.pos = loc;
    }

    @Override
    public String toString() {
        return "Stats{" +
            "hp=" + hp +
            ", mana=" + mana +
            ", end=" + end +
            ", hpmax=" + hpmax +
            ", endmax=" + endmax +
            ", manamax=" + manamax +
            ", pos=" + pos +
            '}';
    }
}
