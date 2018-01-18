package com.geniuscartel.characters.services;

import com.geniuscartel.characters.CharacterState;
import com.geniuscartel.characters.Command;

public abstract class BuffCommand implements Command {

    @Override
    public CharacterState getSTATE() {
        return CharacterState.REST;
    }
}
