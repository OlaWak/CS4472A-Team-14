package atm.session;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import atm.ATM;
import atm.dispatcher.MessageDispatcher;
import atm.exceptions.InvalidAmountException;
import atm.exceptions.InvalidCardNumberException;
import atm.exceptions.InvalidPinFormatException;
import atm.session.states.SessionState;
import atm.session.transactions.ATMWithdrawal;
import atm.ui.panels.MainPanel;
import atm.utils.CredentialsCheck;
import atm.utils.FormatChecker;
import bank.transactions.utils.AccountType;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

/**
 * Tests the assignment's ATM Session PIN and cash-withdrawal requirements.
 * Boundary inputs are varied one characteristic at a time under the
 * single-fault assumption: PIN length, one PIN digit, or withdrawal amount.
 * PIN format is checked through ATM.checkPin(), while amount validation is
 * exercised through Session.setAmount(). The daily-limit account is modeled
 * only to choose the amount boundaries because Session has no account-limit API.
 */
public class SessionTest {
	// Any format-valid card number is sufficient; the dispatcher is mocked.
	private static final String VALID_FORMAT_CARD_NUMBER = "4111111111111111";
	// Five digits are required; this nominal value is a test choice, not a spec value.
	private static final String NOMINAL_VALID_PIN = "24681";
	// Middle-of-range nominal amount: 11 x $50, not divisible by $20.
	private static final int NOMINAL_WITHDRAWAL_AMOUNT = 550;
	// Assignment account values used to calculate the withdrawal boundary.
	private static final int SIMULATED_AVAILABLE_BALANCE = 5000;
	private static final int SIMULATED_DAILY_LIMIT = 1000;
	// Closest bill-dispensable withdrawal amounts below and above the limit.
	private static final int DISPENSABLE_AMOUNT_BELOW_LIMIT = 990;
	private static final int DISPENSABLE_AMOUNT_ABOVE_LIMIT = 1020;
	// This test-only model is not connected to Session or the bank database.
	private static final SimulatedAccount ACCOUNT = new SimulatedAccount(
			SIMULATED_AVAILABLE_BALANCE, SIMULATED_DAILY_LIMIT);
	private MainPanel mainPanel;
	private MessageDispatcher dispatcher;
	private Session session;

	@BeforeEach
	public void setUp() {
		// The UI and dispatcher are fakes so tests need no screen or bank service.
		// Valid credentials let format-valid PIN cases complete the PIN check;
		// the invalid-format cases should stop before credentials are checked.
		mainPanel = Mockito.mock(MainPanel.class);
		dispatcher = Mockito.mock(MessageDispatcher.class);
		Mockito.when(dispatcher.checkCredentials(Mockito.anyString(),
				Mockito.any(char[].class))).thenReturn(true);
		session = new Session(mainPanel, dispatcher);
		// Session.setAmount() calls its state after amount validation. A no-op
		// state keeps these tests focused on validation and prevents transaction
		// execution, so they do not test bank balance changes or limit enforcement.
		SessionState noOpState = currentSession -> {
			// Intentionally do nothing after the amount has been checked.
		};
		session.setState(noOpState);
		// Select transaction choice 1 (withdrawal) and an account type before
		// calling setAmount(), matching the ATM flow up to the amount prompt.
		session.setTransaction(new ATMWithdrawal());
		session.setFromAccount(AccountType.Chequing);
	}

	/*
	 * PIN-length boundaries: the specification requires exactly five digits.
	 * Four and six characters are the nearest invalid lengths; the nominal PIN
	 * is the valid baseline. Case identifiers are parameterized display names so
	 * each input and result remains identifiable in JUnit reports.
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("pinLengthCases")
	public void pinLengthBoundaries(String caseId, String pin, boolean expectedValid)
			throws InvalidCardNumberException {
		assertPinResult(pin, expectedValid);
	}

	private static Stream<Arguments> pinLengthCases() {
		return Stream.of(
				Arguments.of("rejectsPinOneDigitBelowRequiredLength", "2468", false),
				Arguments.of("acceptsPinAtRequiredLength", NOMINAL_VALID_PIN, true),
				Arguments.of("rejectsPinOneDigitAboveRequiredLength", "246810", false));
	}

	/*
	 * PIN-digit boundaries: keep the length at five and vary the first digit.
	 * The valid range is '0' through '9'; '/' and ':' are the adjacent ASCII
	 * characters outside it. The other PIN digits remain nominal.
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("pinDigitBoundaryCases")
	public void pinDigitBoundaries(String caseId, String pin, boolean expectedValid)
			throws InvalidCardNumberException {
		assertPinResult(pin, expectedValid);
	}

	private static Stream<Arguments> pinDigitBoundaryCases() {
		return Stream.of(
				Arguments.of("rejectsCharacterImmediatelyBelowDigitRange", "/4681", false),
				Arguments.of("acceptsPinWithDigitAtLowerBoundary", "04681", true),
				Arguments.of("acceptsPinWithDigitAdjacentToLowerBoundary", "14681", true),
				Arguments.of("acceptsPinWithDigitAdjacentToUpperBoundary", "84681", true),
				Arguments.of("acceptsPinWithDigitAtUpperBoundary", "94681", true),
				Arguments.of("rejectsCharacterImmediatelyAboveDigitRange", ":4681", false));
	}

	/*
	 * Supplemental position checks: vary one character in positions two through
	 * five, keeping the remaining characters nominal. These checks supplement
	 * the first-position BVA cases. Five-character invalid samples can be
	 * rejected by the implementation's earlier length fault, so those passes are
	 * masked and do not independently prove digit validation.
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("pinPositionCases")
	public void pinPositionChecks(String caseId, String pin, boolean expectedValid)
			throws InvalidCardNumberException {
		assertPinResult(pin, expectedValid);
	}

	private static Stream<Arguments> pinPositionCases() {
		return Stream.of(
				Arguments.of("rejectsNonDigitInSecondPinPosition", "2/681", false),
				Arguments.of("rejectsNonDigitInThirdPinPosition", "24:81", false),
				Arguments.of("rejectsNonDigitInFourthPinPosition", "246/1", false),
				Arguments.of("rejectsCharacterImmediatelyBelowDigitRangeAtFifthPosition", "2468/", false),
				Arguments.of("rejectsCharacterImmediatelyAboveDigitRangeAtFifthPosition", "2468:", false),
				Arguments.of("acceptsPinWithZeroInFifthPosition", "24680", true),
				Arguments.of("acceptsPinWithNineInFifthPosition", "24689", true));
	}

	private void assertPinResult(String pin, boolean expectedValid)
			throws InvalidCardNumberException {
		ATM atm = createAtmAtPinPrompt();
		if (expectedValid) {
			assertDoesNotThrow(() -> atm.checkPin(pin.toCharArray()));
		} else {
			assertThrows(InvalidPinFormatException.class, () -> atm.checkPin(pin.toCharArray()));
		}
	}

	/*
	 * Lower amount boundary: $20 is the smallest positive dispensable amount.
	 * Robust BVA uses the adjacent integer values $19 and $21, even though
	 * neither can be dispensed with $20/$50 bills. The nominal $550 is an
	 * interior amount dispensed as eleven $50 bills and is not divisible by $20.
	 */
	@Test
	public void rejectsAmountOneDollarBelowMinimumDispensableAmount() {
		assertThrows(InvalidAmountException.class, () -> session.setAmount(19));
	}

	@Test
	public void acceptsMinimumDispensableAmount() {
		assertDoesNotThrow(() -> session.setAmount(20));
	}

	@Test
	public void rejectsAmountOneDollarAboveMinimumDispensableAmount() {
		assertThrows(InvalidAmountException.class, () -> session.setAmount(21));
	}

	/**
	 * Zero is not a withdrawal amount. This is a supplemental invalid-input
	 * probe; it is outside the core boundary set around the minimum of $20.
	 */
	@Test
	public void rejectsZeroWithdrawalAmount() {
		assertThrows(InvalidAmountException.class, () -> session.setAmount(0));
	}

	@Test
	public void acceptsNominalWithdrawalAmount() {
		assertDoesNotThrow(() -> session.setAmount(NOMINAL_WITHDRAWAL_AMOUNT));
	}

	/*
	 * Upper amount boundary: min($5,000 balance, $1,000 daily limit) is $1,000.
	 * Following the instructor's guidance, use the nearest bill-dispensable
	 * values around the limit: $990 below, $1,000 at, and $1,020 above. This
	 * keeps cash denomination valid so the above-limit test isolates the missing
	 * daily-limit check.
	 */
	@Test
	public void acceptsNearestDispensableAmountBelowDailyLimit() {
		assertDoesNotThrow(() -> session.setAmount(DISPENSABLE_AMOUNT_BELOW_LIMIT));
	}

	@Test
	public void acceptsAmountAtDailyLimit() {
		assertDoesNotThrow(() -> session.setAmount(ACCOUNT.maximumWithdrawalBoundary()));
	}

	@Test
	public void rejectsNearestDispensableAmountAboveDailyLimit() {
		assertThrows(InvalidAmountException.class,
				() -> session.setAmount(DISPENSABLE_AMOUNT_ABOVE_LIMIT));
	}

	/*
	 * Session.addPin() stores a PIN and advances session state without checking
	 * its format. ATM.checkPin() invokes FormatChecker.checkPinFormat() first, so
	 * this helper builds the application's real PIN-validation path. Each test
	 * gets a fresh ATM at the PIN prompt to avoid state leaking between cases.
	 */
	private ATM createAtmAtPinPrompt() throws InvalidCardNumberException {
		ATM atm = new ATM(new FormatChecker(), new CredentialsCheck(dispatcher), dispatcher);
		atm.setMainPanel(mainPanel);
		atm.createSession();
		atm.checkCardNumber(VALID_FORMAT_CARD_NUMBER);
		return atm;
	}

	/**
	 * Holds the assignment's account values for choosing test inputs. The
	 * production Session API has no way to attach this model or configure a daily
	 * limit, so it is not a bank account and does not enforce withdrawal limits.
	 */
	private static final class SimulatedAccount {
		private final int availableBalance;
		private final int dailyLimit;

		private SimulatedAccount(int availableBalance, int dailyLimit) {
			this.availableBalance = availableBalance;
			this.dailyLimit = dailyLimit;
		}

		/**
		 * Returns the stricter of the modeled available balance and daily limit;
		 * for the assignment values, this evaluates to min(5000, 1000) = 1000.
		 */
		private int maximumWithdrawalBoundary() {
			return Math.min(availableBalance, dailyLimit);
		}
	}

}
