package com.geniuscartel.Toon.classes;

import com.geniuscartel.Toon.Stats;
import com.geniuscartel.workers.CharacterManager;

import java.util.Arrays;
import java.util.stream.IntStream;

public abstract class Character implements Runnable{
    private int[] buffs;
    private Stats stats;
    private int casting = 0, id =-1;
    private final String name;
    private boolean running = true;
    private CharacterManager boss;

    public int[] getBuffs() {
        return buffs;
    }

    public Stats getStats() {
        return stats;
    }

    public int getCasting() {
        return casting;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public void sendCommand(String character, String command) {
        boss.submitCommand(character, command);
    }

    public Character(String name, String[] NBPacket, CharacterManager boss) {
        this.name = name;
        this.stats = new Stats();
        updateState(NBPacket);
    }

    public synchronized void updateState(String[] updateParameters) {
        Arrays.stream(updateParameters).forEach(this::parseUpdate);
    }

    private void parseUpdate(String param) {
        String[] directive = param.split("=");
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
                stats.setEnd(param);
                break;
            case "X":

                break;
            case "N":

                break;
            case "L":

                break;
            case "H":
                stats.setHp(param);
                break;
            case "M":
                stats.setMana(param);
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

                break;
            case "T":

                break;
            case "Z":

                break;
            case "D":

                break;
            case "@":
                stats.setLocation(param);
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
                System.out.println("Did not understand" + directive[0]);
        }
        this.notify();
    }

    private void updateBuffs(String update) {
        String[] couplet = update.split("=");
        if (couplet.length == 1) {
            buffs = new int[0];
        }else{
            String[] stringbuffs = couplet[1].split(":");
            int nbuff = stringbuffs.length;
            buffs = new int[nbuff];
            IntStream.iterate(0, i->i+1).limit(nbuff)
                .boxed().forEach(x->buffs[x] = Integer.parseInt(stringbuffs[x]));
        }
    }


    private void updateCasting(String update) {
        String[] couplet = update.split("=");
        if (couplet.length == 1) {
            casting = -1;
        } else {
            casting = Integer.parseInt(couplet[1]);
        }
    }


    @Override
    public String toString() {
        return "Character{" +
            "buffs=" + Arrays.toString(buffs) +
            ", stats=" + stats +
            ", casting=" + casting +
            ", id=" + id +
            ", name='" + name + '\'' +
            '}';
    }

}
