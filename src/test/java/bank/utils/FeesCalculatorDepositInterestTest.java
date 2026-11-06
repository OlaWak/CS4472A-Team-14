package bank.utils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * Deposit Interest Test Cases - Weak Robust Equivalence Class Testing
 * D1-D8 are specification-derived representative cases.
 * D9 and D10 are exploratory negative-input tests based on an explicit
 * assumption because negative-input behavior is not defined by the specification.
 * A tolerance of 0.000000001 is used for floating-point comparison.
 */

class FeesCalculatorDepositInterestTest {
    private FeesCalculator calculator;

    @BeforeEach 
    void setup() {
        calculator = new FeesCalculator();
    }

    @Test
    void d1DepositFeeTestStudentHighAmountHighBalance() {
        // Student, amount > 50, balance >500
        // D1: Amount = 100, Balance = 1000, Student = true
        // Expected fee = 100 * 0.005 = 0.5
        double fee = calculator.calculateDepositInterest(
            100.00, 1000.00, true
        );
        assertEquals(100.00*0.005, fee, 0.000000001, "D1: Deposit interest should be 0.5% for student with amount 100 and balance 1000.");
    }

    @Test
    void d2DepositFeeTestStudentHighAmountLowBalance() {
        // Student, amount > 50, balance ≤ 500
        // D2: Amount = 100, Balance = 400, Student = true
        // Expected fee = 100 * 0.0025 = 0.25
        double fee = calculator.calculateDepositInterest(
            100.00, 400.00, true
        );
        assertEquals(100.00*0.0025, fee, 0.000000001, "D2: Deposit interest should be 0.25% for student with amount 100 and balance 400.");
    }

    @Test 
    // Student, amount <= 50, balance > 5000
    // D3: Amount = 25, Balance = 6000, Student = true
    // Expected fee = 25 * 0.005 = 0.125
    void d3DepositFeeTestStudentLowAmountHighBalance() {
        double fee = calculator.calculateDepositInterest(
            25.00, 6000.00, true
        );
        assertEquals(25.00*0.005, fee, 0.000000001, "D3: Deposit interest should be 0.5% for student with amount 25 and balance 6000.");

    }

    @Test
    // Student, amount <= 50, balance <= 5000
    // D4: Amount = 25, Balance = 2000, Student = true
    // Expected fee = 0
    void d4DepositFeeTestStudentLowAmountLowBalance(){
        double fee = calculator.calculateDepositInterest(
            25.00, 2000.00, true
        );
        assertEquals(0.0, fee, 0.000000001, "D4: Deposit interest should be 0 for student with amount 25 and balance 2000.");
    }

    @Test
    // Non-student, amount > 250, balance > 2500
    // D5: Amount = 500, Balance = 5000, Student = false
    // Expected fee = 500 * 0.008 = 4
    void d5DepositFeeTestNonStudentHighAmountHighBalance(){
        double fee = calculator.calculateDepositInterest(
            500.00, 5000.00, false
        );
        assertEquals(500.00*0.008, fee, 0.000000001, "D5: Deposit interest should be 0.8% for non-student with amount 500 and balance 5000.");
    }

    @Test 
    // Non-student, amount > 250, balance ≤ 2500
    // D6: Amount = 500, Balance = 1000, Student = false
    // Expected fee = 500 * 0.004 = 2
    void d6DepositFeeTestNonStudentHighAmountLowBalance(){
        double fee = calculator.calculateDepositInterest(
            500.00, 1000.00, false
        );
        assertEquals(500.00*0.004, fee, 0.000000001, "D6: Deposit interest should be 0.4% for non-student with amount 500 and balance 1000.");
    }

    @Test 
    // Non-student, amount ≤ 250, balance > 10000
    // D7: Amount = 100, Balance = 20000, Student = false
    // Expected fee = 0
    void d7DepositFeeTestNonStudentLowAmountHighBalance(){
        double fee = calculator.calculateDepositInterest(
            100.00, 20000.00, false
        );
        assertEquals(0.0, fee, 0.000000001, "D7: Deposit interest should be 0 for non-student with amount 100 and balance 20000.");
    }

    @Test 
    // Non-Student, amount ≤ 250, balance ≤ 10000
    // D8: Amount = 100, Balance = 5000, Student = false
    // Expected fee = 100 * 0.001 = 0.1
    void d8DepositFeeTestNonStudentLowAmountLowBalance(){
        double fee = calculator.calculateDepositInterest(
            100.00, 5000.00, false
        );
        assertEquals(100*0.001, fee, 0.000000001, "D8: Deposit interest should be 0.1% for non-student with amount 100 and balance 5000.");
    }

    // ---------- Exploratory Negative-Input Tests ----------

    @Test
    void d9RejectNegativeDepositAmount(){
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculateDepositInterest(-1, 4000.00, true),
        "D9: Negative deposit amount"
        ); 
    }

    @Test 
    void d10RejectNegativeBalance(){
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculateDepositInterest(150.00, -1, true),
        "D10: Negative balance"
        ); 
    }


}
