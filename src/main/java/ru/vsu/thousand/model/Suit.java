package ru.vsu.thousand.model;

public enum Suit {
    SPADES("Пики", 40),
    CLUBS("Трефы", 60),
    DIAMONDS("Бубны", 80),
    HEARTS("Червы", 100);

    private final String displayName;
    private final int marriagePoints;

    Suit(String displayName, int marriagePoints) {
        this.displayName = displayName;
        this.marriagePoints = marriagePoints;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMarriagePoints() {
        return marriagePoints;
    }
}