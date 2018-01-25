package com.geniuscartel;

import java.util.List;

public class App {
    public final static boolean verbose = false;
    public final static String Folder_Location = "D:\\MQ2\\Orchestrator";
    public static List<String> ActiveCharacters = null;

    public static void main( String[] args ) throws InterruptedException {
        EQBCClient client = new EQBCClient();
        Thread.sleep(1000);
        client.run();
        client.shutDownWorkers();
    }

    public static List<String> getActiveCharacters(){
        return ActiveCharacters;
    }

    public static void updateActiveCharacters(List<String> list){
        ActiveCharacters = list;
    }

    public static void debug(Object bug){ if (verbose) System.out.printf("[DEBUG]\t%s\r\n", bug); }
}
