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

