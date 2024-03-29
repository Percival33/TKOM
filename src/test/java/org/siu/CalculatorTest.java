package org.siu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
public class CalculatorTest {
    public class Calculator {
        public int add(int a, int b) {
            return a + b;
        }
    }
    @Test
    public void addition() {
        Calculator calculator = new Calculator();
        assertEquals(2, calculator.add(1, 1), "1 + 1 should equal 2");
    }
}