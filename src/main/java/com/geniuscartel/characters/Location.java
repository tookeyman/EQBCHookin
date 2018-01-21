package com.geniuscartel.characters;

public class Location {
    private final float x, y, z;
    private final int q;

    public Location(float x, float y, float z, int q) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.q = q;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public int getQ() {
        return q;
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
