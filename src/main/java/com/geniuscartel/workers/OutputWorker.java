package com.geniuscartel.workers;

import java.io.IOException;
import java.io.OutputStream;

public class OutputWorker extends DequeWorker {
    private RequestWorker requestWorker;

    public void setRequestWorker(RequestWorker requestWorker) {
        this.requestWorker = requestWorker;
    }

    OutputStream socketOut;

    public OutputWorker(OutputStream socketOut) {
        this.socketOut = socketOut;
    }

    @Override
    void doWorkerTask(Object item) {
        try {
            System.out.printf("[RESPONSE]%s", item);
            socketOut.write(((String)item).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendNamePacket() {
        sendPackets(MsgType.CMD_NAMES.getMessage());
    }

    @Deprecated
    public void sendChannelPacket(){
        sendPackets(MsgType.CMD_CHANNELS.getMessage());
    }

    public void sendDisconnectPacket(){
        sendPackets(MsgType.CMD_DISCONNECT.getMessage());
    }

    @Deprecated
    public void sendBCIPacket(String input) {
        String command = sanitizeCommand(input);
        sendPackets(MsgType.CMD_BCI.getMessage(), command);
    }

    @Deprecated
    public void sendLocalEchoPacket(String echo) {
        String command = sanitizeCommand(echo);
        sendPackets(MsgType.CMD_LOCALECHO.getMessage(), command);
    }

    public void sendCommandTo(String character, String input){
        String command = sanitizeCommand(input);
        sendPackets(MsgType.CMD_TELL.getMessage() + character + " " + command);
    }

    public void sendCommandToAll(String input){
        String command = sanitizeCommand(input);
        sendPackets(MsgType.CMD_MSGALL.getMessage(), command);
    }

    private String sanitizeCommand(String input){
        String command = input;
        if(!command.substring(command.length()-1, command.length()).equals("\n"))
            command += "\n";

        return command;
    }

    public void login() {
        System.out.println("Sending login packet...");
        String login = "LOGIN=Orchestrator;\tLOCALECHO 1\tNBMSGECHO 1\n";
        sendPackets(login);
    }

    private void sendPackets(String...packets){
        for(String packet : packets) {
            this.addToQue(packet);
        }
        this.notifyQue();
    }

    public void pong(){
        sendPackets(MsgType.CMD_PONG.getMessage());
    }

    @Override
    public void run(){
        System.out.println("Starting OutputWorker");
        super.run();
    }
}
