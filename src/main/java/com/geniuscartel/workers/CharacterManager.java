package com.geniuscartel.workers;

import com.geniuscartel.Toon.Toon;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class CharacterManager {
    private HashMap<String, Toon> characters;
    ExecutorService IO_THREADS;

    public CharacterManager(ExecutorService IOTHREADS) {
        this.IO_THREADS = IOTHREADS;
        characters = new HashMap<>();
    }

    public boolean exists(String charname){
        return characters.containsKey(charname);
    }

    public Toon get(String key){
        return characters.get(key);
    }

    public void create(String key, Toon value){
        characters.put(key, value);
        IO_THREADS.execute(value);
    }
}
