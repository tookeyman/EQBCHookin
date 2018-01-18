package com.geniuscartel.characters;

public interface Command {
    void apply();

    CharacterState getSTATE();
}
