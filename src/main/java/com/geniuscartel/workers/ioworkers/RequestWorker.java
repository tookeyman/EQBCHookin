package com.geniuscartel.workers.ioworkers;

import com.geniuscartel.workers.characterworkers.CharacterManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RequestWorker<T> extends DequeWorker {
    private OutputWorker outputWorker;
    private List<String> clientNames = new ArrayList<>();
    private AsyncRequestInterop async;
    private CharacterManager characters;
    private boolean verbose = false;

    public RequestWorker(CharacterManager characters) {
        this.characters = characters;
    }

    public void setOutputWorker(OutputWorker out){
        this.outputWorker = out;
    }

    public void setAsync(AsyncRequestInterop async) {
        this.async = async;
    }

    @Override
    void doWorkerTask(Object item) {
        handleRequest((String)item);
    }

    private void handleRequest(String request){
        if(isNetbotPacket(request)){
            processNetbotPacket(request);
        }
        if(isPing(request)) {
            outputWorker.pong();
            return;
        }
        if(isClientList(request)){
            clientNames = updateClientList(request);
            System.out.println("Updated Client names: " + clientNames);
            return;
        }
        if (isAsyncRequest(request)) {
            async.handleReturnedRequest(request);
        }
        if (verbose) System.out.printf("[REQUEST]%s\r\n", request);
    }

    public List<String> getClientNames() {
        return clientNames;
    }

    private void processNetbotPacket(String request){
        Matcher packetGrabber = Pattern.compile("^\tNBPKT:(\\w+):\\[NB]\\|(.*)\\[NB]$").matcher(request);
        if(packetGrabber.find()){
            String charName = packetGrabber.group(1);
            String updateList[] = packetGrabber.group(2).split("\\|");
            if(characters.exists(charName)){
                characters.get(charName).updateState(updateList);
            }else{
                characters.create(charName, updateList);
            }
        }
    }

    private boolean isAsyncRequest(String request){
        if(!request.substring(0, 1).equals("[")) return false;
        Matcher asyncPattern = Pattern.compile("^\\[\\w+] ASYNC:\\d+::.*$").matcher(request);
        return asyncPattern.find();
    }

    private List<String> updateClientList(String request) {
        String names = request.substring(14, request.length());
        List<String> rawNames = Arrays.asList(names.split(" "));
        return rawNames.stream().filter(nonOrchestratorNames).collect(Collectors.toList());
    }

    private Predicate<String> nonOrchestratorNames = (x) -> !x.equals("Orchestrator");

    private boolean isClientList(String request){
        if(request.length()<14) return false;
        String header = request.substring(0, 14);
        return header.equals("\tNBCLIENTLIST=");
    }

    private boolean isNetbotPacket(String packet){
        if(packet.length()<7)
            return false;
        String header = packet.substring(0, 7);
        return header.equals("\tNBPKT:");
    }

    private boolean isPing(String packet){
        return packet.equals("\tPING");
    }

    @Override
    public void run(){
        System.out.println("[THREAD]\tStarting RequestWorker...");
        super.run();
    }
}
