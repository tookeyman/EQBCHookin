package com.geniuscartel.characters.classes;

import com.geniuscartel.characters.ActionRequest;
import com.geniuscartel.characters.CharacterState;
import com.geniuscartel.characters.Stats;
import com.geniuscartel.workers.characterworkers.CharacterManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Character implements Runnable{
    private List<Integer> buffs;
    private Stats stats;
    private int casting = 0, id =-1;
    private final String name;
    private boolean running = true;
    private CharacterManager boss;
    private List<Integer> neededBuffs;
    private List<Integer> selfBuffs;
    private CharacterState STATE = CharacterState.REST;
    private final ArrayDeque<ActionRequest> actionQueue = new ArrayDeque<>();

    public List<Integer> getBuffs() {
        return buffs;
    }

    public void submitAction(ActionRequest request) {
        this.actionQueue.add(request);
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

    public CharacterState getSTATE() {
        return STATE;
    }

    public void setSTATE(CharacterState STATE) {
        //todo action cleanup between state changes
        this.STATE = STATE;
        synchronized (this) {
            this.notify();
        }
    }

    public Character(String name, String[] NBPacket, CharacterManager boss) {
        this.name = name;
        this.stats = new Stats();
        this.boss = boss;
        updateStats(NBPacket);
        neededBuffs = populateNeededBuffs();
        selfBuffs = populateSelfBuffs();
    }

    public synchronized void updateStats(String[] updateParameters) {
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
                System.out.println("[CHARACTER]\tDid not understand" + directive[0]);
        }
        this.notify();
    }

    private void updateBuffs(String update) {
        String[] couplet = update.split("=");
        if (couplet.length == 1) {
            buffs = new ArrayList<>();
        }else{
            buffs = Arrays.stream(couplet[1].split(":"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
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

    public String queryForInfo(String query){
        String answer = "";
        Future<String> result = boss.getAsync().submitRequest(name, query);
        while(!result.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            answer = result.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return answer;
    }

    public void cast(int spellID){
        boss.submitCommand(name, String.format("//casting %d", spellID));
        try {
            while (casting == -1) {
                this.wait();
            }
            while(casting == spellID) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<Integer> populateNeededBuffs(){
        String buffs = "9184|9211";
        return Arrays.stream(buffs.split("\\|")).map(Integer::parseInt).collect(Collectors.toList());
    }

    private List<Integer> populateSelfBuffs(){
        String selfBuffs = "9011|5327";
        return Arrays.stream(selfBuffs.split("\\|")).map(Integer::parseInt).collect(Collectors.toList());
    }


    public List<Integer> getNeededBuffs() {
        return neededBuffs;
    }

    public List<Integer> getSelfBuffs() {
        return selfBuffs;
    }

    @Override
    public void run(){
        while (running) {
            processActionQueue();
            switch (STATE) {
                case REST:
                    checkBuffs();
                    restStateAction();
                    break;
                case COMBAT:
                    combatStateAction();
                    break;
                case FOLLOWING:
                    followStateAction();
                    break;
            }
        }
    }

    private void processActionQueue(){
        while(actionQueue.size()>0){
            ActionRequest req = actionQueue.removeFirst();
            if (req.getSTATE() == STATE) {
                req.run();
            }
        }
    }

    @Override
    public String toString() {
        return "Character{" +
            "buffs=" + buffs +
            ", stats=" + stats +
            ", casting=" + casting +
            ", id=" + id +
            ", name='" + name + '\'' +
            '}';
    }
    private void checkBuffs(){
        selfBuffs.stream().filter(x -> !buffs.contains(x)).forEach(selfBuff);
    }

    private Consumer<Integer> selfBuff = x->{
        ActionRequest selfBuff = new ActionRequest(CharacterState.REST) {
            @Override
            public void run() {
                cast(x);
            }
        };
        submitAction(selfBuff);
    };

    abstract void restStateAction();
    abstract void followStateAction();
    abstract void combatStateAction();

}
