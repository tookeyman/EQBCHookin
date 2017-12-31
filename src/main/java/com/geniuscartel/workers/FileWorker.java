package com.geniuscartel.workers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileWorker {

    private Matcher lookingFor;

    public void searchForPattern(Pattern p, Path startingPoint) {
        this.lookingFor = p.matcher("");
        try {
            Files.walk(startingPoint, FileVisitOption.FOLLOW_LINKS)
                    .filter(path->!path.toFile().isDirectory())
                    .map(Path::toFile)
                    .forEach(sniffForPattern);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Consumer<File> sniffForPattern = (f) -> {
        try(FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr)
        ){
            br.lines().filter(line->lookingFor.reset(line).find())
                    .collect(Collectors.toList())
                    .forEach(y-> System.out.println("Found reference in " + f.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


}
