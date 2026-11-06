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


## Deposit Interest Fee Test

### Technique  - Weak Robust Equivalence Class Analysis

### Process and assumptions
The deposit calculation has three inputs: deposit amount, account balance and whether the client is a student.

In Weak Robust Equivalence Class Analysis, one representative is selected from
each valid equivalence class. The robust portion also includes one
representative from each invalid class while keeping the other inputs valid.

We treated both `true` and `false` as valid student values. 
We then divided the valid inputs using the conditions in the deposit specification. Student deposits are divided at $50, while non-students are divided at $250. Each amount group is divided again using its applicable balance condition.

These conditions produced eight valid equivalence classes, D1 - D8. We selected one representative value from each class as its representative test case. A tolerance of `0.000000001` is used when comparing `double` values to avoid failures caused only by floating-point representation.

Robust equivalence class analysis also considers invalid input classes.
However, the deposit specification does not identify negative amounts,
negative balances, or any other inputs as invalid, nor does it define the
expected behavior for such values. Therefore, no invalid equivalence classes
could be derived from the specification.

D9 and D10 were included separately as exploratory negative-input tests and
are not part of the specification-derived equivalence classes. These tests use
the explicit assumption that negative inputs should cause an `IllegalArgumentException`. Because this assumption is not a requirement of the deposit specification, their failures are recorded as exploratory observations and are not treated as confirmed faults.

### Deposit equivalence classes
Every case ID corresponds to one test in
`src/test/java/bank/utils/FeesCalculatorDepositInterestTest.java`.

| Case ID | Student | Amount class | Balance class | Representative test case | Expected rate | Expected result |
|---|---:|---|---|---|---:|---:|
| D1 | true | `amount > 50` | `balance > 500` | `(100, 1000, true)` | 0.5% | 0.50 |
| D2 | true | `amount > 50` | `balance <= 500` | `(100, 400, true)` | 0.25% | 0.25 |
| D3 | true | `amount <= 50` | `balance > 5000` | `(25, 6000, true)` | 0.5% | 0.125 |
| D4 | true | `amount <= 50` | `balance <= 5000` | `(25, 2000, true)` | 0% | 0.00 |
| D5 | false | `amount > 250` | `balance > 2500` | `(500, 5000, false)` | 0.8% | 4.00 |
| D6 | false | `amount > 250` | `balance <= 2500` | `(500, 1000, false)` | 0.4% | 2.00 |
| D7 | false | `amount <= 250` | `balance > 10000` | `(100, 20000, false)` | 0% | 0.00 |
| D8 | false | `amount <= 250` | `balance <= 10000` | `(100, 5000, false)` | 0.1% | 0.10 |

### Exploratory negative-input cases

| Case ID | Student | Amount class | Balance class | Assumed result |
|---|---:|---:|---:|---|
| D9 | true | -1 | 4000 (valid balance) | `IllegalArgumentException` |
| D10 | true | 150 (valid amount) | -1 | `IllegalArgumentException` |

### Deposit test results

The suite was executed using:

`mvn -B -Dmaven.test.failure.ignore=true test`

Maven reported 13 tests, 9 failures, 0 errors, and 0 skipped tests. Ten of the thirteen tests came from `FeesCalculatorDepositInterestTest`; the other three were existing repository tests. D3 passed. D1, D2, and D4–D8 failed against the specified deposit-fee behavior. D9 and D10 failed only under the stated exploratory assumption and are not treated as confirmed specification faults. The failure-tolerant run ended with `BUILD SUCCESS`, as required by the `phase:tests` workflow.

| ID | Expected result | Observed result | Status |
|---|---:|---:|---|
| D1 | 0.50 | 0.20 | Fail |
| D2 | 0.25 | 0.20 | Fail |
| D3 | 0.125 | 0.125 | Pass |
| D4 | 0.00 | 0.05 | Fail |
| D5 | 4.00 | 0.00 | Fail |
| D6 | 2.00 | 0.00 | Fail |
| D7 | 0.00 | 0.50 | Fail |
| D8 | 0.10 | 0.00 | Fail |
| D9 | Assumed: `IllegalArgumentException` | No exception thrown | Fail, Exploratory assumption not satisfied |
| D10 | Assumed: `IllegalArgumentException` | No exception thrown | Fail, Exploratory assumption not satisfied |


### Deposit fault localization

| Failing test | Suspected fault | Justification |
|---|---|---|
| `d1DepositFeeTestStudentHighAmountHighBalance` | `FeesCalculator.java`, `calculateDepositInterest`, Lines 39 and 50 | The `$100` deposit should enter the `amount > $50` class, but the `$100` limit at line 39 sends it to the low-amount branch, where the 0.2% rate produces 0.20 instead of 0.50. |
| `d2DepositFeeTestStudentHighAmountLowBalance` | `FeesCalculator.java`, `calculateDepositInterest`, Lines 39 and 50 | The `$100` deposit should enter the `amount > $50` class, but the `$100` limit at line 39 sends it to the low-amount branch, where the 0.2% rate produces 0.20 instead of 0.25. |
| `d4DepositFeeTestStudentLowAmountLowBalance` | `FeesCalculator.java`, `calculateDepositInterest`, Line 50 | The lower-balance interest rate is 0.2% instead of the spec required 0%, the fee is 0.05 instead of 0.00. |
| `d5DepositFeeTestNonStudentHighAmountHighBalance` | `FeesCalculator.java`, `calculateDepositInterest`, Lines 54 and 64 | The `$500` deposit should enter the `amount > $250` class, but the `$500` limit at line 54 sends it to the low-amount branch. Line 64 then applies a 0% rate, producing 0.00 instead of 4.00. |
| `d6DepositFeeTestNonStudentHighAmountLowBalance` | `FeesCalculator.java`, `calculateDepositInterest`, Lines 54 and 64 | The `$500` deposit should enter the `amount > $250` class, but the `$500` limit at line 54 sends it to the low-amount branch. Line 64 then applies a 0% rate, producing 0.00 instead of 2.00. |
| `d7DepositFeeTestNonStudentLowAmountHighBalance` | `FeesCalculator.java`, `calculateDepositInterest`, Line 62 | The high-balance interest rate is 0.5% instead of the spec required 0%, the fee resulting 0.50 instead of 0.00. |
| `d8DepositFeeTestNonStudentLowAmountLowBalance` | `FeesCalculator.java`, `calculateDepositInterest`, Line 64 | The lower-balance rate is 0% instead of 0.1% as required for spec, the fee is resulting 0.00 instead of 0.10. |
| `d9RejectNegativeDepositAmount` | No specification fault established | The test expected an exception based on an assumption, but the specification does not define behavior for negative deposit amounts. |
| `d10RejectNegativeBalance` | No specification fault established | The test expected an exception based on an assumption, but the specification does not define behavior for negative account balances. |
