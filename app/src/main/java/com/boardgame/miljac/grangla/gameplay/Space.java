package com.boardgame.miljac.grangla.gameplay;


import lombok.Getter;
import lombok.Setter;

/**
 * A single field (space) on a game board
 *
 */
@Getter
@Setter
public class Space
{
    /**
     * The default constructor, sets the field (space) to be empty.
     */
    State state;

    public Space()
    {
        this.state=State.empty;
    }

    public boolean equals(Object o)
    {
        if (!( o instanceof Space ) )
            return false;
        Space that = (Space) o;
        return (this.state==that.state);
    }

}
