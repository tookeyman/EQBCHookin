package com.geniuscartel.characters.movement;

import com.geniuscartel.characters.Location;

public class GeometryHelper {
    public static double distance(Location loc1, Location loc2) {
        //warning contains square root
        double x = Math.pow((loc1.getX() - loc2.getX()), 2);
        double y = Math.pow((loc1.getY() - loc2.getY()), 2);
        return Math.sqrt(x + y);
    }
    public static double distance3d(Location loc1, Location loc2) {
        //warning contains square root
        double x = Math.pow((loc2.getX() - loc1.getX()), 2);
        double y = Math.pow((loc2.getY() - loc1.getY()), 2);
        double z = Math.pow((loc2.getZ() - loc1.getZ()), 2);
        return Math.sqrt(x + y + z);
    }
}
