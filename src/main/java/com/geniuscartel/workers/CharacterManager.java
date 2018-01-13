package com.geniuscartel.workers;

import com.geniuscartel.Toon.ShortClass;
import com.geniuscartel.Toon.Toon;
import com.geniuscartel.Toon.classes.ShadowKnight;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CharacterManager {
    private final HashMap<String, Toon> characters;
    private ExecutorService IO_THREADS;
    private AsyncRequestInterop async;

    public CharacterManager(ExecutorService IOTHREADS, AsyncRequestInterop async) {
        this.IO_THREADS = IOTHREADS;
        this.async = async;
        characters = new HashMap<>();
    }

    public boolean exists(String charname){
        return characters.containsKey(charname);
    }

    public Toon get(String key){
        return characters.get(key);
    }

    public void submitCommand(String character, String command) {
        async.oneWayCommand(character, command);
    }

    public void create(String key, String[] value){
        IO_THREADS.execute(createNewCharacter(key, value));
//        characters.put(key, character);
//        IO_THREADS.execute(character);
    }

    private Runnable createNewCharacter(String key, String[] value){
        return ()-> {
            Future className = async.submitRequest(key, "${Me.Class.ShortName}");
            Toon cha = null;
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
                synchronized (characters) {
                    switch (shrt) {
                        case SHD:
                            cha = new ShadowKnight(key, value, this);
                            characters.put(key, cha);
                            break;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (cha != null) {
                IO_THREADS.execute(cha);
            }
        };
    }
}
