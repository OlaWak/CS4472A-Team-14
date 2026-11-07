package bank.utils;

import java.util.Calendar;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Robust worst-case boundary value analysis for Withdrawal Fee
 * Assumptions:
 *   - Balance domain: [0.00, 1,000,000.00], arbitrary upper limit for the purposes of testing as this was unspecified
 *   - Amount domain:  [1.00, 1,000,000.00], arbitrary limits for the purposes of testing as these were unspecified
 *   - Balance and amount resolution: $1.00
 *
 * Input variables and their robust BVA values:
 *   balance (17): min-/min/min+/nominal/max-/max/max+ for each of the 3 fee classes,
 *                 duplicates removed (15 valid, 2 invalid)
 *   amount   (7): min-/min/min+/nominal/max-/max/max+ (5 valid, 2 invalid)
 *   dayOfWeek(7): 0, 1, 2, 4, 6, 7, 8 (5 valid, 2 invalid)
 *   student  (2): true / false
 *
 * Worst-case product: 17 x 7 x 7 x 2 = 1666 combinations
 *   - 15 x 5 x 5 x 2 = 750 with all-valid inputs     -> expect the fee from the spec
 *   - 916 with at least one invalid input            -> expect IllegalArgumentException (assumed; spec is silent)
 */
class WithdrawalFeesTest {

	private static final double MILLION = 1000000.00;

	// customer bank balance
	private static final double[] BALANCES = {
		-1.00,                // just below the lowest balance (invalid)
		0.00,                 // lowest balance
		999.00,               // middle class min-  (just below the 1000 boundary)
		1000.00,              // middle class min   (on the 1000 boundary)
		1001.00,              // middle class min+
		3000.00,              // middle class nominal
		4998.00,              // middle class max-
		4999.00,              // middle class max   (just below the 5000 boundary)
		5000.00,              // middle class max+  (on the 5000 boundary)
		5001.00               // just above the 5000 boundary
	};

	// amount withdrawn
	private static final double[] AMOUNTS = {
		0.00,                 // min-  (invalid)
		1.00,                 // min
		2.00,                 // min+
		1000.00,             // nominal
		MILLION - 1.00,       // max-
		MILLION,              // max
		MILLION + 1.00        // max+  (invalid)
	};

	// weekday or weekend, passed to the function as a representative Calendar day
	private static final boolean[] WEEKDAY = { true, false };
	private static final int WEEKDAY_VALUE = Calendar.WEDNESDAY;
	private static final int WEEKEND_VALUE = Calendar.SATURDAY;
 
	// student account status
	private static final boolean[] STUDENT = { true, false };
 
	private final FeesCalculator calculator = new FeesCalculator();
 
	// The fee rules from the specification
	private enum Rule {
		STUDENT_WEEKDAY("S1: student, weekday", 0.0),
		STUDENT_WEEKEND("S2: student, weekend", 0.001),
		NON_STUDENT_BALANCE_BELOW_1000("S3: non-student, balance < 1000", 0.003),
		NON_STUDENT_BALANCE_1000_TO_4999("S4: non-student, 1000 <= balance < 5000", 0.001),
		NON_STUDENT_BALANCE_5000_OR_MORE("S5: non-student, balance >= 5000", 0.0);

		final String description;
		final double rate;

		Rule(String description, double rate) {
			this.description = description;
			this.rate = rate;
		}
	}

	// Which rule the spec says applies
	private static Rule specRule(boolean student, boolean weekday, double balance) {
		if (student) {
			return weekday ? Rule.STUDENT_WEEKDAY : Rule.STUDENT_WEEKEND;
		}
		if (balance < 1_000.00) {
			return Rule.NON_STUDENT_BALANCE_BELOW_1000;
		} else if (balance < 5_000.00) {
			return Rule.NON_STUDENT_BALANCE_1000_TO_4999;
		} else {
			return Rule.NON_STUDENT_BALANCE_5000_OR_MORE;
		}
	}

	private static boolean validBalance(double balance) {
		return balance >= 0.00;
	}

	private static boolean validAmount(double amount) {
		return amount >= 1.00 && amount <= MILLION;
	}

	// Relative tolerance: expected fees range from 0.001 to 3000
	private static double tolerance(double expected) {
		return Math.max(1e-12, Math.abs(expected) * 1e-9);
	}

	private static String describeInputs(double balance, double amount, boolean student, boolean weekday) {
		return String.format("balance=%.2f%s, amount=%.2f%s, %s, %s",
				balance, validBalance(balance) ? "" : " [INVALID BALANCE]",
				amount, validAmount(amount) ? "" : " [INVALID AMOUNT]",
				student ? "student" : "non-student",
				weekday ? "weekday" : "weekend");
	}

	// Worst-case product, split into all-valid and at-least-one-invalid combinations
	private static Stream<Arguments> combinations(boolean wantValid) {
		Stream.Builder<Arguments> b = Stream.builder();
		for (double balance : BALANCES) {
			for (double amount : AMOUNTS) {
				for (boolean weekday : WEEKDAY) {
					for (boolean student : STUDENT) {
						if ((validBalance(balance) && validAmount(amount)) == wantValid) {
							b.add(Arguments.of(balance, amount, student, weekday));
						}
					}
				}
			}
		}
		return b.build();
	}

	static Stream<Arguments> validCombinations() {
		return combinations(true);
	}

	static Stream<Arguments> invalidCombinations() {
		return combinations(false);
	}

	// 180 tests: all inputs valid
	@ParameterizedTest(name = "balance={0}, amount={1}, student={2}, weekday={3}")
	@MethodSource("validCombinations")
	@DisplayName("Worst-case product (all valid inputs)")
	void validInputs(double balance, double amount, boolean student, boolean weekday) {
		Rule rule = specRule(student, weekday, balance);
		double expected = amount * rule.rate;
		int day = weekday ? WEEKDAY_VALUE : WEEKEND_VALUE;

		double actual = calculator.calculateWithdrawalFee(amount, balance, student, day);

		assertEquals(expected, actual, tolerance(expected),
				String.format("Wrong fee for [%s]. Spec rule: '%s' (%.1f%% of amount)",
						describeInputs(balance, amount, student, weekday), rule.description, rule.rate * 100));
	}

	// 100 tests: out-of-range balance or amount
	// The spec does not define the behaviour; IllegalArgumentException is assumed.
	@ParameterizedTest(name = "balance={0}, amount={1}, student={2}, weekday={3}")
	@MethodSource("invalidCombinations")
	@DisplayName("Worst-case product (out-of-range balance or amount)")
	void invalidInputs(double balance, double amount, boolean student, boolean weekday) {
		int day = weekday ? WEEKDAY_VALUE : WEEKEND_VALUE;

		assertThrows(IllegalArgumentException.class,
				() -> calculator.calculateWithdrawalFee(amount, balance, student, day),
				String.format("Expected IllegalArgumentException for [%s]",
						describeInputs(balance, amount, student, weekday)));
	}
}

