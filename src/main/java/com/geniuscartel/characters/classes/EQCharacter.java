package com.geniuscartel.characters.classes;

import com.geniuscartel.characters.CharacterState;
import com.geniuscartel.characters.Command;
import com.geniuscartel.characters.Stats;
import com.geniuscartel.characters.services.CharacterSaveService;
import com.geniuscartel.workers.characterworkers.CharacterManager;
import com.geniuscartel.workers.ioworkers.AsyncHook;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class EQCharacter implements Runnable, AsyncHook {
    private List<Integer> buffs;
    private Stats stats;
    private int casting = 0, id = -1;
    private final String name;
    private boolean running = true;
    private CharacterManager boss;
    private List<String> neededBuffs = null;
    private List<String> availableBuffs = null;
    private List<String> selfBuffs = null;
    private CharacterState STATE = CharacterState.REST;
    private final ArrayDeque<Command> actionQueue = new ArrayDeque<>();
    private CharacterSaveService characterSaveService;
    private final ConcurrentHashMap<Integer, Future> pendingFutures = new ConcurrentHashMap<>();

    public List<Integer> getBuffs() {
        return buffs;
    }

    @Override
    public void acceptFuture(Callable future, ExecutorService IO_THREAD) {
        System.out.println("accepting future for "+future.toString());
        pendingFutures.put(future.hashCode(), IO_THREAD.submit(future));
    }

    public void submitCommand(Command request) {
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

    public EQCharacter(String name, String[] NBPacket, CharacterManager boss) {
        this.name = name;
        this.stats = new Stats();
        this.boss = boss;
        updateStats(NBPacket);
        this.characterSaveService = new CharacterSaveService(boss.getAsync());
//        neededBuffs = populateNeededBuffs();
//        selfBuffs = populateSelfBuffs();
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
        } else {
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

    public String queryForInfo(String query) {
        String answer = "";
        int hash = boss.getAsync().submitRequest(name, query, this);
        while (!pendingFutures.get(hash).isDone()) {
            processFutureQueue();
        }
        try {
            answer = pendingFutures.get(hash).get().toString();
                System.out.println("got query answer: " + answer);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return answer;
    }


    public void cast(String spellID) {
        boss.submitCommand(name, String.format("//casting \"%s\"", spellID));
        try {
            while (casting == -1) {
                this.wait();
            }
            while (casting > -1) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<String> populateNeededBuffs() {
        return characterSaveService.getMyNeededBuffs(this);
    }

    private List<String> populateSelfBuffs() {
        return characterSaveService.getMySelfBuffs(this);
    }

    private List<String> populateAvailableBuffs() {
        return characterSaveService.getAvailableBuffs(this);
    }

    public List<String> getNeededBuffs() {
        if (neededBuffs == null) {
            neededBuffs = populateNeededBuffs();
        }
        return neededBuffs;
    }

    public List<String> getAvailableBuffs() {
        if (availableBuffs == null) {
            availableBuffs = populateAvailableBuffs();
            System.out.println(availableBuffs);
        }
        if (availableBuffs.size() > 0) {
            System.out.println(String.format("[=============================================]\t%s %s", getName(), availableBuffs));
        }
        return availableBuffs;
    }

    public List<String> getSelfBuffs() {
        if (selfBuffs == null) {
            selfBuffs = populateSelfBuffs();
        }
        return selfBuffs;
    }

    private void processFutureQueue() {
        if (pendingFutures.size() < 1){
            try{
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        synchronized (pendingFutures) {
            pendingFutures.entrySet().stream().filter(x -> x.getValue().isDone()).forEach(x -> {
                synchronized (x) {
                    x.notify();
                }
            });
        }

    }

    @Override
    public void run() {
        while (running) {
            processFutureQueue();
            processActionQueue();
            switch (STATE) {
                case REST:
                    checkBuffs();
//                    benchMark();
                    try {
                        synchronized (this) {
                            this.wait(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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

    private void processActionQueue() {
        while (actionQueue.size() > 0) {
            Command req = actionQueue.removeFirst();
            if (req.getSTATE() == STATE) {
                System.out.println("executing " + req.getClass());
                req.apply();
            }
        }
    }

    @Override
    public String toString() {
        return "EQCharacter{" +
            "buffs=" + buffs +
            ", " + stats +
            ", casting=" + casting +
            ", id=" + id +
            ", name='" + name + '\'' +
            '}';
    }

    public int queueDepth() {
        return this.actionQueue.size();
    }

    private void checkBuffs() {
        HashMap<String, Integer> durationMap = durationMap();
        durationMap.entrySet().stream().filter(x -> x.getValue() < 20).forEach(x -> {
            boolean needToWait = boss.requestBuff(this, x.getKey());
//            if (needToWait) {
//                try{
//                    synchronized (this){
//                        this.wait(1000);
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        });
    }

    private HashMap<String, Integer> durationMap() {
        HashMap<String, Integer> duration = new HashMap<>();
        String buffDurationString = getNeededBuffs().stream().map(nameToDuration).collect(Collectors.joining());
        String response = this.queryForInfo(buffDurationString);
        List<String> results = Arrays.asList(response.split("\\|"));
        results.forEach(x -> {
            String[] temp = x.split(":");
            if (temp.length > 1) {
                int dur = temp[1].equals("NULL") ? -1 : Integer.parseInt(temp[1]);
                duration.put(temp[0], dur);
            }
        });
        return duration;
    }

    Function<String, String> nameToDuration = x -> String.format("%s:${Me.Buff[%s].Duration}|", x, x);

    private void benchMark() {
        System.out.println("Starting benchmark for " + name);
        long start = System.currentTimeMillis();
        int testCount = 100;
        AtomicInteger nullCount = new AtomicInteger(0);
        IntStream.range(0, testCount).forEach(x -> {
            if (queryForInfo("${Me.Buff[Talisman of Fortitude]}").equals("NULL")) {
                nullCount.getAndIncrement();
            }
        });
        long duration = (System.currentTimeMillis() - start);
        String report = String.format("Name: %s\r\n" +
                "time:%d\r\n" +
                "%d Commands (%f/sec %f error rate)", name, duration, testCount,
            ((double) testCount / ((double) duration / 1000.0)), ((double) nullCount.get() / (double) testCount));
        System.out.println(report);
    }

    public void slashCommand(String command) {
        boss.submitCommand(name, "/" + command);
    }

    abstract void restStateAction();

    abstract void followStateAction();

    abstract void combatStateAction();

}
