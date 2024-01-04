package org.example;

import java.io.Serializable;

public class Participant implements Serializable {

    private Long idParticipant;

    private Long idCountry;

    private int points;

    public Long getIdParticipant() {
        return idParticipant;
    }

    public void setIdParticipant(Long idParticipant) {
        this.idParticipant = idParticipant;
    }

    public Long getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return  idParticipant +
                " " + idCountry +
                " " + points;
    }
}
