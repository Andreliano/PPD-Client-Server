package org.example;

import java.io.Serializable;
import java.util.Objects;

public class Tuple<E1, E2, E3> implements Serializable {
    private E1 country;
    private E2 id;
    private E3 score;

    public Tuple(E1 country, E2 id, E3 score) {
        this.country = country;
        this.id = id;
        this.score = score;
    }

    public void setCountry(E1 country) {
        this.country = country;
    }
    public void setId(E2 id) {
        this.id = id;
    }
    public void setScore(E3 score) {
        this.score = score;
    }
    public E1 getCountry() {
        return country;
    }
    public E2 getId() {
        return id;
    }
    public E3 getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "country=" + country +
                ", id=" + id +
                ", score=" + score +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?, ?> tuple = (Tuple<?, ?, ?>) o;
        return Objects.equals(id, tuple.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}