package bank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Transfer-fee specification: decision table rules R1-R16.
 * Each test supplies one case and checks the fee required by its rule.
 */
class TransferFeeDecisionTableTest {

	private FeesCalculator calculator;

	@BeforeEach
	void setUp() {
		calculator = new FeesCalculator();
	}

	// Each test below is one column of our decision table. We pick one concrete
	// input that lands in that rule and check the fee the spec says it should get.
	// The expected fee is the percentage from the spec applied to the amount, so
	// 0.1% of $100 is 0.10. The test names spell out the situation (student or
	// not, amount size, and where the two balances sit) so a failure tells you
	// which rule broke without opening the table.
	// This first one is a student moving less than $200 with both balances low.
	@Test
	void r1StudentBelow200BothBalancesLow() {
		assertTransferFee(
				true,
				100.00,
				1000.00,
				500.00,
				0.10,
				"R1: Fee_0.1%");
	}

	@Test
	void r2StudentBelow200FromLowToHigh() {
		assertTransferFee(
				true,
				100.00,
				1000.00,
				1000.00,
				0.05,
				"R2: Fee_0.05%");
	}

	@Test
	void r3StudentBelow200FromHighToLow() {
		assertTransferFee(
				true,
				100.00,
				2000.00,
				500.00,
				0.05,
				"R3: Fee_0.05%");
	}

	@Test
	void r4StudentBelow200BothBalancesHigh() {
		assertTransferFee(
				true,
				100.00,
				2000.00,
				1000.00,
				0.025,
				"R4: Fee_0.025%");
	}

	@Test
	void r5StudentAt200BothBalancesLow() {
		assertTransferFee(
				true,
				200.00,
				1000.00,
				500.00,
				0.10,
				"R5: Fee_0.05%");
	}

	@Test
	void r6StudentAt200FromLowToHigh() {
		assertTransferFee(
				true,
				200.00,
				1000.00,
				1000.00,
				0.05,
				"R6: Fee_0.025%");
	}

	@Test
	void r7StudentAt200FromHighToLow() {
		assertTransferFee(
				true,
				200.00,
				2000.00,
				500.00,
				0.50,
				"R7: Fee_0.25%");
	}

	@Test
	void r8StudentAt200BothBalancesHigh() {
		assertTransferFee(
				true,
				200.00,
				2000.00,
				1000.00,
				0.25,
				"R8: Fee_0.125%");
	}

	@Test
	void r9NonStudentBelow100BothBalancesLow() {
		assertTransferFee(
				false,
				50.00,
				2000.00,
				1000.00,
				0.10,
				"R9: Fee_0.2%");
	}

	@Test
	void r10NonStudentBelow100FromLowToHigh() {
		assertTransferFee(
				false,
				50.00,
				2000.00,
				2000.00,
				0.05,
				"R10: Fee_0.1%");
	}

	@Test
	void r11NonStudentBelow100FromHighToLow() {
		assertTransferFee(
				false,
				50.00,
				4000.00,
				1000.00,
				0.50,
				"R11: Fee_1%");
	}

	@Test
	void r12NonStudentBelow100BothBalancesHigh() {
		assertTransferFee(
				false,
				50.00,
				4000.00,
				2000.00,
				0.25,
				"R12: Fee_0.5%");
	}

	@Test
	void r13NonStudentAt100BothBalancesLow() {
		assertTransferFee(
				false,
				100.00,
				1000.00,
				500.00,
				0.20,
				"R13: Fee_0.2%");
	}

	@Test
	void r14NonStudentAt100FromLowToHigh() {
		assertTransferFee(
				false,
				100.00,
				1000.00,
				1000.00,
				0.10,
				"R14: Fee_0.1%");
	}

	@Test
	void r15NonStudentAt100FromHighToLow() {
		assertTransferFee(
				false,
				100.00,
				2000.00,
				500.00,
				0.50,
				"R15: Fee_0.5%");
	}

	@Test
	void r16NonStudentAt100BothBalancesHigh() {
		assertTransferFee(
				false,
				100.00,
				2000.00,
				1000.00,
				0.25,
				"R16: Fee_0.25%");
	}

	// Shared helper so every rule test stays a one-liner of inputs. It runs the
	// calculator and compares against the fee we worked out from the spec.
	private void assertTransferFee(boolean student, double amountTransferred, double balanceComingFrom,
			double balanceGoingTo, double expectedFee, String rule) {
		double actualFee = calculator.calculateTransferFee(amountTransferred,
				balanceComingFrom, balanceGoingTo, student);

		assertEquals(expectedFee, actualFee, 0.000000001,
				"Transfer-fee specification " + rule);
	}
}
