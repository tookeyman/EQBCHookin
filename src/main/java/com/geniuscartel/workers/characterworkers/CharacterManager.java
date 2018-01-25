package com.geniuscartel.workers.characterworkers;

import com.geniuscartel.App;
import com.geniuscartel.characters.CharacterState;
import com.geniuscartel.characters.EQCharacter;
import com.geniuscartel.characters.ShortClass;
import com.geniuscartel.characters.classes.*;
import com.geniuscartel.characters.services.BuffService;
import com.geniuscartel.characters.services.SaveService;
import com.geniuscartel.workers.ioworkers.EQCharacterInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CharacterManager {
    private final HashMap<String, EQCharacter> characters;
    private ExecutorService IOThreads;
    private EQCharacterInterface async;
    private BuffService buffs;
    private boolean creationFlush = false;
    private EventQue creationEvent = new EventQue();

    public CharacterManager(ExecutorService IOTHREADS, EQCharacterInterface async) {
        this.IOThreads = IOTHREADS;
        this.async = async;
        characters = new HashMap<>();
        buffs = new BuffService();

    }

    public void requestBuff(EQCharacter c, String buff) throws Error{
        if (buffs == null) {
            synchronized (this){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!buffs.checkIfCharactersChanged(characters.entrySet().size())) {
            buffs.updateBuffers(characters.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        }

        buffs.requestBuff(c, buff);
    }

    public EQCharacter getCharacterByName(String name) {
        return characters.get(name);
    }

    public void setGlobalState(CharacterState cs){
        characters.entrySet().stream().forEach(x->x.getValue().getStatus().setState(cs));
    }

    public boolean exists(String charname){
        return characters.containsKey(charname);
    }

    public EQCharacter get(String key){
        return characters.get(key);
    }

    public void submitCommand(String character, String command) {
        async.oneWayCommand(character, command);
    }

    public EQCharacterInterface getAsync() {
        return async;
    }

    public void submitRequest(String request) {
        Matcher managerRequest = Pattern.compile("^\\[\\w+] MANAGER: (.*)$").matcher(request);
        if(managerRequest.find()){
            String command = managerRequest.group(1);
            processRequest(command);
        }
    }

    private void processRequest(String command){
        String[] comSplit = command.split("\\s");
        if (comSplit.length == 1) {
            return;
        }
        switch(comSplit[0]){
            case "STATE":
                setGlobalState(CharacterState.valueOf(comSplit[1]));
        }
    }

    public void create(String key, String[] value){
        Runnable r = createNewCharacter(key, value);
        creationEvent.addCommand(r);
        if (creationFlush) {
            IOThreads.execute(creationEvent);
        } else {
            if (creationEvent.queueDepth() == App.ActiveCharacters.size()) {
                creationEvent.addCommand(()->this.buffs = new BuffService());
                IOThreads.execute(creationEvent);
                creationFlush = true;
            }
        }
    }

    private EQCharacter getCharacterConstructorFromEnum(ShortClass shrt, String key, String[] value){
        EQCharacter cha = null;
        switch (shrt) {
            case BER:
                cha = new Berserker(key, value, this);
                break;
            case BRD:
                cha = new Bard(key, value, this);
                break;
            case BST:
                cha = new Beastlord(key, value, this);
                break;
            case CLR:
                cha = new Cleric(key, value, this);
                break;
            case DRU:
                cha = new Druid(key, value, this);
                break;
            case ENC:
                cha = new Enchanter(key, value, this);
                break;
            case MAG:
                cha = new Magician(key, value, this);
                break;
            case MNK:
                cha = new Monk(key, value, this);
                break;
            case NEC:
                cha = new Necromancer(key, value, this);
                break;
            case PAL:
                cha = new Paladin(key, value, this);
                break;
            case RNG:
                cha = new Ranger(key, value, this);
                break;
            case ROG:
                cha = new Rogue(key, value, this);
                break;
            case SHD:
                cha = new ShadowKnight(key, value, this);
                break;
            case SHM:
                cha = new Shaman(key, value, this);
                break;
            case WAR:
                cha = new Warrior(key, value, this);
                break;
            case WIZ:
                cha = new Wizard(key, value, this);
                break;
        }
        return cha;
    }

    private Runnable createNewCharacter(String key, String[] value){
        return ()-> {
            EQCharacter cha = null;
            try {
                String className = async.submitSynchronousQuery(key, "${Me.Class.ShortName}");
                ShortClass shrt = ShortClass.valueOf(className);
                cha = getCharacterConstructorFromEnum(shrt, key, value);
                if (cha != null) {
                    synchronized (characters) {
                        System.out.println("[CHARACTER MANAGER]\tRegistering: " + key);
                        characters.put(key, cha);
                        IOThreads.execute(cha);
                    }
                }else{
                    //no character
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        };
    }

    public void initializeSaveService() {
        SaveService.initServerName(async);
    }
}

class EventQue implements Runnable{
    List<Runnable> commands = new ArrayList<>();

    public void addCommand(Runnable r){
        this.commands.add(r);
    }

    public int queueDepth(){
        return commands.size();
    }

    @Override
    public void run() {
        while(commands.size()>0){
            commands.remove(0).run();
        }
    }
}