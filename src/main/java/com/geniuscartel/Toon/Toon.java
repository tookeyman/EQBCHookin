package com.geniuscartel.Toon;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Toon implements Runnable{
    private int[] buffs;
    private Stats stats;
    private ShortClass className;
    private int casting = 0, id;
    private final String name;
    private boolean running = true;

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Toon(String name, String[] NBPacket) {
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
        return "Toon{" +
            "buffs=" + Arrays.toString(buffs) +
            ", stats=" + stats +
            ", className=" + className +
            ", casting=" + casting +
            ", id=" + id +
            ", name='" + name + '\'' +
            '}';
    }

    @Override
    public void run() {
        while (running) {
            synchronized (this){
                try {
                    System.out.println(this.toString());
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
