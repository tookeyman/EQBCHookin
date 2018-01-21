package com.geniuscartel;

import com.geniuscartel.workers.characterworkers.CharacterManager;
import com.geniuscartel.workers.ioworkers.EQCharacterInterface;
import com.geniuscartel.workers.ioworkers.OutputWorker;
import com.geniuscartel.workers.ioworkers.RequestWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EQBCClient {
    private Socket s;
    private OutputStream socketOut;
    private BufferedReader socketIn;

    private boolean running = true;

    private RequestWorker requestWorker;
    private OutputWorker outputWorker;
    private EQCharacterInterface async;

    private final static ExecutorService IO_THREADS = Executors.newCachedThreadPool();
    private CharacterManager characters;

    public EQBCClient() {
        try {
            this.s = new Socket("localhost", 2112);
            this.socketOut = s.getOutputStream();
            this.socketIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            System.out.println("[CLIENT]\tCould not createBuffCommand socket...");
        }
        outputWorker = new OutputWorker(socketOut);
        async = new EQCharacterInterface(IO_THREADS, outputWorker);
        characters = new CharacterManager(IO_THREADS, async);
        requestWorker = new RequestWorker(characters);


        requestWorker.setOutputWorker(outputWorker);
        requestWorker.setAsync(async);
        outputWorker.setRequestWorker(requestWorker);

        IO_THREADS.execute(outputWorker);
        IO_THREADS.execute(requestWorker);
        IO_THREADS.execute(()->characters.initializeSaveService());
    }

    public void shutDownWorkers(){
        //todo createBuffCommand a mechanism for shutting down the orchestrator from a character
        outputWorker.setRunning(false);
        requestWorker.setRunning(false);
        outputWorker.notifyQue();
        requestWorker.notifyQue();
        IO_THREADS.shutdown();
        try {
            IO_THREADS.awaitTermination(1, TimeUnit.MINUTES);
            s.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        System.out.println("[CLIENT]\tWorkers shut down");
    }

    public void run() {
        outputWorker.login();
        boolean triggered = false;
        while (running) {
            String currentRequest = null;
            if(!triggered){
                //test methods go here
                triggered = true;
            }
            try {
                currentRequest = socketIn.readLine();
                requestWorker.addToQue(currentRequest);
            } catch (IOException e) {
                System.out.println("[CLIENT]\tTried to pass a fucked up string to request worker: " + currentRequest);
            }
            requestWorker.notifyQue();
        }
    }

    public void setRunning(boolean t){
        this.running = t;
    }
}
