package com.geniuscartel.characters.services;

import com.geniuscartel.characters.classes.EQCharacter;

public class BuffRequest {
    private EQCharacter requester, caster;
    private String spellName;
    private Runnable releaseRequest = ()->{};

    public BuffRequest(EQCharacter requester, EQCharacter caster, String spellName) {
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
