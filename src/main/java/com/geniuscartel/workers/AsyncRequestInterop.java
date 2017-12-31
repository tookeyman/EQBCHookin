package com.geniuscartel.workers;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class AsyncRequestInterop {
    private final TreeMap<Integer, CharacterRequest> requests = new TreeMap<>();
    private final HashMap<Integer, String> resultMap = new HashMap<>();
    private ExecutorService threadPool;
    private OutputWorker output;

    public AsyncRequestInterop(ExecutorService threadPool, OutputWorker output) {
        this.threadPool = threadPool;
        this.output = output;
    }

    synchronized public void updateResultMap(int reference, String value) {
        CharacterRequest pending;
        resultMap.put(reference, value);
        pending = requests.get(reference);
        synchronized (pending){
            pending.notify();
        }
    }

    public void handleReturnedRequest(String request) {
        if (request.length() <= 6) {
            throw new Error("Returned Async Request was malformed");
        }
        String refinedRequest = removeCharacterResponseHeader(request);
        assert refinedRequest != null;
        String[] payload = refinedRequest.substring(6, refinedRequest.length()).split("::");
        int reference = Integer.parseInt(payload[0]);
        updateResultMap(reference, payload[1]);
    }

    private String removeCharacterResponseHeader(String request) {
        for(int i = 0; i < request.length();i++) {
            if(request.substring(i, i+2).equals("] ")){
                return request.substring(i + 2, request.length());
            }
        }
        return null;
    }

    public Future submitRequest(String character, String command){
        //todo this has to talk to the mq scripts. right now it's fucking awful
        CharacterRequest pendingRequest = new CharacterRequest(assignRequestNumber(), resultMap, this);
        requests.put(pendingRequest.getId(), pendingRequest);
        String constructedCommand = String.format("//bct Orchestrator ASYNC:%d::%s", pendingRequest.getId(), command);
        output.sendCommandTo(character, constructedCommand);
        return threadPool.submit(pendingRequest);
    }

    private int assignRequestNumber(){
        if(requests.size() == 0) return 0;

        return IntStream.iterate(0, i -> i + 1).limit(requests.size()+1).filter(isInUse).findFirst().getAsInt();
    }

    public void releaseRequest(int key){
        requests.remove(key);
    }

    private IntPredicate isInUse = x -> !requests.containsKey(x);
}
