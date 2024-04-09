package com.boardgame.miljac.grangla.gameplay;

public class Coordinates {
    int x;
    int y;

    public Coordinates(int ix, int iy) {
        int i2 = (ix + TableConfig.TABLE_SIZE*2) % TableConfig.TABLE_SIZE;
        int j2 = (iy + TableConfig.TABLE_SIZE*2) % TableConfig.TABLE_SIZE;
        this.x = i2;
        this.y = j2;
    }

    public boolean equals(Object aThat){
        if ( this == aThat ) return true;
        if ( !(aThat instanceof Coordinates) ) return false;
        Coordinates that = (Coordinates)aThat;
        return ((this.x == that.x) && (this.y == that.y));
    }
}
