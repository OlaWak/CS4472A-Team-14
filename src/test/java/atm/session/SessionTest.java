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
	// Assignment boundary; Session has no API to configure an account's daily limit.
	private static final int DAILY_LIMIT = 1000;
	private MainPanel mainPanel;
	private MessageDispatcher dispatcher;
	private Session session;

	@BeforeEach
	public void setUp() {
		mainPanel = Mockito.mock(MainPanel.class);
		dispatcher = Mockito.mock(MessageDispatcher.class);
		session = new Session(mainPanel, dispatcher);

		// Keep amount tests focused on Session.setAmount input validation.
		SessionState noOpState = currentSession -> {
			// The test does not perform a bank transaction.
		};
		session.setState(noOpState);
		session.setTransaction(new ATMWithdrawal());
		session.setFromAccount(AccountType.Chequing);
	}

	@Test
	public void rejectsPinOneDigitBelowRequiredLength() throws Exception {
		ATM atm = createAtm();

		assertThrows(InvalidPinFormatException.class,
				() -> atm.checkPin(new char[] {'1', '2', '3', '4'}));
	}

	@Test
	public void acceptsPinAtRequiredLength() throws Exception {
		ATM atm = createAtm();

		assertDoesNotThrow(() -> atm.checkPin(new char[] {'1', '2', '3', '4', '5'}));
	}

	@Test
	public void rejectsPinOneDigitAboveRequiredLength() throws Exception {
		ATM atm = createAtm();

		assertThrows(InvalidPinFormatException.class,
				() -> atm.checkPin(new char[] {'1', '2', '3', '4', '5', '6'}));
	}

	@Test
	public void acceptsPinWithDigitAtLowerBoundary() throws Exception {
		ATM atm = createAtm();

		assertDoesNotThrow(() -> atm.checkPin(new char[] {'0', '1', '2', '3', '4'}));
	}

	@Test
	public void acceptsPinWithDigitAdjacentToLowerBoundary() throws Exception {
		ATM atm = createAtm();

		assertDoesNotThrow(() -> atm.checkPin(new char[] {'1', '1', '2', '3', '4'}));
	}

	@Test
	public void acceptsPinWithDigitAdjacentToUpperBoundary() throws Exception {
		ATM atm = createAtm();

		assertDoesNotThrow(() -> atm.checkPin(new char[] {'8', '1', '2', '3', '4'}));
	}

	@Test
	public void acceptsPinWithDigitAtUpperBoundary() throws Exception {
		ATM atm = createAtm();

		assertDoesNotThrow(() -> atm.checkPin(new char[] {'9', '1', '2', '3', '4'}));
	}

	@Test
	public void rejectsCharacterImmediatelyBelowDigitRange() throws Exception {
		ATM atm = createAtm();

		assertThrows(InvalidPinFormatException.class,
				() -> atm.checkPin(new char[] {'/', '1', '2', '3', '4', '5'}));
	}

	@Test
	public void rejectsCharacterImmediatelyAboveDigitRange() throws Exception {
		ATM atm = createAtm();

		assertThrows(InvalidPinFormatException.class,
				() -> atm.checkPin(new char[] {':', '1', '2', '3', '4', '5'}));
	}

	@Test
	public void rejectsAmountOneDollarBelowMinimumDispensableAmount() {
		assertThrows(InvalidAmountException.class, () -> session.setAmount(19));
	}

	@Test
	public void rejectsNegativeWithdrawalAmount() {
		assertThrows(InvalidAmountException.class, () -> session.setAmount(-20));
	}

	@Test
	public void acceptsMinimumDispensableAmount() {
		assertDoesNotThrow(() -> session.setAmount(20));
	}

	@Test
	public void acceptsAmountDispensableAsThreeTwentyDollarBills() {
		assertDoesNotThrow(() -> session.setAmount(60));
	}

	@Test
	public void rejectsAmountOneDollarAboveMinimumDispensableAmount() {
		assertThrows(InvalidAmountException.class, () -> session.setAmount(21));
	}

	@Test
	public void acceptsNominalWithdrawalAmount() {
		assertDoesNotThrow(() -> session.setAmount(500));
	}

	@Test
	public void acceptsDispensableAmountImmediatelyBelowDailyLimit() {
		assertDoesNotThrow(() -> session.setAmount(990));
	}

	@Test
	public void acceptsAmountAtDailyLimit() {
		assertDoesNotThrow(() -> session.setAmount(DAILY_LIMIT));
	}

	@Test
	public void rejectsDispensableAmountImmediatelyAboveDailyLimit() {
		// The assignment calls $1,000 the upper boundary. Treat an over-limit
		// amount as invalid input; this isolated Session test cannot configure an account.
		assertThrows(InvalidAmountException.class,
				() -> session.setAmount(DAILY_LIMIT + 20));
	}

	private ATM createAtm() throws InvalidCardNumberException {
		ATM atm = new ATM(new FormatChecker(), new CredentialsCheck(dispatcher), dispatcher);
		atm.setMainPanel(mainPanel);
		atm.createSession();
		atm.checkCardNumber("4000000000000000");
		Mockito.when(dispatcher.checkCredentials(
				Mockito.anyString(), Mockito.any(char[].class))).thenReturn(true);
		return atm;
	}
}
