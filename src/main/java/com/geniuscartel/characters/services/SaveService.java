package com.geniuscartel.characters.services;

import com.geniuscartel.App;
import com.geniuscartel.characters.EQCharacter;
import com.geniuscartel.workers.ioworkers.EQCharacterInterface;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SaveService {
    private static EQCharacterInterface threads;
    private static String server = null;

    public SaveService(EQCharacterInterface threads) {
        setThreads(threads);
    }

    private static void setThreads(EQCharacterInterface async){
        if(threads == null)
            threads = async;
    }

    private static EQCharacterInterface getThreads(){
        return threads;
    }

    public static void initServerName(EQCharacterInterface thread){
        try {
            while (App.getActiveCharacters() == null || App.getActiveCharacters().size() == 0) {
                Thread.sleep(100);
            }
            String charName = App.getActiveCharacters().get(0);
            server = thread.submitSynchronousQuery(charName, "${MacroQuest.Server}");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getServer(){
        if (server == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getServer();
        }
        return server;
    }

    public HashMap<String, String> getSpellConfig(EQCharacter c, String key){
        return null;
    }

    public List<String> readBuffFile(EQCharacter c, String key){
        File myNeededBuffFile = getFileForKey(c, key);
        try (FileReader fr = new FileReader(myNeededBuffFile)){
            return readUncommentedLines(fr);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private File getFileForKey(EQCharacter c, String key) {
        String server = getServer();
        String name = c.getName();
        checkServerDirectory(server);
        checkCharacterDirectory(server, name);
        File myNeededBuffFile = new File(String.format("%s/%s/%s/%s.txt", App.Folder_Location, server.trim(), name, key));

        if(myNeededBuffFile.exists()){
            return myNeededBuffFile;
        }else{
            File defaultBuffFile = new File(String.format("%s/default-%s.txt", App.Folder_Location, key));
            if(defaultBuffFile.exists()){
                try {
                    Files.copy(defaultBuffFile.toPath(), new FileOutputStream(myNeededBuffFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return myNeededBuffFile;
            }else{
                return createDefaultFile(key);
            }
        }
    }

    private void checkServerDirectory(String server) {
        File dir = new File(String.format("%s/%s", App.Folder_Location, server.trim()));
        if(!dir.exists()){
            try {
                Files.createDirectories(dir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkCharacterDirectory(String server, String character) {
        File dir = new File(String.format("%s/%s/%s", App.Folder_Location, server.trim(), character));
        if(!dir.exists()){
            try {
                Files.createDirectories(dir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createDefaultFile(String key) {
        File dfile = new File(String.format("%s/default-%s.txt", App.Folder_Location, key));
        if(!dfile.exists()){
            try {
                Files.createFile(dfile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dfile;
    }

    private List<String> readUncommentedLines(Reader input){
        return linesOf(input,
            lines -> lines.filter(line -> !line.startsWith("//")).collect(Collectors.toList()),
            BuffReaderException);
    }

    private <T> T linesOf(Reader input,
                          Function<Stream<String>, T> handler,
                          Function<IOException, RuntimeException> error){
        try (BufferedReader reader = new BufferedReader(input)) {
            return handler.apply(reader.lines());
        } catch (IOException e) {
            throw error.apply(e);
        }
    }

    private Function<IOException, RuntimeException> BuffReaderException = (x)-> new RuntimeException();

    public List<String> getMySelfBuffs(EQCharacter c) {
        return readBuffFile(c, "selfBuffs");
    }
    public List<String> getMyNeededBuffs(EQCharacter c){
        return readBuffFile(c, "neededBuffs");
    }

    public List<String> getAvailableBuffs(EQCharacter c) {
        return readBuffFile(c, "AvailableBuffs");
    }
}


