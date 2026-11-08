package ru.vsu.thousand.model;

public enum Rank {
    NINE("Девятка", 0, 1),
    JACK("Валет", 2, 2),
    QUEEN("Дама", 3, 3),
    KING("Король", 4, 4),
    TEN("Десятка", 10, 5),
    ACE("Туз", 11, 6);

    private final String displayName;
    private final int points;
    private final int strength;

    Rank(String displayName, int points, int strength) {
        this.displayName = displayName;
        this.points = points;
        this.strength = strength;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPoints() {
        return points;
    }

    public int getStrength() {
        return strength;
    }
}