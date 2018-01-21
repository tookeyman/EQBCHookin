package com.geniuscartel.workers.ioworkers;

import com.geniuscartel.characters.EQCharacter;
import com.geniuscartel.workers.characterworkers.CharacterInfoQuery;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class EQCharacterInterface {
    private final TreeMap<Integer, CharacterInfoQuery> requests = new TreeMap<>();
    private final ConcurrentHashMap<Integer, EQCharacter> pending = new ConcurrentHashMap<>();
    private final HashMap<Integer, String> resultMap = new HashMap<>();
    private ExecutorService threadPool;
    private OutputWorker output;

    public EQCharacterInterface(ExecutorService threadPool, OutputWorker output) {
        this.threadPool = threadPool;
        this.output = output;
    }

    public CharacterInfoQuery submitAsyncQuery(EQCharacter c, String command){
        CharacterInfoQuery pendingRequest = new CharacterInfoQuery(assignRequestNumber(), resultMap, this);
        requests.put(pendingRequest.getId(), pendingRequest);
        pending.put(pendingRequest.getId(), c);
        String constructedCommand = String.format("//squelch /bct Orchestrator ASYNC:%d::%s", pendingRequest.getId(), command);
//        System.out.println("[ASYNC]\tIssuing: " + character + " -> " + constructedCommand);
        output.sendCommandTo(c.getName(), constructedCommand);
        return pendingRequest;
    }

    public void oneWayCommand(String character, String command){
        output.sendCommandTo(character, command);
    }

    public String submitSynchronousQuery(String characterName, String command) throws ExecutionException, InterruptedException {
        CharacterInfoQuery pendingRequest = new CharacterInfoQuery(assignRequestNumber(), resultMap, this);
        requests.put(pendingRequest.getId(), pendingRequest);
        String constructedCommand = String.format("//squelch /bct Orchestrator ASYNC:%d::%s", pendingRequest.getId(), command);
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

    public void releaseRequest(int key){
        requests.remove(key);
        if (pending.contains(key)) {
            pending.remove(key).notify();
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

    private synchronized void updateResultMap(int reference, String value) {
        CharacterInfoQuery pending;
        resultMap.put(reference, value);
        pending = requests.get(reference);
        if(pending != null) {
            synchronized (pending) {
//                System.out.println("[ASYNC]\tRESPONSE: " + reference + ": " + value + "");
                pending.notify();
            }
        }
    }

    private String removeCharacterResponseHeader(String request) {
        for(int i = 0; i < request.length();i++) {
            if(request.substring(i, i+2).equals("] ")){
                return request.substring(i + 2, request.length());
            }
        }
        return null;
    }

    private int assignRequestNumber(){
        if(requests.size() == 0) return 0;
        return IntStream.range(0, requests.size()+1).filter(isInUse).findFirst().orElse(-1);
    }

    private IntPredicate isInUse = x -> !requests.containsKey(x);
}
