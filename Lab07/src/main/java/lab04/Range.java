package lab04;

public class Range {
    private int min;
    private int max;

    public Range(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Min cannot be greater than Max");
        }
        this.min = min;
        this.max = max;
    }

    public boolean isInRange(int number) {
        return number >= min && number <= max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
