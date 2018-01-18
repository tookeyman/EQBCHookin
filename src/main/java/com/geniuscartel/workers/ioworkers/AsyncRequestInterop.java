package com.geniuscartel.workers.ioworkers;

import com.geniuscartel.characters.classes.EQCharacter;
import com.geniuscartel.workers.characterworkers.CharacterRequest;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class AsyncRequestInterop {
    private final TreeMap<Integer, CharacterRequest> requests = new TreeMap<>();
    private final ConcurrentHashMap<Integer, EQCharacter> pending = new ConcurrentHashMap<>();
    private final HashMap<Integer, String> resultMap = new HashMap<>();
    private ExecutorService threadPool;
    private OutputWorker output;

    public AsyncRequestInterop(ExecutorService threadPool, OutputWorker output) {
        this.threadPool = threadPool;
        this.output = output;
    }

    public CharacterRequest submitAsyncQuery(EQCharacter c, String command){
        CharacterRequest pendingRequest = new CharacterRequest(assignRequestNumber(), resultMap, this);
        requests.put(pendingRequest.getId(), pendingRequest);
        pending.put(pendingRequest.getId(), c);
        String constructedCommand = String.format("//bct Orchestrator ASYNC:%d::%s", pendingRequest.getId(), command);
//        System.out.println("[ASYNC]\tIssuing: " + character + " -> " + constructedCommand);
        output.sendCommandTo(c.getName(), constructedCommand);
        return pendingRequest;
    }

    synchronized public void updateResultMap(int reference, String value) {
        CharacterRequest pending;
        resultMap.put(reference, value);
        pending = requests.get(reference);
        if(pending != null) {
            synchronized (pending) {
//                System.out.println("[ASYNC]\tRESPONSE: " + reference + ": " + value + "");
                pending.notify();
            }
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

    public void oneWayCommand(String character, String command){
        long start = System.currentTimeMillis();
        rateLimit(start);
        output.sendCommandTo(character, command);
    }

    public String synchronousInformation(String characterName, String command) throws ExecutionException, InterruptedException {
        CharacterRequest pendingRequest = new CharacterRequest(assignRequestNumber(), resultMap, this);
        requests.put(pendingRequest.getId(), pendingRequest);
        String constructedCommand = String.format("//bct Orchestrator ASYNC:%d::%s", pendingRequest.getId(), command);
//        System.out.println("[ASYNC]\tIssuing: " + character + " -> " + constructedCommand);
        output.sendCommandTo(characterName, constructedCommand);
        Future result = threadPool.submit(pendingRequest);
        while(!result.isDone()){
            synchronized (this) {
                wait(10);
            }

        }
        return result.get().toString();
    }

    private void rateLimit(long start){
        double rateLimit = 1000.0/10.0;
        long duration = System.currentTimeMillis() - start;
        long delT = (long)Math.floor(rateLimit) - duration;
        while ((System.currentTimeMillis() - start) < delT) {
            try {
                synchronized (this) {
                    this.wait(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int assignRequestNumber(){
        if(requests.size() == 0) return 0;
        return IntStream.range(0, requests.size()+1).filter(isInUse).findFirst().orElse(-1);
    }

    public void releaseRequest(int key){
        requests.remove(key);
        if (pending.contains(key)) {
            pending.remove(key).notify();
        }
    }

    private IntPredicate isInUse = x -> !requests.containsKey(x);
}
