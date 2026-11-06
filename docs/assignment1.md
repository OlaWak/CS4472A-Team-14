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
  - This results in 17 test values for Bank Account Balance, not 7 (7*3 - 4, as there is overlap between the min+1 and max-1 values of the internal thresholds)
- Boundary value analysis is not suitable for testing boolean variables, as they cannot take on the 5-7 values necessary. Instead of creating min-1, max+1, etc. values for the client's Student Account Status, the tests can only test whether the client is or isn't a student (2 test values).

Therefore, the number of test cases is equal to # of test values for account balance * # of test values for withdrawal amount * # test values for student status * # test values for withdrawal date, OR 17*7*2*7 = 1666

**Test Case Table**