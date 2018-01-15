package com.geniuscartel.characters;

public abstract class ActionRequest implements Runnable {
    private final CharacterState STATE;

    public ActionRequest(CharacterState STATE) {
        this.STATE = STATE;
    }

    public CharacterState getSTATE() {
        return STATE;
    }
}
