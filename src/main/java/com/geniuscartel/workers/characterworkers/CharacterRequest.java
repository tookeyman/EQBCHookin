package com.geniuscartel.workers.characterworkers;

import com.geniuscartel.workers.ioworkers.AsyncRequestInterop;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class CharacterRequest implements Callable{
    private final int id;
    private String requestedValue;
    private final HashMap<Integer, String> resultMap;
    private AsyncRequestInterop watcher;

    public CharacterRequest(int id, HashMap<Integer, String> resultMap, AsyncRequestInterop watcher) {
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
