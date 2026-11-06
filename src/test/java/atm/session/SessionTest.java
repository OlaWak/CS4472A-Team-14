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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
	// Assignment account values used to calculate the withdrawal boundary.
	private static final int SIMULATED_AVAILABLE_BALANCE = 5000;
	private static final int SIMULATED_DAILY_LIMIT = 1000;
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
	 * Four and six characters are the nearest invalid lengths; NOMINAL_VALID_PIN is
	 * the valid baseline. These tests use numeric characters so length is the
	 * only input characteristic being changed.
	 */
	@Test
	public void rejectsPinOneDigitBelowRequiredLength() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'2', '4', '6', '8'}));
	}

	@Test
	public void acceptsPinAtRequiredLength() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt().checkPin(NOMINAL_VALID_PIN.toCharArray()));
	}

	@Test
	public void rejectsPinOneDigitAboveRequiredLength() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'2', '4', '6', '8', '1', '0'}));
	}

	/*
	 * PIN-digit robust boundaries: hold the PIN length at five and keep the
	 * other digits nominal. The valid range is '0' through '9'; '/' and ':' are
	 * the adjacent ASCII characters just outside it. Values 0, 1, 8, and 9 cover
	 * both valid endpoints and the nearest valid values inside those endpoints.
	 */
	@Test
	public void acceptsPinWithDigitAtLowerBoundary() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'0', '4', '6', '8', '1'}));
	}

	@Test
	public void acceptsPinWithDigitAdjacentToLowerBoundary() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'1', '4', '6', '8', '1'}));
	}

	@Test
	public void acceptsPinWithDigitAdjacentToUpperBoundary() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'8', '4', '6', '8', '1'}));
	}

	@Test
	public void acceptsPinWithDigitAtUpperBoundary() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'9', '4', '6', '8', '1'}));
	}

	@Test
	public void rejectsCharacterImmediatelyBelowDigitRange() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'/', '4', '6', '8', '1'}));
	}

	@Test
	public void rejectsCharacterImmediatelyAboveDigitRange() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {':', '4', '6', '8', '1'}));
	}

	@Test
	public void rejectsNonDigitInSecondPinPosition() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'2', '/', '6', '8', '1'}));
	}

	@Test
	public void rejectsNonDigitInThirdPinPosition() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'2', '4', ':', '8', '1'}));
	}

	@Test
	public void rejectsNonDigitInFourthPinPosition() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'2', '4', '6', '/', '1'}));
	}

	/*
	 * Supplemental position checks: put invalid boundary characters in positions
	 * two through five and valid endpoint digits in position five. This checks
	 * whether the rule applies throughout the PIN, rather than only at position
	 * one. The five-character invalid samples can be rejected by the current
	 * implementation's earlier length fault, so those passing assertions are
	 * masked and do not independently prove the digit checks work.
	 */
	@Test
	public void rejectsCharacterImmediatelyBelowDigitRangeAtFifthPosition() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'2', '4', '6', '8', '/'}));
	}

	@Test
	public void rejectsCharacterImmediatelyAboveDigitRangeAtFifthPosition() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'2', '4', '6', '8', ':'}));
	}

	@Test
	public void acceptsPinWithZeroInFifthPosition() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'2', '4', '6', '8', '0'}));
	}

	@Test
	public void acceptsPinWithNineInFifthPosition() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'2', '4', '6', '8', '9'}));
	}

	/*
	 * Lower amount boundary: with only $20 and $50 bills, $20 is the smallest
	 * positive dispensable amount. $19 and $21 are the adjacent integer values
	 * and cannot be formed from those bills. $40 is the next dispensable amount
	 * and $500 is the nominal amount used for the other amount-boundary tests.
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

	@Test
	public void acceptsNextDispensableAmountAboveMinimum() {
		assertDoesNotThrow(() -> session.setAmount(40));
	}

	@Test
	public void acceptsNominalWithdrawalAmount() {
		assertDoesNotThrow(() -> session.setAmount(500));
	}

	/*
	 * Upper amount boundary: the simulated account has $5,000 available and a
	 * $1,000 daily limit, so min(balance, limit) makes $1,000 the upper boundary.
	 * $990 is the nearest dispensable amount below it (19 x $50 + 2 x $20), and
	 * $1,010 is the nearest above it (17 x $50 + 8 x $20). The current
	 * CashValidator rejects $1,010 before a limit could be checked; its passing
	 * exception assertion is therefore masked. $1,020 (18 x $50 + 6 x $20) is an
	 * additional dispensable over-limit probe; Session currently accepts it.
	 */
	@Test
	public void acceptsDispensableAmountImmediatelyBelowDailyLimit() {
		assertDoesNotThrow(() -> session.setAmount(ACCOUNT.maximumWithdrawalBoundary() - 10));
	}

	@Test
	public void acceptsAmountAtDailyLimit() {
		assertDoesNotThrow(() -> session.setAmount(ACCOUNT.maximumWithdrawalBoundary()));
	}

	@Test
	public void rejectsNearestDispensableAmountAboveDailyLimit() {
		assertThrows(InvalidAmountException.class,
				() -> session.setAmount(ACCOUNT.maximumWithdrawalBoundary() + 10));
	}

	@Test
	public void rejectsDispensableAmountAboveDailyLimit() {
		assertThrows(InvalidAmountException.class,
				() -> session.setAmount(ACCOUNT.maximumWithdrawalBoundary() + 20));
	}

	/*
	 * Supplemental amount probes: $50 is one $50 bill, $60 is three $20 bills,
	 * and $70 is one $50 plus one $20 bill. They check combinations that the
	 * validator should accept. -$20 should be rejected as negative. The handout
	 * does not explicitly define a zero withdrawal, so rejecting $0 records the
	 * team assumption that withdrawals must be positive.
	 */
	@Test
	public void rejectsZeroWithdrawalAmount() {
		assertThrows(InvalidAmountException.class, () -> session.setAmount(0));
	}

	@Test
	public void rejectsNegativeWithdrawalAmount() {
		assertThrows(InvalidAmountException.class, () -> session.setAmount(-20));
	}

	@Test
	public void acceptsMinimumFiftyDollarBillAmount() {
		assertDoesNotThrow(() -> session.setAmount(50));
	}

	@Test
	public void acceptsAmountDispensableAsThreeTwentyDollarBills() {
		assertDoesNotThrow(() -> session.setAmount(60));
	}

	@Test
	public void acceptsAmountDispensableAsOneFiftyAndOneTwentyDollarBill() {
		assertDoesNotThrow(() -> session.setAmount(70));
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
