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
    private EQCharacterInterface characterInterface;

    //threadpool init
    private final static ExecutorService threadPool = Executors.newCachedThreadPool();
    private CharacterManager characters;


    /*
    todo method/mechanism to stop everything gracefully
     */
    public EQBCClient() {
        try {
            //init the socket and register the in/out streams for the workers
            this.s = new Socket("localhost", 2112);
            this.socketOut = s.getOutputStream();
            this.socketIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            System.out.println("[CLIENT]\tCould not createBuffCommand socket...");
        }
        //output worker handles all the commands that get sent to the eqbcserver
        outputWorker = new OutputWorker(socketOut);
        //character interface handles all the async communication with individual characters
        characterInterface = new EQCharacterInterface(threadPool, outputWorker);
        //character manager supervises and tracks all the registered characters
        characters = new CharacterManager(threadPool, characterInterface);
        //request worker handles all the incoming eqbc and netbot packets and notifies the related character
        requestWorker = new RequestWorker(characters);

        //register a few components
        requestWorker.setOutputWorker(outputWorker);
        requestWorker.setAsync(characterInterface);
        outputWorker.setRequestWorker(requestWorker);

        //start the workers in their own threads
        threadPool.execute(outputWorker);
        threadPool.execute(requestWorker);
        //runs the save service initialization later, asynchronously
        threadPool.execute(() -> characters.initializeSaveService());
    }

    public void shutDownWorkers(){//attempts to shut down the client gracefully
        outputWorker.setRunning(false);
        requestWorker.setRunning(false);
        outputWorker.notifyQue();
        requestWorker.notifyQue();
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.MINUTES);
            s.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        System.out.println("[CLIENT]\tWorkers shut down");
    }

    public void run() {//main loop for the client
        //log the orchestrator into the eqbc server
        outputWorker.login();
        boolean triggered = false;//debugging switch, runs a high-level method once
        while (running) {
            String currentRequest = null;
            if(!triggered){
                triggered = true;
                //test methods go here
            }
            try {
                //reads requests off the socket
                currentRequest = socketIn.readLine();
                //delegate processing by adding the request to the associated worker queue
                requestWorker.addToQue(currentRequest);
            } catch (IOException e) {
                System.out.println("[CLIENT]\tTried to pass a fucked up string to request worker: " + currentRequest);
            }
            //wake up worker to do work
            requestWorker.notifyQue();
        }
    }

    public void setRunning(boolean t){
        this.running = t;
    }
}
