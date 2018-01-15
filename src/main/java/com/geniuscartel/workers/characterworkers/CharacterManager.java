package com.geniuscartel.workers.characterworkers;

import com.geniuscartel.characters.CharacterState;
import com.geniuscartel.characters.ShortClass;
import com.geniuscartel.characters.classes.*;
import com.geniuscartel.characters.classes.Character;
import com.geniuscartel.workers.ioworkers.AsyncRequestInterop;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterManager {
    private final HashMap<String, Character> characters;
    private ExecutorService IO_THREADS;
    private AsyncRequestInterop async;

    public CharacterManager(ExecutorService IOTHREADS, AsyncRequestInterop async) {
        this.IO_THREADS = IOTHREADS;
        this.async = async;
        characters = new HashMap<>();
    }

    public void setGlobalState(CharacterState cs){
        characters.entrySet().stream().forEach(x->x.getValue().setSTATE(cs));
    }

    public boolean exists(String charname){
        return characters.containsKey(charname);
    }

    public Character get(String key){
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

    private Character getCharacterConstructorFromEnum(ShortClass shrt, String key, String[] value){
        Character cha = null;
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
            Future className = async.submitRequest(key, "${Me.Class.ShortName}");
            Character cha = null;
            while (!className.isDone()) {
                try {
                    synchronized (this) {
                        this.wait(500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                ShortClass shrt = ShortClass.valueOf(className.get().toString());
                cha = getCharacterConstructorFromEnum(shrt, key, value);
                if (cha != null) {
                    synchronized (characters) {
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
