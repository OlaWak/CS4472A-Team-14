# Assignment 1 Failure Log

**Contributor:** Sonia Sharma

This log is retained for use in Assignment 3. Times are rough estimates based on the Maven output and timestamps available from the runs; focused diagnosis time was not timed separately.

| Failure ID | Test case(s) exposing it | Failure found / test time before detection | Rough diagnosis time | Fault and notes |
|---|---|---|---|---|
| F-01 | `rejectsPinOneDigitBelowRequiredLength`; `acceptsPinAtRequiredLength`; `acceptsPinWithDigitAtLowerBoundary`; `acceptsPinWithDigitAdjacentToLowerBoundary`; `acceptsPinWithDigitAdjacentToUpperBoundary`; `acceptsPinWithDigitAtUpperBoundary` | Confirmed in the case-separated run on 2026-11-03 at 21:12. The Maven run took 3.919 seconds; all failures were reported at the end of that run. | About 12 minutes elapsed from the first grouped failure report at 21:00 to the case-separated rerun at 21:12. This includes reviewing the report and separating cases; focused diagnosis time was not measured. | `FormatChecker.checkPinFormat()` checks for a four-character PIN, while the specification requires five digits. The digit-boundary failures are masked by this length fault and do not independently prove the digit comparisons are wrong. |
| F-02 | `rejectsDispensableAmountImmediatelyAboveDailyLimit` | Confirmed in the case-separated run on 2026-11-03 at 21:12. The Maven run took 3.919 seconds; the failure was reported at the end of that run. | About 12 minutes elapsed from the first grouped failure report at 21:00 to the case-separated rerun at 21:12. This includes reviewing the report and separating cases; focused diagnosis time was not measured. | `Session.setAmount()` accepts the cash-dispensable `$1,020` amount in this test path, despite the assignment's `$1,000` upper boundary. The test does not configure the required `$5,000` account balance or a daily-limit account because this codebase exposes no daily-limit setup API. |
| F-03 | `rejectsNegativeWithdrawalAmount` | Found in the run on 2026-11-05 at 21:29. The Maven run took 5.011 seconds; the failure was reported at the end of that run. | Roughly 2 minutes reviewing the failure output and `CashValidator.validateWithdrawal()`; not timed contemporaneously, so this is an estimate. | `CashValidator.validateWithdrawal()` accepts `-$20`: the negative remainder is then divisible by 20, so the method returns `true` instead of causing `Session.setAmount()` to throw `InvalidAmountException`. |
| F-04 | `acceptsAmountDispensableAsThreeTwentyDollarBills` | Found in the run on 2026-11-05 at 21:29. The Maven run took 5.011 seconds; the failure was reported at the end of that run. | Roughly 2 minutes reviewing the failure output and `CashValidator.validateWithdrawal()`; not timed contemporaneously, so this is an estimate. | `CashValidator.validateWithdrawal()` rejects `$60`, although three `$20` bills can dispense it; checking the remainder first against 50 and then 20 does not recognize every valid combination. |

## Run record

| Run | Result | Notes |
|---|---|---|
| 2026-11-03 21:00 | 4 tests, 3 failures; Maven reported 7.120 seconds | Grouped assertions made some PIN results inconclusive. |
| 2026-11-03 21:12 | 16 tests, 7 failures, 9 passes; Maven reported 3.919 seconds | Cases were split into independent JUnit tests. This run is the source of the result table in `assignment1.md`. |
| 2026-11-05 21:29 | 18 tests, 9 failures, 9 passes; Maven reported 5.011 seconds | Added negative and valid mixed-bill amount cases; this run is the current source of the result table in `assignment1.md`. Checkstyle reported zero violations. |

