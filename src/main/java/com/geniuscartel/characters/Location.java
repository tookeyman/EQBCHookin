package com.geniuscartel.characters;

public class Location {
    float x, y, z;
    int q;

    public Location(float x, float y, float z, int q) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.q = q;
    }

    @Override
    public String toString() {
        return "Location{" +
            "x=" + x +
            ", y=" + y +
            ", z=" + z +
            ", q=" + q +
            '}';
    }
}
