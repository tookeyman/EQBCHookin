package com.geniuscartel;

import java.util.List;
/**
 * Entry point for the client
 * **/

public class App {
    //set verbose to true for crazy, per-packet logging
    public final static boolean verbose = false;
    //base directory for where to put files, stuff like that
    public final static String Folder_Location = "D:\\MQ2\\Orchestrator";
    //list of currently connected characters' names... we need this for random bootstrapping info
    public static List<String> ActiveCharacters = null;

    public static void main( String[] args ) throws InterruptedException {
        //create the client
        EQBCClient client = new EQBCClient();
        //pause for a second to let a few packets show up
        Thread.sleep(1000);
        //begin client
        client.run();
        client.shutDownWorkers();
    }

    public static List<String> getActiveCharacters(){
        //returns a list containing strings of all the char names
        return ActiveCharacters;
    }

    public static void updateActiveCharacters(List<String> list){
        //updates the active character list
        ActiveCharacters = list;
    }

    //static debugging method
    public static void debug(Object bug){ if (verbose) System.out.printf("[DEBUG]\t%s\r\n", bug); }
}
