## Withdrawal Fee Test Cases

**Technique:** robust worst-case boundary value analysis, applied to the withdrawal fee specification.

We started by reading the spec and determining variables that affect the withdrawl fee calculation. These variables are as follows:

1. Client Student Account Status (Student, !Student)
2. Client Bank Account Balance 
    - Bank Account Balance < $1000
    - Bank Account Balance >= $1000, and Bank Account Balance < $5000
    - Bank Account Balance >= $5000
3. Date of Withdrawal (Weekday, Weekend)
   - Can also be represented by a range: 2-6 (Monday - Friday) = Weekday, 1 or 7 (Sunday or Saturday) = Weekend 
4. The Amount Withdrawn 

**Assumptions**
- The client's bank account balance is at least 0 and at max 1,000,000 for the purposes of testing
- The calculated withdrawal fees do NOT include overdraft fees (if they exist) or any other fee related to over-withdrawal from the account (eg. withdrawing 100 when you only have 50 in your account)

**Test Case Decisions**

Robust Worst Case Boundary Value Analysis usually results in 7^n (n=number of variables) test cases, the 7 coming from the different boundary values per variable to test (min-1, min, min+1, normative, max-1, max, max+1). Though we have 4 input variables for Withdrawal Fees, the number of tests we conducted did not add up to 7^4. This is because of the following:

- Client Bank Account Balance has thresholds within its overall minimum/maximum boundaries. As such, to adequately test the robust values for the three internal thresholds, additional test values are needed. 
  - This results in 10 test values for Bank Account Balance, not 7
- Boolean variables cannot take on the 5-7 values necessary for BVA. Instead of creating min-1, max+1, etc. values for the client's Student Account Status, the tests can only test whether the client is or isn't a student (2 test values) and whether the withdrawal happens on a weekday or weekend (2 test values).

Therefore, the number of test cases is equal to # of test values for account balance * # of test values for withdrawal amount * # test values for student status * # test values for withdrawal date, OR 10*7*2*2 = 280

**Test Case Table**
See withdrawal_fee_test_cases.csv for the test cases.

**Test Results**
In total, of the 280 tests there were 230 failures and 50 successes. A summarized version of the test results separated into distinct classes based on their suspected faults and the fault localization for each class are as shown:

| Failing test | Suspected fault (file, method, line) | One-line justification |
|---|---|---|
| `validInputs`: student, day ∈ {MON, WED, FRI}, any balance | `FeesCalculator.java`, `calculateWithdrawalFee`, line 15 | Spec says a student pays no fees on a weekday, but the weekday branch assigns 0.001 as the percentage, not 0 resulting in an incorrect fee result. |
| `validInputs`: student, day ∈ {SUN, SAT}, any balance| `FeesCalculator.java`, `calculateWithdrawalFee`, line 13 | Spec says a student pays 0.1% on the weekend, but the result is 0% (expected 1.0, was 0.0): the weekend branch assigns 0.0. Together with the row above, the outcomes of the weekend check on line 12 are swapped. |
| `validInputs`: non-student, balance ∈ {0, 1, 500, 998, 999}, any valid day | `FeesCalculator.java`, `calculateWithdrawalFee`, line 19 | Spec says 0.3% below $1,000, but the code uses 0.002. Balance 1000 passes, so the line 18 boundary is correct and only the rate is wrong. |
| `validInputs`: non-student, balance ∈ {5000, 5001}, any valid day | `FeesCalculator.java`, `calculateWithdrawalFee`, line 20 | Spec says no fee from $5,000, but the code's upper threshold is `< 10000.00`. |
| `invalidInputs`: balance ∈ {-1, 1,000,001} or day ∈ {0, 8}, all combinations | `FeesCalculator.java`, `calculateWithdrawalFee`, method entry (lines 6-8) | The method has no input validation, so no `IllegalArgumentException` is thrown--the expected exception is our assumption. |


The JaCoCo coverage statistics are as follows:
Type	Coverage
Line	5.9%
Branch	9.3%
Method	13.8%