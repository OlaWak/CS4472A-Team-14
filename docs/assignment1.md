# Assignment 1

## Transfer Fee Test Cases

**Technique:** decision table testing, applied to the transfer fee specification.

We started by reading the spec and pulling out every condition that can change
the fee. After merging the ones that say the same thing in different words, we
summarized them down to 7 conditions:

1. the client is a student
2. the amount transferred is less than $200
3. the amount transferred is less than $100
4. the balance of the account the money comes from is less than $2,000
5. the balance of the account the money comes from is less than $4,000
6. the balance of the account the money goes to is less than $1,000
7. the balance of the account the money goes to is less than $2,000

Going by the formula in the slides, 7 binary conditions would give us 2^7 = 128
rules. Most of those combinations can never happen or don't matter. For example,
the $200 amount limit only applies to students and the $100 limit only applies to
non-students, and an amount can't be both under $100 and not under $200 at once.
The balance limits also change depending on the client type and amount, so only
one "from" limit and one "to" limit are ever relevant for a given rule. Marking
those as "don't care" and dropping the impossible combinations lets us have only
16 rules.

Assumptions we made:

- Fees are a percentage of the amount transferred, so the expected fee in each
  test is the percentage times the amount.
- Each rule gets one representative test case, and the inputs are chosen so
  they sit right at the limits where possible.
- A double tolerance of 1e-9 is used when comparing fees, to ignore tiny
  floating point differences.

The decision table we built from the spec:

![Transfer fee decision table](../images/transfer-fee-decision-table.png)

Every rule R1 to R16 in the table has one matching test in
`src/test/java/bank/utils/TransferFeeDecisionTableTest.java`, named after the
rule (for example `r1StudentBelow200BothBalancesLow`).

Here is the table for each test:
| Failing test | Suspected fault (file, method, line) | One-line justification |
|---|---|---|
| r1StudentBelow200BothBalancesLow | FeesCalculator.java, calculateTransferFee, L80 (and L81, L103) | Student amount limit is `< 100` instead of `< 200`, and the percentage is too large: a $100 transfer returns fee 2.5 instead of 0.1. |
| r2StudentBelow200FromLowToHigh | FeesCalculator.java, calculateTransferFee, L80 (and L81, L105) | Same wrong amount limit and "from" limit (`< 1000` instead of `< 2000`), so the fee is 1.25 instead of 0.05. |
| r3StudentBelow200FromHighToLow | FeesCalculator.java, calculateTransferFee, L80 (and L102, L103) | $100 falls into the wrong amount group and the percentage is too large: 2.5 instead of 0.05. |
| r4StudentBelow200BothBalancesHigh | FeesCalculator.java, calculateTransferFee, L80 (and L105) | $100 falls into the wrong amount group: 1.25 instead of 0.025. |
| r5StudentAt200BothBalancesLow | FeesCalculator.java, calculateTransferFee, L81 (and L98, L99, L103) | The "from" limit is `< 1000` instead of `< 2000`, and the percentage is far too large: 5.0 instead of 0.1. |
| r6StudentAt200FromLowToHigh | FeesCalculator.java, calculateTransferFee, L95 (and L105) | Wrong "from" limit (`< 1000` instead of `< 2000`) and wrong percentage: 2.5 instead of 0.05. |
| r7StudentAt200FromHighToLow | FeesCalculator.java, calculateTransferFee, L102-L103 | The percentage is wrong: 5.0 instead of 0.5. |
| r8StudentAt200BothBalancesHigh | FeesCalculator.java, calculateTransferFee, L104-L105 | The percentage is wrong: 2.5 instead of 0.25. |
| r9NonStudentBelow100BothBalancesLow | FeesCalculator.java, calculateTransferFee, L111-L112 (and L121) | "From" limit is `< 1000` instead of `< 4000` and "to" limit is `< 1000` instead of `< 2000`, so it lands in the high/high branch: 2.5 instead of 0.1. |
| r10NonStudentBelow100FromLowToHigh | FeesCalculator.java, calculateTransferFee, L111 (and L121) | "From" limit `< 1000` should be `< 4000`, so a $2,000 balance wrongly goes to the high branch: 2.5 instead of 0.05. |
| r11NonStudentBelow100FromHighToLow | FeesCalculator.java, calculateTransferFee, L112, L118 (and L121) | "To" limit `< 1000` should be `< 2000`, so a $1,000 balance is treated as high: 2.5 instead of 0.5. |
| r12NonStudentBelow100BothBalancesHigh | FeesCalculator.java, calculateTransferFee, L118-L121 | The percentage for the high/high case is wrong: 2.5 instead of 0.25. |
| r13NonStudentAt100BothBalancesLow | FeesCalculator.java, calculateTransferFee, L125 (and L131, L133) | "From" limit is `< 1000` instead of `< 2000`, so a $1,000 balance is treated as high: 5.0 instead of 0.2. |
| r14NonStudentAt100FromLowToHigh | FeesCalculator.java, calculateTransferFee, L135 | The high/high percentage `0.055` is wrong, and the "from" limit is also wrong: 5.5 instead of 0.1. |
| r15NonStudentAt100FromHighToLow | FeesCalculator.java, calculateTransferFee, L132-L133 | The percentage is wrong: 5.0 instead of 0.5. |
| r16NonStudentAt100BothBalancesHigh | FeesCalculator.java, calculateTransferFee, L134-L135 | The percentage `0.055` is wrong: 5.5 instead of 0.25. |

## ATM Session (`atm.session.Session`) test cases and results

**Contributor:** Sonia Sharma

### 1. Process and assumptions

The ATM Session tests use Robust Worst Case Boundary Value Analysis under the single-fault assumption: one input characteristic is varied at a time while the others use nominal values. The model has three characteristics: PIN length, one PIN digit, and withdrawal amount. The nominal inputs are PIN `24681` and withdrawal `$500`.

For weak robust, one-factor-at-a-time BVA, the usual `6n + 1` count is one nominal combination plus six boundary values per input characteristic. PIN length is an exact-length condition (`min = max = 5`), so it has only two distinct non-nominal values: 4 and 6 characters. A PIN digit ranges from `0` through `9`; its six robust boundary values are `/`, `0`, `1`, `8`, `9`, and `:` with the other positions held at their nominal values from `24681`. For withdrawal amount, the six selected boundary values are `$19`, `$20`, `$21`, `$990`, `$1,000`, and `$1,010`. This gives `1 + 2 + 6 + 6 = 15` core input combinations. The nominal PIN and nominal withdrawal are asserted in separate JUnit tests to keep the PIN-format and amount paths independently observable. Additional position and denomination probes are listed separately below.

The amount domain is constrained by the console ATM's `$20` and `$50` bills. `$20` is the minimum positive dispensable amount. `$40` is the next dispensable amount above it. Around the `$1,000` upper boundary, `$990` is the nearest dispensable amount below it and `$1,010` is the nearest dispensable amount above it (`17 × $50 + 8 × $20`). `$1,020` is included as an additional over-limit amount that the current denomination validator accepts, so it can expose the missing limit check. The `$1,010` case is expected to be rejected for exceeding the limit, but the current `CashValidator` also rejects it incorrectly, so a passing assertion there does not prove that the limit was checked.

Additional amount probes test `$40`, `$50`, `$60`, and `$70` bill combinations, `-$20`, and `$0`. The specification does not explicitly say whether a zero-dollar withdrawal is valid; this test assumes a withdrawal must be positive. PIN digit cases also place invalid characters in positions two through five and valid boundary digits in the fifth position, rather than assuming only the first position matters.

PIN format is checked through `ATM.checkPin()`, which invokes `FormatChecker.checkPinFormat()` before calling `Session.addPin()`. `Session.addPin()` stores the PIN and advances the state; it does not validate the format. The helper uses the format-valid test card `4111111111111111` to reach the PIN prompt, and credentials are mocked so no bank lookup is needed. Amount tests construct a `Session` with an `ATMWithdrawal`, select a Chequing account, and use a no-op state so `Session.setAmount()` validation can be tested without performing a bank transaction. This keeps the amount cases independent of the PIN-format fault, but it does not exercise transaction processing or account balance handling.

`SessionTest` defines a test-only `SimulatedAccount` with `$5,000` available and a `$1,000` daily limit. The maximum withdrawal boundary is calculated as `min($5,000, $1,000) = $1,000`, and the tests derive their upper-boundary inputs from that value. This model is not attached to `Session` or stored in the bank database; amount tests still use a no-op state. `DBHandler.setBalance()` can set a stored balance, but these tests do not call it. The application has no daily-limit field or setup path, so the fixture represents the required values for boundary selection but does not exercise an account with them. The database seed PIN is `5555` (four digits); PIN tests use mocked credentials and test PINs so they can check the assignment's five-digit format independently. Instructor guidance is still needed on the missing production daily-limit support.

For `$1,020`, the test expects `InvalidAmountException` because the assignment identifies `$1,000` as the withdrawal upper boundary. This is our interpretation of the exception for a cash-dispensable amount above the daily limit; the handout does not state that outcome explicitly. No production code was changed.

### 2. Test cases

#### PIN format cases

| Test case | Input | Expected result |
|---|---|---|
| `rejectsPinOneDigitBelowRequiredLength` | `2468` | Throw `InvalidPinFormatException` |
| `acceptsPinAtRequiredLength` | `24681` | Accept |
| `rejectsPinOneDigitAboveRequiredLength` | `246810` | Throw `InvalidPinFormatException` |
| `acceptsPinWithDigitAtLowerBoundary` | `04681` | Accept |
| `acceptsPinWithDigitAdjacentToLowerBoundary` | `14681` | Accept |
| `acceptsPinWithDigitAdjacentToUpperBoundary` | `84681` | Accept |
| `acceptsPinWithDigitAtUpperBoundary` | `94681` | Accept |
| `rejectsCharacterImmediatelyBelowDigitRange` | `/4681` | Throw `InvalidPinFormatException` |
| `rejectsCharacterImmediatelyAboveDigitRange` | `:4681` | Throw `InvalidPinFormatException` |
| `rejectsNonDigitInSecondPinPosition` | `2/681` | Throw `InvalidPinFormatException` |
| `rejectsNonDigitInThirdPinPosition` | `24:81` | Throw `InvalidPinFormatException` |
| `rejectsNonDigitInFourthPinPosition` | `246/1` | Throw `InvalidPinFormatException` |
| `rejectsCharacterImmediatelyBelowDigitRangeAtFifthPosition` | `2468/` | Throw `InvalidPinFormatException` |
| `rejectsCharacterImmediatelyAboveDigitRangeAtFifthPosition` | `2468:` | Throw `InvalidPinFormatException` |
| `acceptsPinWithZeroInFifthPosition` | `24680` | Accept |
| `acceptsPinWithNineInFifthPosition` | `24689` | Accept |

#### Withdrawal amount cases

| Test case | Amount | Expected result |
|---|---:|---|
| `rejectsAmountOneDollarBelowMinimumDispensableAmount` | `$19` | Throw `InvalidAmountException` |
| `acceptsMinimumDispensableAmount` | `$20` | Accept |
| `rejectsAmountOneDollarAboveMinimumDispensableAmount` | `$21` | Throw `InvalidAmountException` |
| `acceptsNextDispensableAmountAboveMinimum` | `$40` | Accept |
| `acceptsNominalWithdrawalAmount` | `$500` | Accept |
| `acceptsDispensableAmountImmediatelyBelowDailyLimit` | `$990` | Accept |
| `acceptsAmountAtDailyLimit` | `$1,000` | Accept |
| `rejectsNearestDispensableAmountAboveDailyLimit` | `$1,010` | Throw `InvalidAmountException` |
| `rejectsDispensableAmountAboveDailyLimit` | `$1,020` | Throw `InvalidAmountException` |
| `rejectsZeroWithdrawalAmount` | `$0` | Throw `InvalidAmountException` (team assumption: withdrawals must be positive) |
| `rejectsNegativeWithdrawalAmount` | `-$20` | Throw `InvalidAmountException` |
| `acceptsMinimumFiftyDollarBillAmount` | `$50` | Accept |
| `acceptsAmountDispensableAsThreeTwentyDollarBills` | `$60` | Accept |
| `acceptsAmountDispensableAsOneFiftyAndOneTwentyDollarBill` | `$70` | Accept |

#### Test results

Full-suite run: `mvn -B test`, 2026-11-06. Checkstyle reported zero violations. Maven ran 49 tests: 28 failed and 21 passed. The ATM Session class ran 30 tests: 12 failed and 18 passed. The 16 Transfer Fee failures are from the separate Transfer Fee tests; the table below reports the ATM Session cases only.

The latest PR CI run, after the test card and nominal PIN values were updated, reported 49 tests overall (21 passed, 28 failed), including 30 ATM Session tests (18 passed, 12 failed). The ATM Session statuses in the table below match that run. Its JaCoCo coverage is project-wide: 12.0% line, 18.1% branch, and 25.8% method coverage. The 16 failing `TransferFeeDecisionTableTest` cases belong to the separate Transfer Fee target and are not included in this section's fault localization.

| Test case | Observed result | Status / notes |
|---|---|---|
| `rejectsPinOneDigitBelowRequiredLength` | Accepted | **Fail.** Four-character PIN accepted. |
| `acceptsPinAtRequiredLength` | Threw `InvalidPinFormatException` | **Fail.** Five-digit PIN rejected. |
| `rejectsPinOneDigitAboveRequiredLength` | Threw `InvalidPinFormatException` | **Pass.** |
| `acceptsPinWithDigitAtLowerBoundary` | Threw `InvalidPinFormatException` | **Fail.** Five-character PIN rejected by the length check. |
| `acceptsPinWithDigitAdjacentToLowerBoundary` | Threw `InvalidPinFormatException` | **Fail.** Five-character PIN rejected by the length check. |
| `acceptsPinWithDigitAdjacentToUpperBoundary` | Threw `InvalidPinFormatException` | **Fail.** Five-character PIN rejected by the length check. |
| `acceptsPinWithDigitAtUpperBoundary` | Threw `InvalidPinFormatException` | **Fail.** Five-character PIN rejected by the length check. |
| `rejectsCharacterImmediatelyBelowDigitRange` | Threw `InvalidPinFormatException` | **Pass, masked.** The five-character PIN is rejected by the length check before `/` is independently checked. |
| `rejectsCharacterImmediatelyAboveDigitRange` | Threw `InvalidPinFormatException` | **Pass, masked.** The five-character PIN is rejected by the length check before `:` is independently checked. |
| `rejectsNonDigitInSecondPinPosition` | Threw `InvalidPinFormatException` | **Pass, masked.** Rejection occurs at the length check. |
| `rejectsNonDigitInThirdPinPosition` | Threw `InvalidPinFormatException` | **Pass, masked.** Rejection occurs at the length check. |
| `rejectsNonDigitInFourthPinPosition` | Threw `InvalidPinFormatException` | **Pass, masked.** Rejection occurs at the length check. |
| `rejectsCharacterImmediatelyBelowDigitRangeAtFifthPosition` | Threw `InvalidPinFormatException` | **Pass, masked.** Rejection occurs at the length check. |
| `rejectsCharacterImmediatelyAboveDigitRangeAtFifthPosition` | Threw `InvalidPinFormatException` | **Pass, masked.** Rejection occurs at the length check. |
| `acceptsPinWithZeroInFifthPosition` | Threw `InvalidPinFormatException` | **Fail.** Five-character PIN rejected by the length check. |
| `acceptsPinWithNineInFifthPosition` | Threw `InvalidPinFormatException` | **Fail.** Five-character PIN rejected by the length check. |
| `rejectsAmountOneDollarBelowMinimumDispensableAmount` | Threw `InvalidAmountException` | **Pass.** |
| `acceptsMinimumDispensableAmount` | Accepted | **Pass.** |
| `rejectsAmountOneDollarAboveMinimumDispensableAmount` | Threw `InvalidAmountException` | **Pass.** |
| `acceptsNextDispensableAmountAboveMinimum` | Accepted | **Pass.** |
| `acceptsNominalWithdrawalAmount` | Accepted | **Pass.** |
| `acceptsDispensableAmountImmediatelyBelowDailyLimit` | Accepted | **Pass.** |
| `acceptsAmountAtDailyLimit` | Accepted | **Pass.** |
| `rejectsNearestDispensableAmountAboveDailyLimit` | Threw `InvalidAmountException` | **Pass, masked.** `CashValidator` rejects `$1,010` even though it can be dispensed; this result does not show that the daily limit was checked. |
| `rejectsDispensableAmountAboveDailyLimit` | Accepted | **Fail.** `Session.setAmount()` accepts `$1,020` in the isolated no-op-state test; no daily-limit check rejects it. |
| `rejectsZeroWithdrawalAmount` | Accepted | **Fail under the stated positive-withdrawal assumption.** `CashValidator` accepts zero. |
| `rejectsNegativeWithdrawalAmount` | Accepted | **Fail.** `CashValidator` accepts `-$20`. |
| `acceptsMinimumFiftyDollarBillAmount` | Accepted | **Pass.** |
| `acceptsAmountDispensableAsThreeTwentyDollarBills` | Threw `InvalidAmountException` | **Fail.** `CashValidator` rejects `$60`, although it can be dispensed as three `$20` bills. |
| `acceptsAmountDispensableAsOneFiftyAndOneTwentyDollarBill` | Accepted | **Pass.** |

#### Fault localization

The failures below are grouped by suspected fault. Copy these entries into the phase:tests fault-localization table in the PR description.

| Failing test case(s) | Suspected fault (file, method, condition) | One-sentence justification |
|---|---|---|
| `rejectsPinOneDigitBelowRequiredLength`; `acceptsPinAtRequiredLength`; `acceptsPinWithDigitAtLowerBoundary`; `acceptsPinWithDigitAdjacentToLowerBoundary`; `acceptsPinWithDigitAdjacentToUpperBoundary`; `acceptsPinWithDigitAtUpperBoundary`; `acceptsPinWithZeroInFifthPosition`; `acceptsPinWithNineInFifthPosition` | `src/main/java/atm/utils/FormatChecker.java`, `checkPinFormat()`, line 33: `pin.length == 4` | The implementation accepts four-character PINs and rejects the required five-character PINs; valid-digit boundary failures are caused by the length check before digit validation. |
| `rejectsDispensableAmountAboveDailyLimit` | Missing daily-limit check in the `atm.session.Session.setAmount()` withdrawal path | The session accepts `$1,020` even though it exceeds the assignment's `$1,000` upper boundary; `InvalidAmountException` for this limit violation is the documented interpretation pending instructor confirmation. |
| `rejectsNegativeWithdrawalAmount`; `rejectsZeroWithdrawalAmount` | `src/main/java/atm/utils/CashValidator.java`, `validateWithdrawal()`, missing positive-amount check | The validator returns true for `-$20` and `$0`, so `Session.setAmount()` accepts them; the zero case uses the documented positive-withdrawal assumption. |
| `acceptsAmountDispensableAsThreeTwentyDollarBills` | `src/main/java/atm/utils/CashValidator.java`, `validateWithdrawal()`, remainder checks | The validator rejects `$60` even though three `$20` bills can dispense it. |

