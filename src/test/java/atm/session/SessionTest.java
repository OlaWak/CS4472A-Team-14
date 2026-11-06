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

public class SessionTest {
	private static final String CARD = "4000000000000000";
	private static final String NOMINAL_PIN = "55555";
	private static final int SIMULATED_AVAILABLE_BALANCE = 5000;
	private static final int SIMULATED_DAILY_LIMIT = 1000;
	// Test-only model for deriving boundaries; Session cannot be given account limits.
	private static final SimulatedAccount ACCOUNT = new SimulatedAccount(
			SIMULATED_AVAILABLE_BALANCE, SIMULATED_DAILY_LIMIT);
	private MainPanel mainPanel;
	private MessageDispatcher dispatcher;
	private Session session;

	@BeforeEach
	public void setUp() {
		mainPanel = Mockito.mock(MainPanel.class);
		dispatcher = Mockito.mock(MessageDispatcher.class);
		Mockito.when(dispatcher.checkCredentials(Mockito.anyString(),
				Mockito.any(char[].class))).thenReturn(true);
		session = new Session(mainPanel, dispatcher);
		SessionState noOpState = currentSession -> {
			// Keep amount checks isolated from transaction processing.
		};
		session.setState(noOpState);
		session.setTransaction(new ATMWithdrawal());
		session.setFromAccount(AccountType.Chequing);
	}

	@Test
	public void rejectsPinOneDigitBelowRequiredLength() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'5', '5', '5', '5'}));
	}

	@Test
	public void acceptsPinAtRequiredLength() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt().checkPin(NOMINAL_PIN.toCharArray()));
	}

	@Test
	public void rejectsPinOneDigitAboveRequiredLength() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'5', '5', '5', '5', '5', '5'}));
	}

	@Test
	public void acceptsPinWithDigitAtLowerBoundary() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'0', '5', '5', '5', '5'}));
	}

	@Test
	public void acceptsPinWithDigitAdjacentToLowerBoundary() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'1', '5', '5', '5', '5'}));
	}

	@Test
	public void acceptsPinWithDigitAdjacentToUpperBoundary() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'8', '5', '5', '5', '5'}));
	}

	@Test
	public void acceptsPinWithDigitAtUpperBoundary() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'9', '5', '5', '5', '5'}));
	}

	@Test
	public void rejectsCharacterImmediatelyBelowDigitRange() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'/', '5', '5', '5', '5'}));
	}

	@Test
	public void rejectsCharacterImmediatelyAboveDigitRange() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {':', '5', '5', '5', '5'}));
	}

	@Test
	public void rejectsNonDigitInSecondPinPosition() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'5', '/', '5', '5', '5'}));
	}

	@Test
	public void rejectsNonDigitInThirdPinPosition() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'5', '5', ':', '5', '5'}));
	}

	@Test
	public void rejectsNonDigitInFourthPinPosition() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'5', '5', '5', '/', '5'}));
	}

	@Test
	public void rejectsCharacterImmediatelyBelowDigitRangeAtFifthPosition() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'5', '5', '5', '5', '/'}));
	}

	@Test
	public void rejectsCharacterImmediatelyAboveDigitRangeAtFifthPosition() throws Exception {
		assertThrows(InvalidPinFormatException.class,
				() -> createAtmAtPinPrompt().checkPin(new char[] {'5', '5', '5', '5', ':'}));
	}

	@Test
	public void acceptsPinWithZeroInFifthPosition() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'5', '5', '5', '5', '0'}));
	}

	@Test
	public void acceptsPinWithNineInFifthPosition() throws Exception {
		assertDoesNotThrow(() -> createAtmAtPinPrompt()
				.checkPin(new char[] {'5', '5', '5', '5', '9'}));
	}

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

	private ATM createAtmAtPinPrompt() throws InvalidCardNumberException {
		ATM atm = new ATM(new FormatChecker(), new CredentialsCheck(dispatcher), dispatcher);
		atm.setMainPanel(mainPanel);
		atm.createSession();
		atm.checkCardNumber(CARD);
		return atm;
	}

	private static final class SimulatedAccount {
		private final int availableBalance;
		private final int dailyLimit;

		private SimulatedAccount(int availableBalance, int dailyLimit) {
			this.availableBalance = availableBalance;
			this.dailyLimit = dailyLimit;
		}

		private int maximumWithdrawalBoundary() {
			return Math.min(availableBalance, dailyLimit);
		}
	}

}
