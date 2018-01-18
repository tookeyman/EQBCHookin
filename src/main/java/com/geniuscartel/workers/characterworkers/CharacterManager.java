package com.geniuscartel.workers.characterworkers;

import com.geniuscartel.App;
import com.geniuscartel.characters.CharacterState;
import com.geniuscartel.characters.ShortClass;
import com.geniuscartel.characters.classes.*;
import com.geniuscartel.characters.services.BuffService;
import com.geniuscartel.workers.ioworkers.AsyncRequestInterop;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CharacterManager {
    private final HashMap<String, EQCharacter> characters;
    private ExecutorService IO_THREADS;
    private AsyncRequestInterop async;
    private BuffService buffs;

    public CharacterManager(ExecutorService IOTHREADS, AsyncRequestInterop async) {
        this.IO_THREADS = IOTHREADS;
        this.async = async;
        characters = new HashMap<>();
        buffs = new BuffService();
    }

    public boolean requestBuff(EQCharacter c, String buff) {
        if (!buffs.checkIfCharactersChanged(App.getActiveCharacters().size())) {
            buffs.updateBuffers(characters.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        }
        buffs.printAvailableBuffers();
        return buffs.requestBuff(c, buff);
    }

    public void setGlobalState(CharacterState cs){
        characters.entrySet().stream().forEach(x->x.getValue().setSTATE(cs));
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

    public AsyncRequestInterop getAsync() {
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
        IO_THREADS.execute(createNewCharacter(key, value));
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
                String className = async.synchronousInformation(key, "${Me.Class.ShortName}");
                ShortClass shrt = ShortClass.valueOf(className);
                cha = getCharacterConstructorFromEnum(shrt, key, value);
                if (cha != null) {
                    synchronized (characters) {
                        System.out.println("putting eqcharacter" + key+ " " + cha);
                        characters.put(key, cha);
                        IO_THREADS.execute(cha);
                    }
                }else{
                    //no character
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        };
    }
}
