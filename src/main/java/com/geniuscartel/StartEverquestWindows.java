package com.geniuscartel;

import java.io.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StartEverquestWindows {
    public static void main(String... args) {
        accountNames().stream().forEach(login);
    }

    private static List<String> accountNames(){
        String accountList = "D:\\MQ2\\fullraid.txt";
        List<String> accountNames = null;
        try (FileReader fr = new FileReader(new File(accountList));
             BufferedReader br = new BufferedReader(fr)) {
            accountNames = br.lines().collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return accountNames;
    }

    private static Consumer<String> login =
         x -> { try {
             String shellCommand = String.format("D:\\everquest\\eqgame.exe patchme /login:%s", x);
             System.out.println(shellCommand);
//             Runtime.getRuntime().exec();
             Thread.sleep(1000);
         } catch (InterruptedException e) { e.printStackTrace(); }
         };

}
