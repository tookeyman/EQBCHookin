package com.geniuscartel.characters.services;

import com.geniuscartel.characters.classes.EQCharacter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BuffService {
    private List<EQCharacter> availableBuffers = null;
    private List<BuffRequest> pendingRequests = new ArrayList<>();
    private int lastUpdateSize = -1;

    public boolean checkIfCharactersChanged(int size){
        System.out.println("buffers comparing " + size + " " + lastUpdateSize);
        boolean isSame = lastUpdateSize == size;
        if(!isSame)
            lastUpdateSize = size;
        return isSame;
    }

    public void printAvailableBuffers(){
        System.out.println("available buffers: ");
        availableBuffers.forEach(x -> System.out.println(x.getName()));
    }

    public boolean requestBuff(EQCharacter c, String spellName) {
        if (pendingRequests.size() > 1) {
            if(pendingRequests.stream().anyMatch(x->x.equals(c, spellName))) {
                System.out.println(String.format("[BUFF]\t%s\t%s FOUND ALREADY SUBMITTED BUFF REQUEST", c.getName(), spellName));
                return false;
            }
        }
        if(availableBuffers== null) {
            System.out.println(String.format("[BUFF]\t%s\t%s NO BUFFERS REGISTERED", c.getName(), spellName));
            return false;
        }
        EQCharacter caster = availableBuffers.stream()
            .filter(x -> x.getAvailableBuffs().contains(spellName))
            .sorted(Comparator.comparingInt(EQCharacter::queueDepth))
            .findFirst().orElse(null);
        if(caster == null) {
            System.out.println(String.format("[BUFF]\t%s\t%s NO AVAILABLE BUFFERS", c.getName(), spellName));
            return false;
        }
        BuffRequest br = new BuffRequest(c, caster, spellName);
        Runnable r = () -> {
            synchronized (pendingRequests){
                pendingRequests.remove(br);
            }
        };
        br.setReleaseRequest(r);
        pendingRequests.add(br);
        System.out.println("adding buff request: " + br.toString());
        BuffCommand command = BuffCommandFactory.create(br);
        caster.submitCommand(command);
        System.out.println(String.format("[BUFF]\t%s\t%s SUCCESS", c.getName(), spellName));
        return true;
    }


    public void updateBuffers(List<EQCharacter> characters){
        availableBuffers = characters.stream()
            .filter(cha -> cha.getAvailableBuffs().size() > 0)
            .collect(Collectors.toList());
        System.out.println("Parsed buffers: " + availableBuffers);
    }
}
