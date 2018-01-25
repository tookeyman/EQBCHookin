package com.geniuscartel.characters;

import com.geniuscartel.characters.movement.MovementManager;
import com.geniuscartel.characters.services.SaveService;
import com.geniuscartel.workers.characterworkers.CharacterInfoQuery;
import com.geniuscartel.workers.characterworkers.CharacterManager;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.geniuscartel.characters.CharacterState.ANY;

public abstract class EQCharacter implements Runnable {
    private Status status;
    private final String name;
    private boolean characterRunning = true;

    private final SaveService saveService;
    private final CharacterManager characterManager;
    private final BuffManager buffManager;
    private final ActionManager actionManager;
    private final MovementManager movementManager;
    private final ArrayDeque<Command> actionQueue = new ArrayDeque<>();

    public EQCharacter(String name, String[] NBPacket, CharacterManager characterManager) {
        this.name = name;
        this.status = new Status(this);
        this.characterManager = characterManager;
        this.saveService = new SaveService(characterManager.getAsync());
        this.buffManager = new BuffManager(this, this.saveService);
        this.status.processUpdatePacket(NBPacket);
        this.actionManager = new ActionManager(this);
        this.movementManager = new MovementManager(this);
    }

    public String getName() {
        return this.name;
    }

    protected CharacterManager getCharacterManager(){
        return this.characterManager;
    }

    protected SaveService getSaveService(){
        return this.saveService;
    }

    public BuffManager getBuffManager(){
        return this.buffManager;
    }

    public Status getStatus() {
        return this.status;
    }

    public MovementManager getMovementManager() {
        return movementManager;
    }

    public int getActionQueueDepth() {
        return this.actionQueue.size();
    }

    public boolean isCharacterRunning() {
        return this.characterRunning;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public void setCharacterRunning(boolean characterRunning) {
        this.characterRunning = characterRunning;
    }

    public void enqueCommand(Command request) {
        this.actionQueue.add(request);
    }

    public String queryForInfo(String query) {
        String answer = "";
        final CharacterInfoQuery pending = characterManager.getAsync().submitAsyncQuery(this, query);
        try {
            answer = pending.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return answer;
    }

    @Override
    public void run() {
        while (characterRunning) {
            processActionQueue();
            switch (status.getState()) {
                case REST:
                    buffManager.checkForExpiredBuffs();
//                    benchMark();
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
            final Command req = actionQueue.removeFirst();
            if (req.getSTATE() == ANY || req.getSTATE() == status.getState()) {
                System.out.println("["+name+"]\tExecuting " + req.getClass());
                req.apply();
            }else{
                System.out.println("states don't match, discarding stupid fucking action");
            }
        }
    }

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

    public void rawSlashCommand(String command) {
        characterManager.submitCommand(name, "/" + command);
    }

    public abstract void restStateAction();

    public abstract void followStateAction();

    public abstract void combatStateAction();

}
