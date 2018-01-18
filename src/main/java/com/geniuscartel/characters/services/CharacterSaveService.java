package com.geniuscartel.characters.services;

import com.geniuscartel.App;
import com.geniuscartel.characters.classes.EQCharacter;
import com.geniuscartel.workers.ioworkers.AsyncRequestInterop;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CharacterSaveService {
    private AsyncRequestInterop threads;
    private String server = null;

    public CharacterSaveService(AsyncRequestInterop threads) {
        this.threads = threads;
    }

    private String getServer(){
        if (server == null) {
            try {
                server = threads.synchronousInformation(App.getActiveCharacters().get(0), "${MacroQuest}.Server");
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return server;
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


