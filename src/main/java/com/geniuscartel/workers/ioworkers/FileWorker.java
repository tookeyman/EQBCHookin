package com.geniuscartel.workers.ioworkers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileWorker {

    private Matcher lookingFor;

    public void searchFolderForPattern(Path startingPoint, Pattern p) {
        this.lookingFor = p.matcher("");
        try {
            Files.walk(startingPoint, FileVisitOption.FOLLOW_LINKS)
                .filter(path -> !path.toFile().isDirectory())
                .map(Path::toFile)
                .forEach(sniffForPattern);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File searchForFile(Path startingPoint, String fileName) throws IOException {
        return Files.walk(startingPoint, FileVisitOption.FOLLOW_LINKS)
            .filter(matchesFileName(fileName))
            .findAny()
            .map(Path::toFile)
            .orElse(null);
    }

    private static Predicate<Path> matchesFileName(String fileName){
        return (x) -> x.toFile().getName().matches(fileName);
    }

    private Consumer<File> sniffForPattern = (f) -> {
        try(FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr)
        ){
            br.lines()
                    .filter(line -> lookingFor.reset(line).find())
                    .collect(Collectors.toList())
                    .forEach(y -> System.out.println("Found reference in " + f.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


}
