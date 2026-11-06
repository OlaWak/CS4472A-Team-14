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

	private static final double MILLION = 1_000_000.00;

	// customer bank balance
	private static final double[] BALANCES = {
		// class 1: [0.00, 1000.00)
		-1.00, 0.00, 1.00, 500.00, 998.00, 999.00,
		// class 2: [1000.00, 5000.00)
		1000.00, 1001.00, 3000.00, 4998.00, 4999.00,
		// class 3: [5000.00, 1_000_000.00]
		5000.00, 5001.00, 50_000.00, MILLION - 1.00, MILLION, MILLION + 1.00
	};

	// amount withdrawn
	private static final double[] AMOUNTS = {
		0.00,                 // min-  (invalid)
		1.00,                 // min
		2.00,                 // min+
		1_000.00,             // nominal
		MILLION - 1.00,       // max-
		MILLION,              // max
		MILLION + 1.00        // max+  (invalid)
	};

	// day of transaction
	private static final int[] DAYS = {
		0,                    // min-  (invalid)
		Calendar.SUNDAY,      // min   (1, weekend)
		Calendar.MONDAY,      // min+  (2, first weekday)
		Calendar.WEDNESDAY,   // nominal (4)
		Calendar.FRIDAY,      // max-  (6, last weekday)
		Calendar.SATURDAY,    // max   (7, weekend)
		8                     // max+  (invalid)
	};

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

	// Which rule the spec says applies (valid inputs only)
	private static Rule specRule(boolean student, int day, double balance) {
		if (student) {
			boolean weekend = (day == Calendar.SATURDAY || day == Calendar.SUNDAY);
			return weekend ? Rule.STUDENT_WEEKEND : Rule.STUDENT_WEEKDAY;
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
		return balance >= 0.00 && balance <= MILLION;
	}

	private static boolean validAmount(double amount) {
		return amount >= 1.00 && amount <= MILLION;
	}

	private static boolean validDay(int day) {
		return day >= Calendar.SUNDAY && day <= Calendar.SATURDAY;
	}

	// Relative tolerance: expected fees range from 0.001 to 3000
	private static double tolerance(double expected) {
		return Math.max(1e-12, Math.abs(expected) * 1e-9);
	}

	private static String dayName(int day) {
		switch (day) {
			case Calendar.SUNDAY:    return "SUNDAY";
			case Calendar.MONDAY:    return "MONDAY";
			case Calendar.TUESDAY:   return "TUESDAY";
			case Calendar.WEDNESDAY: return "WEDNESDAY";
			case Calendar.THURSDAY:  return "THURSDAY";
			case Calendar.FRIDAY:    return "FRIDAY";
			case Calendar.SATURDAY:  return "SATURDAY";
			default:                 return "INVALID";
		}
	}

	private static String describeInputs(double balance, double amount, boolean student, int day) {
		return String.format("balance=%.2f%s, amount=%.2f%s, %s, day=%d (%s)%s",
				balance, validBalance(balance) ? "" : " [INVALID BALANCE]",
				amount, validAmount(amount) ? "" : " [INVALID AMOUNT]",
				student ? "student" : "non-student",
				day, dayName(day), validDay(day) ? "" : " [INVALID DAY]");
	}

	// Worst-case product, split into all-valid and at-least-one-invalid combinations
	private static Stream<Arguments> combinations(boolean wantValid) {
		Stream.Builder<Arguments> b = Stream.builder();
		for (double balance : BALANCES) {
			for (double amount : AMOUNTS) {
				for (int day : DAYS) {
					for (boolean student : STUDENT) {
						boolean allValid = validBalance(balance) && validAmount(amount) && validDay(day);
						if (allValid == wantValid) {
							b.add(Arguments.of(balance, amount, student, day));
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

	// 750 tests: all inputs valid
	@ParameterizedTest(name = "balance={0}, amount={1}, student={2}, day={3}")
	@MethodSource("validCombinations")
	@DisplayName("Worst-case product (all valid inputs)")
	void validInputs(double balance, double amount, boolean student, int day) {
		Rule rule = specRule(student, day, balance);
		double expected = amount * rule.rate;

		double actual = calculator.calculateWithdrawalFee(amount, balance, student, day);

		assertEquals(expected, actual, tolerance(expected),
				String.format("Wrong fee for [%s]. Spec rule: '%s' (%.1f%% of amount)",
						describeInputs(balance, amount, student, day), rule.description, rule.rate * 100));
	}

	// 916 tests: at least one invalid input
	// The spec does not define the behaviour; IllegalArgumentException is assumed.
	@ParameterizedTest(name = "balance={0}, amount={1}, student={2}, day={3}")
	@MethodSource("invalidCombinations")
	@DisplayName("Worst-case product (at least one invalid input)")
	void invalidInputs(double balance, double amount, boolean student, int day) {
		assertThrows(IllegalArgumentException.class,
				() -> calculator.calculateWithdrawalFee(amount, balance, student, day),
				String.format("Expected IllegalArgumentException for [%s]",
						describeInputs(balance, amount, student, day)));
	}
}
