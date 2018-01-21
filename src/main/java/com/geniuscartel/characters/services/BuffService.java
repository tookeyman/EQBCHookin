package com.geniuscartel.characters.services;

import com.geniuscartel.characters.EQCharacter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BuffService {
    private List<EQCharacter> availableBuffers = null;
    private final List<BuffRequest> pendingRequests = new ArrayList<>();
    private int lastUpdateSize = -1;

    public boolean checkIfCharactersChanged(int size){
        boolean isSame = lastUpdateSize == size;
        if(!isSame)
            lastUpdateSize = size;
        return isSame;
    }

    public void requestBuff(EQCharacter c, String spellName) throws Error{
        if (pendingRequests.size() > 0) {
            if (pendingRequests.stream().anyMatch(x -> x.equals(c, spellName))) {
                String error = String.format("[BUFF]\t%s\t%s FOUND ALREADY SUBMITTED BUFF REQUEST", c.getName(), spellName);
                throw new Error(error);
            }
        }
        if (availableBuffers == null) {
            String error = String.format("[BUFF]\t%s\t%s NO BUFFERS REGISTERED", c.getName(), spellName);
            throw new Error(error);
        }
        EQCharacter caster = getLeastBurdenedCaster(spellName);
        if(caster == null) {
            String error = String.format("[BUFF]\t%s\t%s NO AVAILABLE BUFFERS", c.getName(), spellName);
            throw new Error(error);
        }
        BuffRequest request = new BuffRequest(c, caster, spellName);
        Runnable r = () -> {
            synchronized (pendingRequests){
                pendingRequests.remove(request);
            }
        };
        request.setReleaseRequest(r);
        pendingRequests.add(request);
        CommandFactory.BuffCommand command = CommandFactory.createBuffCommand(request);
        caster.submitCommand(command);
        synchronized (caster) {
            caster.currentActionDescription = "notifying for buff";
            caster.notify();
            caster.currentActionDescription = "finished notifying for buff";
        }
    }

    private EQCharacter getLeastBurdenedCaster(String spellName){
        return availableBuffers.stream()
            .filter(x -> x.getBuffManager().getAvailableBuffs().contains(spellName))
            .min(Comparator.comparingInt(EQCharacter::getActionQueueDepth)).orElse(null);
    }


    public void updateBuffers(List<EQCharacter> characters){
        availableBuffers = characters.stream()
            .filter(cha -> cha.getBuffManager().getAvailableBuffs().size() > 0)
            .collect(Collectors.toList());
//        System.out.println("Parsed buffers: " + availableBuffers);
    }

    public static class BuffRequest {
        private EQCharacter requester, caster;
        private String spellName;
        private Runnable releaseRequest = ()->{};

        BuffRequest(EQCharacter requester, EQCharacter caster, String spellName) {
            this.requester = requester;
            this.caster = caster;
            this.spellName = spellName;
        }

        @Override
        public String toString() {
            return "BuffRequest{" +
                "requester=" + requester +
                ", caster=" + caster +
                ", spellName='" + spellName + '\'' +
                ", releaseRequest=" + releaseRequest +
                '}';
        }

        public EQCharacter getRequester() {
            return requester;
        }

        public EQCharacter getCaster() {
            return caster;
        }

        public String getSpellName() {
            return spellName;
        }

        public Runnable getReleaseRequest() {
            return releaseRequest;
        }

        public void setReleaseRequest(Runnable releaseRequest) {
            this.releaseRequest = releaseRequest;
        }

        public boolean equals(EQCharacter c, String spellName){
            boolean reqName = this.requester.getName().equals(c.getName());
            boolean sameSpell = this.spellName.equals(spellName);
            return reqName && sameSpell;
        }
    }
}


