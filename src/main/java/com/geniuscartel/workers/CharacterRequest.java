package com.geniuscartel.workers;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class CharacterRequest implements Callable{
    private final int id;
    private String requestedValue;
    private final HashMap<Integer, String> resultMap;
    AsyncRequestInterop watcher;

    public CharacterRequest(int id, HashMap<Integer, String> resultMap, AsyncRequestInterop watcher) {
        System.out.println("Request created with ID " + id);
        this.id = id;
        this.resultMap = resultMap;
        this.watcher = watcher;
    }

    public int getId() {
        return id;
    }

    @Override
    public String call() throws Exception {
        synchronized (this){
            this.wait();
        }
        requestedValue = resultMap.get(this.id);
        synchronized (resultMap){
            resultMap.remove(this.id);
        }
        watcher.releaseRequest(this.id);
        return requestedValue;
    }
}
