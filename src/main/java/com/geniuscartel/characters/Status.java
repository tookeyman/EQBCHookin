package com.geniuscartel.characters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Status {
    private int casting = 0, id = -1, sitState = 0;
    private int hp = 0, mana = 0, end = 0, hpmax = 0, endmax = 0, manamax = 0, level = 0, xp = 0;
    private Location loc;
    private CharacterState state = CharacterState.REST;
    private List<Integer> buffs = null, buffDuration = null;
    private final EQCharacter me;

    public Status(EQCharacter me) {
        this.me = me;
    }

    public int getCasting() {
        return casting;
    }

    public int getSitState() {
        return sitState;
    }

    public void setCasting(int casting) {
        this.casting = casting;
    }

    public Location getLoc() {
        return loc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CharacterState getState() {
        return state;
    }

    public void setState(CharacterState state) {
        this.state = state;
        synchronized (me) {
            me.notify();
        }
    }

    public void setHp(String hpString) {
        String[] couplet = hpString.split("=");
        String[] currMax = couplet[1].split("/");
        hp = Integer.parseInt(currMax[0]);
        hpmax = Integer.parseInt(currMax[1]);
    }

    private void updateBuffs(String update) {
        final String[] couplet = update.split("=");
        if (couplet.length == 1) {
            buffs = new ArrayList<>();
        } else {
            buffs = Arrays.stream(couplet[1].split(":"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
    }

    private void updateSitState(String update) {
        final String[] couplet = update.split("=");
        sitState = Integer.parseInt(couplet[1]);
    }

    private void updateBuffDuration(String update) {
        final String[] couplet = update.split("=");
        if (couplet.length == 1) {
            buffDuration = new ArrayList<>();
        } else {
            buffDuration = Arrays.stream(couplet[1].split(":"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
    }

    private void updateCasting(String update) {
        final String[] couplet = update.split("=");
        if (couplet.length == 1) {
            casting = -1;
        } else {
            casting = Integer.parseInt(couplet[1]);
        }
    }

    public List<Integer> getBuffs() {
        if(buffs == null){
            synchronized (this){
                try {
                    wait(100);
                    return getBuffs();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffs;
    }

    public List<Integer> getBuffDuration() {
        if(buffDuration == null){
            synchronized (this){
                try {
                    wait(100);
                    return getBuffDuration();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffDuration;
    }

    public double pctHp(){
        return (double) hp / (double) hpmax;
    }

    public double pctMana(){
        return (double) mana / (double) manamax;
    }

    public int missingHp(){
        return hpmax - hp;
    }

    public int missingMana(){
        return manamax - mana;
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
        this.loc = new Location(
            Float.parseFloat(coords[1]),
            Float.parseFloat(coords[0]),
            Float.parseFloat(coords[2]),
            Integer.parseInt(coords[3])
        );
    }

    @Override
    public String toString() {
        return "Status{" +
            "hp=" + hp +
            ", mana=" + mana +
            ", end=" + end +
            ", hpmax=" + hpmax +
            ", endmax=" + endmax +
            ", manamax=" + manamax +
            ", loc=" + loc +
            '}';
    }

    public synchronized void processUpdatePacket(String[] updateParameters) {
        Arrays.stream(updateParameters).forEach(this::parseUpdatePacketParameter);
    }

    private void parseUpdatePacketParameter(String param) {
        final String[] directive = param.split("=");
        switch (directive[0]) {
            case "B":
                updateBuffs(param);
                break;
            case "F":
                break;
            case "C":
                updateCasting(param);
                break;
            case "E":
                setEnd(param);
                break;
            case "X":

                break;
            case "N":

                break;
            case "L":

                break;
            case "H":
                setHp(param);
                break;
            case "M":
                setMana(param);
                break;
            case "W":

                break;
            case "P":

                break;
            case "G":

                break;
            case "S":

                break;
            case "Y":
                updateSitState(param);
                break;
            case "T":

                break;
            case "Z":
                parseZoneParam(param);
                break;
            case "D":
                updateBuffDuration(param);
                break;
            case "@":
                setLocation(param);
                break;
            case "$":

                break;
            case "O":

                break;
            case "A":

                break;
            case "I":

                break;
            case "R":

                break;
            case "Q":

                break;
            default:
                System.out.println("[CHARACTER]\tDid not understand" + directive[0]);
        }
        synchronized (me) {
            me.notify();
        }
    }

    private void parseZoneParam(String param) {
        final String[] directive = param.split("=");
        final String[] zoneSplit = directive[1].split(":");
        final String[] idSplit = zoneSplit[1].split(">");
        if(idSplit.length<2){
            //zone change detected
            synchronized (this){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        setId(Integer.parseInt(idSplit[1]));
    }
}
