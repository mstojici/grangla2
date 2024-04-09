package com.boardgame.miljac.grangla.high_scores;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Score {
    String level;
    long score;
    String name;
    String tag;

    public Score(@JsonProperty("level") String level,
                 @JsonProperty("score") long score,
                 @JsonProperty("name") String name) {
        this.level = level;
        this.score = score;
        this.name = name;
    }

    public Score(String tag){
        this.tag = tag;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString(){
        if(tag != null){
            return tag;
        }
        return null;
    }
}
