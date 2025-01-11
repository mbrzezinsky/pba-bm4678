package lab04;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RangeTest {
    private Range range;

    @BeforeEach
    public void setUp() {
        range = new Range(10, 20);
    }

    @Test
    public void testIsMaxGreater() {
        assertThrows(IllegalArgumentException.class, () -> new Range(20, 10));
    }

    @Test
    public void testIsInRangeInside() {
        assertTrue(range.isInRange(15));
    }

    @Test
    public void testIsEdgeCase() {
        assertTrue(range.isInRange(10));
    }

    @Test
    public void testIsInRangeOutsideBelow() {
        assertFalse(range.isInRange(5));
    }
}
