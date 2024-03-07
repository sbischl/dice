package com.github.sbischl.dice;

public class FrequencyDatapoint {
    private double expectedCount;
    private int actualCount;
    private int name;

    public FrequencyDatapoint(int actualCount, double expectedCount, int name) {
        this.expectedCount = expectedCount;
        this.actualCount = actualCount;
        this.name = name;
    }

    public double getExpectedCount() {
        return expectedCount;
    }

    public int getActualCount() {
        return actualCount;
    }

    public int getName() {
        return name;
    }
}
