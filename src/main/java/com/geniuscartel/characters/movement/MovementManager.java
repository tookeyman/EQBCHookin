package com.geniuscartel.characters.movement;

import com.geniuscartel.characters.Command;
import com.geniuscartel.characters.EQCharacter;
import com.geniuscartel.characters.Location;
import com.geniuscartel.characters.services.CommandFactory;

public class MovementManager {
    private final EQCharacter ME;

    public MovementManager(EQCharacter ME) {
        this.ME = ME;
    }

    public void issueMovementCommand(Location location) {
        Command move;
        if(GeometryHelper.distance(ME.getStatus().getLoc(), location)<10.0) {
            return;
        }else{
            move = CommandFactory.createMovementCommand(location, ME);
        }
        ME.enqueCommand(move);
        ME.notify();
    }
}
