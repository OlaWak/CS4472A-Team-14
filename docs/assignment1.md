# Assignment 1

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

Table A: all inputs valid (180 test cases)
 
| Test case | Balance ($) | Amount ($) | Student | Day type | Spec rule | Expected rate | Expected fee ($) |
|---:|---:|---:|:---:|:---:|:---:|:---:|---:|
| 1 | 0 | 1 | Yes | Weekday | S1 | 0% | 0.000 |
| 2 | 0 | 1 | No | Weekday | S3 | 0.3% | 0.003 |
| 3 | 0 | 1 | Yes | Weekend | S2 | 0.1% | 0.001 |
| 4 | 0 | 1 | No | Weekend | S3 | 0.3% | 0.003 |
| 5 | 0 | 2 | Yes | Weekday | S1 | 0% | 0.000 |
| 6 | 0 | 2 | No | Weekday | S3 | 0.3% | 0.006 |
| 7 | 0 | 2 | Yes | Weekend | S2 | 0.1% | 0.002 |
| 8 | 0 | 2 | No | Weekend | S3 | 0.3% | 0.006 |
| 9 | 0 | 1,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 10 | 0 | 1,000 | No | Weekday | S3 | 0.3% | 3.000 |
| 11 | 0 | 1,000 | Yes | Weekend | S2 | 0.1% | 1.000 |
| 12 | 0 | 1,000 | No | Weekend | S3 | 0.3% | 3.000 |
| 13 | 0 | 999,999 | Yes | Weekday | S1 | 0% | 0.000 |
| 14 | 0 | 999,999 | No | Weekday | S3 | 0.3% | 2,999.997 |
| 15 | 0 | 999,999 | Yes | Weekend | S2 | 0.1% | 999.999 |
| 16 | 0 | 999,999 | No | Weekend | S3 | 0.3% | 2,999.997 |
| 17 | 0 | 1,000,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 18 | 0 | 1,000,000 | No | Weekday | S3 | 0.3% | 3,000.000 |
| 19 | 0 | 1,000,000 | Yes | Weekend | S2 | 0.1% | 1,000.000 |
| 20 | 0 | 1,000,000 | No | Weekend | S3 | 0.3% | 3,000.000 |
| 21 | 999 | 1 | Yes | Weekday | S1 | 0% | 0.000 |
| 22 | 999 | 1 | No | Weekday | S3 | 0.3% | 0.003 |
| 23 | 999 | 1 | Yes | Weekend | S2 | 0.1% | 0.001 |
| 24 | 999 | 1 | No | Weekend | S3 | 0.3% | 0.003 |
| 25 | 999 | 2 | Yes | Weekday | S1 | 0% | 0.000 |
| 26 | 999 | 2 | No | Weekday | S3 | 0.3% | 0.006 |
| 27 | 999 | 2 | Yes | Weekend | S2 | 0.1% | 0.002 |
| 28 | 999 | 2 | No | Weekend | S3 | 0.3% | 0.006 |
| 29 | 999 | 1,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 30 | 999 | 1,000 | No | Weekday | S3 | 0.3% | 3.000 |
| 31 | 999 | 1,000 | Yes | Weekend | S2 | 0.1% | 1.000 |
| 32 | 999 | 1,000 | No | Weekend | S3 | 0.3% | 3.000 |
| 33 | 999 | 999,999 | Yes | Weekday | S1 | 0% | 0.000 |
| 34 | 999 | 999,999 | No | Weekday | S3 | 0.3% | 2,999.997 |
| 35 | 999 | 999,999 | Yes | Weekend | S2 | 0.1% | 999.999 |
| 36 | 999 | 999,999 | No | Weekend | S3 | 0.3% | 2,999.997 |
| 37 | 999 | 1,000,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 38 | 999 | 1,000,000 | No | Weekday | S3 | 0.3% | 3,000.000 |
| 39 | 999 | 1,000,000 | Yes | Weekend | S2 | 0.1% | 1,000.000 |
| 40 | 999 | 1,000,000 | No | Weekend | S3 | 0.3% | 3,000.000 |
| 41 | 1,000 | 1 | Yes | Weekday | S1 | 0% | 0.000 |
| 42 | 1,000 | 1 | No | Weekday | S4 | 0.1% | 0.001 |
| 43 | 1,000 | 1 | Yes | Weekend | S2 | 0.1% | 0.001 |
| 44 | 1,000 | 1 | No | Weekend | S4 | 0.1% | 0.001 |
| 45 | 1,000 | 2 | Yes | Weekday | S1 | 0% | 0.000 |
| 46 | 1,000 | 2 | No | Weekday | S4 | 0.1% | 0.002 |
| 47 | 1,000 | 2 | Yes | Weekend | S2 | 0.1% | 0.002 |
| 48 | 1,000 | 2 | No | Weekend | S4 | 0.1% | 0.002 |
| 49 | 1,000 | 1,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 50 | 1,000 | 1,000 | No | Weekday | S4 | 0.1% | 1.000 |
| 51 | 1,000 | 1,000 | Yes | Weekend | S2 | 0.1% | 1.000 |
| 52 | 1,000 | 1,000 | No | Weekend | S4 | 0.1% | 1.000 |
| 53 | 1,000 | 999,999 | Yes | Weekday | S1 | 0% | 0.000 |
| 54 | 1,000 | 999,999 | No | Weekday | S4 | 0.1% | 999.999 |
| 55 | 1,000 | 999,999 | Yes | Weekend | S2 | 0.1% | 999.999 |
| 56 | 1,000 | 999,999 | No | Weekend | S4 | 0.1% | 999.999 |
| 57 | 1,000 | 1,000,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 58 | 1,000 | 1,000,000 | No | Weekday | S4 | 0.1% | 1,000.000 |
| 59 | 1,000 | 1,000,000 | Yes | Weekend | S2 | 0.1% | 1,000.000 |
| 60 | 1,000 | 1,000,000 | No | Weekend | S4 | 0.1% | 1,000.000 |
| 61 | 1,001 | 1 | Yes | Weekday | S1 | 0% | 0.000 |
| 62 | 1,001 | 1 | No | Weekday | S4 | 0.1% | 0.001 |
| 63 | 1,001 | 1 | Yes | Weekend | S2 | 0.1% | 0.001 |
| 64 | 1,001 | 1 | No | Weekend | S4 | 0.1% | 0.001 |
| 65 | 1,001 | 2 | Yes | Weekday | S1 | 0% | 0.000 |
| 66 | 1,001 | 2 | No | Weekday | S4 | 0.1% | 0.002 |
| 67 | 1,001 | 2 | Yes | Weekend | S2 | 0.1% | 0.002 |
| 68 | 1,001 | 2 | No | Weekend | S4 | 0.1% | 0.002 |
| 69 | 1,001 | 1,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 70 | 1,001 | 1,000 | No | Weekday | S4 | 0.1% | 1.000 |
| 71 | 1,001 | 1,000 | Yes | Weekend | S2 | 0.1% | 1.000 |
| 72 | 1,001 | 1,000 | No | Weekend | S4 | 0.1% | 1.000 |
| 73 | 1,001 | 999,999 | Yes | Weekday | S1 | 0% | 0.000 |
| 74 | 1,001 | 999,999 | No | Weekday | S4 | 0.1% | 999.999 |
| 75 | 1,001 | 999,999 | Yes | Weekend | S2 | 0.1% | 999.999 |
| 76 | 1,001 | 999,999 | No | Weekend | S4 | 0.1% | 999.999 |
| 77 | 1,001 | 1,000,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 78 | 1,001 | 1,000,000 | No | Weekday | S4 | 0.1% | 1,000.000 |
| 79 | 1,001 | 1,000,000 | Yes | Weekend | S2 | 0.1% | 1,000.000 |
| 80 | 1,001 | 1,000,000 | No | Weekend | S4 | 0.1% | 1,000.000 |
| 81 | 3,000 | 1 | Yes | Weekday | S1 | 0% | 0.000 |
| 82 | 3,000 | 1 | No | Weekday | S4 | 0.1% | 0.001 |
| 83 | 3,000 | 1 | Yes | Weekend | S2 | 0.1% | 0.001 |
| 84 | 3,000 | 1 | No | Weekend | S4 | 0.1% | 0.001 |
| 85 | 3,000 | 2 | Yes | Weekday | S1 | 0% | 0.000 |
| 86 | 3,000 | 2 | No | Weekday | S4 | 0.1% | 0.002 |
| 87 | 3,000 | 2 | Yes | Weekend | S2 | 0.1% | 0.002 |
| 88 | 3,000 | 2 | No | Weekend | S4 | 0.1% | 0.002 |
| 89 | 3,000 | 1,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 90 | 3,000 | 1,000 | No | Weekday | S4 | 0.1% | 1.000 |
| 91 | 3,000 | 1,000 | Yes | Weekend | S2 | 0.1% | 1.000 |
| 92 | 3,000 | 1,000 | No | Weekend | S4 | 0.1% | 1.000 |
| 93 | 3,000 | 999,999 | Yes | Weekday | S1 | 0% | 0.000 |
| 94 | 3,000 | 999,999 | No | Weekday | S4 | 0.1% | 999.999 |
| 95 | 3,000 | 999,999 | Yes | Weekend | S2 | 0.1% | 999.999 |
| 96 | 3,000 | 999,999 | No | Weekend | S4 | 0.1% | 999.999 |
| 97 | 3,000 | 1,000,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 98 | 3,000 | 1,000,000 | No | Weekday | S4 | 0.1% | 1,000.000 |
| 99 | 3,000 | 1,000,000 | Yes | Weekend | S2 | 0.1% | 1,000.000 |
| 100 | 3,000 | 1,000,000 | No | Weekend | S4 | 0.1% | 1,000.000 |
| 101 | 4,998 | 1 | Yes | Weekday | S1 | 0% | 0.000 |
| 102 | 4,998 | 1 | No | Weekday | S4 | 0.1% | 0.001 |
| 103 | 4,998 | 1 | Yes | Weekend | S2 | 0.1% | 0.001 |
| 104 | 4,998 | 1 | No | Weekend | S4 | 0.1% | 0.001 |
| 105 | 4,998 | 2 | Yes | Weekday | S1 | 0% | 0.000 |
| 106 | 4,998 | 2 | No | Weekday | S4 | 0.1% | 0.002 |
| 107 | 4,998 | 2 | Yes | Weekend | S2 | 0.1% | 0.002 |
| 108 | 4,998 | 2 | No | Weekend | S4 | 0.1% | 0.002 |
| 109 | 4,998 | 1,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 110 | 4,998 | 1,000 | No | Weekday | S4 | 0.1% | 1.000 |
| 111 | 4,998 | 1,000 | Yes | Weekend | S2 | 0.1% | 1.000 |
| 112 | 4,998 | 1,000 | No | Weekend | S4 | 0.1% | 1.000 |
| 113 | 4,998 | 999,999 | Yes | Weekday | S1 | 0% | 0.000 |
| 114 | 4,998 | 999,999 | No | Weekday | S4 | 0.1% | 999.999 |
| 115 | 4,998 | 999,999 | Yes | Weekend | S2 | 0.1% | 999.999 |
| 116 | 4,998 | 999,999 | No | Weekend | S4 | 0.1% | 999.999 |
| 117 | 4,998 | 1,000,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 118 | 4,998 | 1,000,000 | No | Weekday | S4 | 0.1% | 1,000.000 |
| 119 | 4,998 | 1,000,000 | Yes | Weekend | S2 | 0.1% | 1,000.000 |
| 120 | 4,998 | 1,000,000 | No | Weekend | S4 | 0.1% | 1,000.000 |
| 121 | 4,999 | 1 | Yes | Weekday | S1 | 0% | 0.000 |
| 122 | 4,999 | 1 | No | Weekday | S4 | 0.1% | 0.001 |
| 123 | 4,999 | 1 | Yes | Weekend | S2 | 0.1% | 0.001 |
| 124 | 4,999 | 1 | No | Weekend | S4 | 0.1% | 0.001 |
| 125 | 4,999 | 2 | Yes | Weekday | S1 | 0% | 0.000 |
| 126 | 4,999 | 2 | No | Weekday | S4 | 0.1% | 0.002 |
| 127 | 4,999 | 2 | Yes | Weekend | S2 | 0.1% | 0.002 |
| 128 | 4,999 | 2 | No | Weekend | S4 | 0.1% | 0.002 |
| 129 | 4,999 | 1,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 130 | 4,999 | 1,000 | No | Weekday | S4 | 0.1% | 1.000 |
| 131 | 4,999 | 1,000 | Yes | Weekend | S2 | 0.1% | 1.000 |
| 132 | 4,999 | 1,000 | No | Weekend | S4 | 0.1% | 1.000 |
| 133 | 4,999 | 999,999 | Yes | Weekday | S1 | 0% | 0.000 |
| 134 | 4,999 | 999,999 | No | Weekday | S4 | 0.1% | 999.999 |
| 135 | 4,999 | 999,999 | Yes | Weekend | S2 | 0.1% | 999.999 |
| 136 | 4,999 | 999,999 | No | Weekend | S4 | 0.1% | 999.999 |
| 137 | 4,999 | 1,000,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 138 | 4,999 | 1,000,000 | No | Weekday | S4 | 0.1% | 1,000.000 |
| 139 | 4,999 | 1,000,000 | Yes | Weekend | S2 | 0.1% | 1,000.000 |
| 140 | 4,999 | 1,000,000 | No | Weekend | S4 | 0.1% | 1,000.000 |
| 141 | 5,000 | 1 | Yes | Weekday | S1 | 0% | 0.000 |
| 142 | 5,000 | 1 | No | Weekday | S5 | 0% | 0.000 |
| 143 | 5,000 | 1 | Yes | Weekend | S2 | 0.1% | 0.001 |
| 144 | 5,000 | 1 | No | Weekend | S5 | 0% | 0.000 |
| 145 | 5,000 | 2 | Yes | Weekday | S1 | 0% | 0.000 |
| 146 | 5,000 | 2 | No | Weekday | S5 | 0% | 0.000 |
| 147 | 5,000 | 2 | Yes | Weekend | S2 | 0.1% | 0.002 |
| 148 | 5,000 | 2 | No | Weekend | S5 | 0% | 0.000 |
| 149 | 5,000 | 1,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 150 | 5,000 | 1,000 | No | Weekday | S5 | 0% | 0.000 |
| 151 | 5,000 | 1,000 | Yes | Weekend | S2 | 0.1% | 1.000 |
| 152 | 5,000 | 1,000 | No | Weekend | S5 | 0% | 0.000 |
| 153 | 5,000 | 999,999 | Yes | Weekday | S1 | 0% | 0.000 |
| 154 | 5,000 | 999,999 | No | Weekday | S5 | 0% | 0.000 |
| 155 | 5,000 | 999,999 | Yes | Weekend | S2 | 0.1% | 999.999 |
| 156 | 5,000 | 999,999 | No | Weekend | S5 | 0% | 0.000 |
| 157 | 5,000 | 1,000,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 158 | 5,000 | 1,000,000 | No | Weekday | S5 | 0% | 0.000 |
| 159 | 5,000 | 1,000,000 | Yes | Weekend | S2 | 0.1% | 1,000.000 |
| 160 | 5,000 | 1,000,000 | No | Weekend | S5 | 0% | 0.000 |
| 161 | 5,001 | 1 | Yes | Weekday | S1 | 0% | 0.000 |
| 162 | 5,001 | 1 | No | Weekday | S5 | 0% | 0.000 |
| 163 | 5,001 | 1 | Yes | Weekend | S2 | 0.1% | 0.001 |
| 164 | 5,001 | 1 | No | Weekend | S5 | 0% | 0.000 |
| 165 | 5,001 | 2 | Yes | Weekday | S1 | 0% | 0.000 |
| 166 | 5,001 | 2 | No | Weekday | S5 | 0% | 0.000 |
| 167 | 5,001 | 2 | Yes | Weekend | S2 | 0.1% | 0.002 |
| 168 | 5,001 | 2 | No | Weekend | S5 | 0% | 0.000 |
| 169 | 5,001 | 1,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 170 | 5,001 | 1,000 | No | Weekday | S5 | 0% | 0.000 |
| 171 | 5,001 | 1,000 | Yes | Weekend | S2 | 0.1% | 1.000 |
| 172 | 5,001 | 1,000 | No | Weekend | S5 | 0% | 0.000 |
| 173 | 5,001 | 999,999 | Yes | Weekday | S1 | 0% | 0.000 |
| 174 | 5,001 | 999,999 | No | Weekday | S5 | 0% | 0.000 |
| 175 | 5,001 | 999,999 | Yes | Weekend | S2 | 0.1% | 999.999 |
| 176 | 5,001 | 999,999 | No | Weekend | S5 | 0% | 0.000 |
| 177 | 5,001 | 1,000,000 | Yes | Weekday | S1 | 0% | 0.000 |
| 178 | 5,001 | 1,000,000 | No | Weekday | S5 | 0% | 0.000 |
| 179 | 5,001 | 1,000,000 | Yes | Weekend | S2 | 0.1% | 1,000.000 |
| 180 | 5,001 | 1,000,000 | No | Weekend | S5 | 0% | 0.000 |
 
#### Table B: out-of-range balance or amount (100 test cases)
 
An out-of-range balance is below $0. An out-of-range amount is below $1 or above
$1,000,000.
 
| Test case | Balance ($) | Amount ($) | Student | Day type | Invalid input | Expected result |
|---:|---:|---:|:---:|:---:|---|---|
| 181 | -1 | 0 | Yes | Weekday | balance and amount | `IllegalArgumentException` |
| 182 | -1 | 0 | No | Weekday | balance and amount | `IllegalArgumentException` |
| 183 | -1 | 0 | Yes | Weekend | balance and amount | `IllegalArgumentException` |
| 184 | -1 | 0 | No | Weekend | balance and amount | `IllegalArgumentException` |
| 185 | -1 | 1 | Yes | Weekday | balance | `IllegalArgumentException` |
| 186 | -1 | 1 | No | Weekday | balance | `IllegalArgumentException` |
| 187 | -1 | 1 | Yes | Weekend | balance | `IllegalArgumentException` |
| 188 | -1 | 1 | No | Weekend | balance | `IllegalArgumentException` |
| 189 | -1 | 2 | Yes | Weekday | balance | `IllegalArgumentException` |
| 190 | -1 | 2 | No | Weekday | balance | `IllegalArgumentException` |
| 191 | -1 | 2 | Yes | Weekend | balance | `IllegalArgumentException` |
| 192 | -1 | 2 | No | Weekend | balance | `IllegalArgumentException` |
| 193 | -1 | 1,000 | Yes | Weekday | balance | `IllegalArgumentException` |
| 194 | -1 | 1,000 | No | Weekday | balance | `IllegalArgumentException` |
| 195 | -1 | 1,000 | Yes | Weekend | balance | `IllegalArgumentException` |
| 196 | -1 | 1,000 | No | Weekend | balance | `IllegalArgumentException` |
| 197 | -1 | 999,999 | Yes | Weekday | balance | `IllegalArgumentException` |
| 198 | -1 | 999,999 | No | Weekday | balance | `IllegalArgumentException` |
| 199 | -1 | 999,999 | Yes | Weekend | balance | `IllegalArgumentException` |
| 200 | -1 | 999,999 | No | Weekend | balance | `IllegalArgumentException` |
| 201 | -1 | 1,000,000 | Yes | Weekday | balance | `IllegalArgumentException` |
| 202 | -1 | 1,000,000 | No | Weekday | balance | `IllegalArgumentException` |
| 203 | -1 | 1,000,000 | Yes | Weekend | balance | `IllegalArgumentException` |
| 204 | -1 | 1,000,000 | No | Weekend | balance | `IllegalArgumentException` |
| 205 | -1 | 1,000,001 | Yes | Weekday | balance and amount | `IllegalArgumentException` |
| 206 | -1 | 1,000,001 | No | Weekday | balance and amount | `IllegalArgumentException` |
| 207 | -1 | 1,000,001 | Yes | Weekend | balance and amount | `IllegalArgumentException` |
| 208 | -1 | 1,000,001 | No | Weekend | balance and amount | `IllegalArgumentException` |
| 209 | 0 | 0 | Yes | Weekday | amount | `IllegalArgumentException` |
| 210 | 0 | 0 | No | Weekday | amount | `IllegalArgumentException` |
| 211 | 0 | 0 | Yes | Weekend | amount | `IllegalArgumentException` |
| 212 | 0 | 0 | No | Weekend | amount | `IllegalArgumentException` |
| 213 | 0 | 1,000,001 | Yes | Weekday | amount | `IllegalArgumentException` |
| 214 | 0 | 1,000,001 | No | Weekday | amount | `IllegalArgumentException` |
| 215 | 0 | 1,000,001 | Yes | Weekend | amount | `IllegalArgumentException` |
| 216 | 0 | 1,000,001 | No | Weekend | amount | `IllegalArgumentException` |
| 217 | 999 | 0 | Yes | Weekday | amount | `IllegalArgumentException` |
| 218 | 999 | 0 | No | Weekday | amount | `IllegalArgumentException` |
| 219 | 999 | 0 | Yes | Weekend | amount | `IllegalArgumentException` |
| 220 | 999 | 0 | No | Weekend | amount | `IllegalArgumentException` |
| 221 | 999 | 1,000,001 | Yes | Weekday | amount | `IllegalArgumentException` |
| 222 | 999 | 1,000,001 | No | Weekday | amount | `IllegalArgumentException` |
| 223 | 999 | 1,000,001 | Yes | Weekend | amount | `IllegalArgumentException` |
| 224 | 999 | 1,000,001 | No | Weekend | amount | `IllegalArgumentException` |
| 225 | 1,000 | 0 | Yes | Weekday | amount | `IllegalArgumentException` |
| 226 | 1,000 | 0 | No | Weekday | amount | `IllegalArgumentException` |
| 227 | 1,000 | 0 | Yes | Weekend | amount | `IllegalArgumentException` |
| 228 | 1,000 | 0 | No | Weekend | amount | `IllegalArgumentException` |
| 229 | 1,000 | 1,000,001 | Yes | Weekday | amount | `IllegalArgumentException` |
| 230 | 1,000 | 1,000,001 | No | Weekday | amount | `IllegalArgumentException` |
| 231 | 1,000 | 1,000,001 | Yes | Weekend | amount | `IllegalArgumentException` |
| 232 | 1,000 | 1,000,001 | No | Weekend | amount | `IllegalArgumentException` |
| 233 | 1,001 | 0 | Yes | Weekday | amount | `IllegalArgumentException` |
| 234 | 1,001 | 0 | No | Weekday | amount | `IllegalArgumentException` |
| 235 | 1,001 | 0 | Yes | Weekend | amount | `IllegalArgumentException` |
| 236 | 1,001 | 0 | No | Weekend | amount | `IllegalArgumentException` |
| 237 | 1,001 | 1,000,001 | Yes | Weekday | amount | `IllegalArgumentException` |
| 238 | 1,001 | 1,000,001 | No | Weekday | amount | `IllegalArgumentException` |
| 239 | 1,001 | 1,000,001 | Yes | Weekend | amount | `IllegalArgumentException` |
| 240 | 1,001 | 1,000,001 | No | Weekend | amount | `IllegalArgumentException` |
| 241 | 3,000 | 0 | Yes | Weekday | amount | `IllegalArgumentException` |
| 242 | 3,000 | 0 | No | Weekday | amount | `IllegalArgumentException` |
| 243 | 3,000 | 0 | Yes | Weekend | amount | `IllegalArgumentException` |
| 244 | 3,000 | 0 | No | Weekend | amount | `IllegalArgumentException` |
| 245 | 3,000 | 1,000,001 | Yes | Weekday | amount | `IllegalArgumentException` |
| 246 | 3,000 | 1,000,001 | No | Weekday | amount | `IllegalArgumentException` |
| 247 | 3,000 | 1,000,001 | Yes | Weekend | amount | `IllegalArgumentException` |
| 248 | 3,000 | 1,000,001 | No | Weekend | amount | `IllegalArgumentException` |
| 249 | 4,998 | 0 | Yes | Weekday | amount | `IllegalArgumentException` |
| 250 | 4,998 | 0 | No | Weekday | amount | `IllegalArgumentException` |
| 251 | 4,998 | 0 | Yes | Weekend | amount | `IllegalArgumentException` |
| 252 | 4,998 | 0 | No | Weekend | amount | `IllegalArgumentException` |
| 253 | 4,998 | 1,000,001 | Yes | Weekday | amount | `IllegalArgumentException` |
| 254 | 4,998 | 1,000,001 | No | Weekday | amount | `IllegalArgumentException` |
| 255 | 4,998 | 1,000,001 | Yes | Weekend | amount | `IllegalArgumentException` |
| 256 | 4,998 | 1,000,001 | No | Weekend | amount | `IllegalArgumentException` |
| 257 | 4,999 | 0 | Yes | Weekday | amount | `IllegalArgumentException` |
| 258 | 4,999 | 0 | No | Weekday | amount | `IllegalArgumentException` |
| 259 | 4,999 | 0 | Yes | Weekend | amount | `IllegalArgumentException` |
| 260 | 4,999 | 0 | No | Weekend | amount | `IllegalArgumentException` |
| 261 | 4,999 | 1,000,001 | Yes | Weekday | amount | `IllegalArgumentException` |
| 262 | 4,999 | 1,000,001 | No | Weekday | amount | `IllegalArgumentException` |
| 263 | 4,999 | 1,000,001 | Yes | Weekend | amount | `IllegalArgumentException` |
| 264 | 4,999 | 1,000,001 | No | Weekend | amount | `IllegalArgumentException` |
| 265 | 5,000 | 0 | Yes | Weekday | amount | `IllegalArgumentException` |
| 266 | 5,000 | 0 | No | Weekday | amount | `IllegalArgumentException` |
| 267 | 5,000 | 0 | Yes | Weekend | amount | `IllegalArgumentException` |
| 268 | 5,000 | 0 | No | Weekend | amount | `IllegalArgumentException` |
| 269 | 5,000 | 1,000,001 | Yes | Weekday | amount | `IllegalArgumentException` |
| 270 | 5,000 | 1,000,001 | No | Weekday | amount | `IllegalArgumentException` |
| 271 | 5,000 | 1,000,001 | Yes | Weekend | amount | `IllegalArgumentException` |
| 272 | 5,000 | 1,000,001 | No | Weekend | amount | `IllegalArgumentException` |
| 273 | 5,001 | 0 | Yes | Weekday | amount | `IllegalArgumentException` |
| 274 | 5,001 | 0 | No | Weekday | amount | `IllegalArgumentException` |
| 275 | 5,001 | 0 | Yes | Weekend | amount | `IllegalArgumentException` |
| 276 | 5,001 | 0 | No | Weekend | amount | `IllegalArgumentException` |
| 277 | 5,001 | 1,000,001 | Yes | Weekday | amount | `IllegalArgumentException` |
| 278 | 5,001 | 1,000,001 | No | Weekday | amount | `IllegalArgumentException` |
| 279 | 5,001 | 1,000,001 | Yes | Weekend | amount | `IllegalArgumentException` |
| 280 | 5,001 | 1,000,001 | No | Weekend | amount | `IllegalArgumentException` |

The JaCoCo coverage statistics are as follows:
Type	Coverage
Line	5.8%
Branch	8.2%
Method	13.8%

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

The ATM Session tests use Robust Worst Case Boundary Value Analysis under the single-fault assumption: one input characteristic is varied at a time while the others use nominal values. The model has three characteristics: PIN length, one PIN digit, and withdrawal amount. The nominal inputs are PIN `24681` and withdrawal `$550`.

For weak robust, one-factor-at-a-time BVA, the usual `6n + 1` count is one nominal combination plus six boundary values per input characteristic. PIN length is an exact-length condition (`min = max = 5`), so it has only two distinct non-nominal values: 4 and 6 characters. A PIN digit ranges from `0` through `9`; its six robust boundary values are `/`, `0`, `1`, `8`, `9`, and `:` with the other positions held at their nominal values from `24681`. The withdrawal amount has a minimum positive dispensable value of `$20` and a daily-limit upper boundary of `$1,000`. Its lower-boundary cases are `$19`, `$20`, and `$21`. Following the instructor's clarification and the `$20`/`$50` denomination rule, the upper-boundary cases use the nearest dispensable amounts below, at, and above the limit: `$990`, `$1,000`, and `$1,020`. `$550` is the nominal amount: it is between the limits, can be dispensed as eleven `$50` bills, and is not divisible by `$20`. The conceptual core set is `1 + 2 + 6 + 6 = 15` BVA combinations: one shared nominal baseline, two non-nominal PIN-length values, six PIN-digit boundary values, and six withdrawal-amount boundary values. In the implementation, the nominal assertions are kept observable in their respective validation paths: `pinLengthBoundaries` includes the nominal five-digit PIN and the amount cases include the nominal `$550`. This results in 16 executable core invocations (3 PIN-length + 6 PIN-digit + 7 amount cases), because the nominal PIN and amount are asserted separately. The seven position-specific PIN checks and the `$0` invalid-withdrawal probe are supplemental, bringing the class total to 24 invocations.

The console ATM dispenses `$20` and `$50` bills. The instructor confirmed that the upper-limit probes should use amounts that satisfy this denomination rule, so `$990` and `$1,020` are the nearest bill-dispensable values below and above `$1,000`. The `$1,020` case expects `InvalidAmountException` from the stated limit; the supplied code does not implement that limit, so the observed result should be that no exception is thrown. `$0` is included as a supplemental invalid-withdrawal probe, based on the instructor's confirmation that zero is a sensible value to test.

PIN digit cases also place invalid characters in positions two through five and valid boundary digits in the fifth position, rather than assuming only the first position matters.

PIN format is checked through `ATM.checkPin()`, which invokes `FormatChecker.checkPinFormat()` before calling `Session.addPin()`. `Session.addPin()` stores the PIN and advances the state; it does not validate the format. The helper uses the format-valid test card `4111111111111111` to reach the PIN prompt, and credentials are mocked so no bank lookup is needed. The PIN cases use JUnit parameterized tests; each method-source row has a descriptive case ID listed in the test-case tables. In the supplied CI report, parameterized cases are displayed by method name and invocation index rather than by those case IDs; the result table below maps them by source-row order. Amount tests construct a `Session` with an `ATMWithdrawal`, select a Chequing account, and use a no-op state so `Session.setAmount()` validation can be tested without performing a bank transaction. This keeps the amount cases independent of the PIN-format fault, but it does not exercise transaction processing or account balance handling.

`SessionTest` defines a test-only `SimulatedAccount` with `$5,000` available and a `$1,000` daily limit. The maximum withdrawal boundary is calculated as `min($5,000, $1,000) = $1,000`, and the tests derive their upper-boundary inputs from that value. This model is not attached to `Session` or stored in the bank database; amount tests still use a no-op state. The instructor confirmed that the daily limit is a testing target but is not implemented; therefore, the `$1,020` test is expected to expose that `Session.setAmount()` does not reject a dispensable amount above `$1,000`. The database seed PIN is `5555` (four digits); PIN tests use mocked credentials and test PINs so they can check the assignment's five-digit format independently.

No production code was changed. The professor clarified that tests should vary one input at a time into invalid areas; full worst-case combinations are not expected under the single-fault assumption. For the daily-limit probe, bill dispensability remains valid while only the amount's relationship to the `$1,000` limit changes.

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
| `acceptsNominalWithdrawalAmount` | `$550` | Accept |
| `acceptsNearestDispensableAmountBelowDailyLimit` | `$990` | Accept |
| `acceptsAmountAtDailyLimit` | `$1,000` | Accept |
| `rejectsNearestDispensableAmountAboveDailyLimit` | `$1,020` | Throw `InvalidAmountException` |
| `rejectsZeroWithdrawalAmount` (supplemental) | `$0` | Throw `InvalidAmountException` |

#### Test results

After applying the instructor's clarification, the targeted local run of `mvn -B -Dtest=atm.session.SessionTest test` ran 24 tests: 14 passed and 10 failed. Checkstyle reported zero violations. The latest PR CI output also lists 24 Session tests with 14 passed and 10 failed. Across all rows shown in that CI output, there are 43 results: 17 passed and 26 failed. This includes the two passing `ATMTest` cases, one passing withdrawal-fee test, and 16 failing transfer-fee decision-table cases. Project JaCoCo coverage is 12.0% line, 18.1% branch, and 25.8% method. The parameterized cases below are mapped by method-source order because CI displays their method names and invocation indexes rather than their descriptive case IDs.

| Test case | Observed result | Status |
|---|---|---|
| `rejectsPinOneDigitBelowRequiredLength` | `pinLengthBoundaries` invocation [1] | Failed |
| `acceptsPinAtRequiredLength` | `pinLengthBoundaries` invocation [2] | Failed |
| `rejectsPinOneDigitAboveRequiredLength` | `pinLengthBoundaries` invocation [3] | Passed |
| `rejectsCharacterImmediatelyBelowDigitRange` | `pinDigitBoundaries` invocation [1] | Passed |
| `acceptsPinWithDigitAtLowerBoundary` | `pinDigitBoundaries` invocation [2] | Failed |
| `acceptsPinWithDigitAdjacentToLowerBoundary` | `pinDigitBoundaries` invocation [3] | Failed |
| `acceptsPinWithDigitAdjacentToUpperBoundary` | `pinDigitBoundaries` invocation [4] | Failed |
| `acceptsPinWithDigitAtUpperBoundary` | `pinDigitBoundaries` invocation [5] | Failed |
| `rejectsCharacterImmediatelyAboveDigitRange` | `pinDigitBoundaries` invocation [6] | Passed |
| `rejectsNonDigitInSecondPinPosition` | `pinPositionChecks` invocation [1] | Passed |
| `rejectsNonDigitInThirdPinPosition` | `pinPositionChecks` invocation [2] | Passed |
| `rejectsNonDigitInFourthPinPosition` | `pinPositionChecks` invocation [3] | Passed |
| `rejectsCharacterImmediatelyBelowDigitRangeAtFifthPosition` | `pinPositionChecks` invocation [4] | Passed |
| `rejectsCharacterImmediatelyAboveDigitRangeAtFifthPosition` | `pinPositionChecks` invocation [5] | Passed |
| `acceptsPinWithZeroInFifthPosition` | `pinPositionChecks` invocation [6] | Failed |
| `acceptsPinWithNineInFifthPosition` | `pinPositionChecks` invocation [7] | Failed |
| `rejectsAmountOneDollarBelowMinimumDispensableAmount` | `SessionTest` | Passed |
| `acceptsMinimumDispensableAmount` | `SessionTest` | Passed |
| `rejectsAmountOneDollarAboveMinimumDispensableAmount` | `SessionTest` | Passed |
| `acceptsNominalWithdrawalAmount` | `SessionTest` | Passed |
| `acceptsNearestDispensableAmountBelowDailyLimit` | `SessionTest` | Passed |
| `acceptsAmountAtDailyLimit` | `SessionTest` | Passed |
| `rejectsNearestDispensableAmountAboveDailyLimit` | `SessionTest` | Failed |
| `rejectsZeroWithdrawalAmount` (supplemental) | `SessionTest` | Failed |

#### Fault localization

The current PR CI failures for this section include PIN cases and two amount cases. The parameterized invocation indexes below correspond to the ordered method-source rows in `SessionTest`; invalid-character cases pass because the incorrect length check also throws the expected exception, so those passes do not demonstrate correct digit validation. The `$1,020` test expects an exception because the professor confirmed the `$1,000` daily limit is the target; no exception is thrown because the limit is not implemented. The supplemental `$0` test also expects an exception, under the instructor-confirmed assumption that zero is not a withdrawal amount.

| Failing test case(s) | Suspected fault (file, method, condition) | One-sentence justification |
|---|---|---|
| `rejectsPinOneDigitBelowRequiredLength` (`pinLengthBoundaries` [1]) | `src/main/java/atm/utils/FormatChecker.java`, `checkPinFormat()`, line 33: `pin.length == 4` | The four-character input is accepted by the implementation even though the specification requires five digits. |
| `acceptsPinAtRequiredLength` (`pinLengthBoundaries` [2]); valid digit cases `pinDigitBoundaries` [2]–[5] and `pinPositionChecks` [6]–[7] | `src/main/java/atm/utils/FormatChecker.java`, `checkPinFormat()`, line 33: `pin.length == 4` | The implementation rejects five-character PINs before it can validate their digits, causing each expected-valid PIN to fail. |
| `rejectsNearestDispensableAmountAboveDailyLimit` | `src/main/java/atm/session/Session.java`, `setAmount()`, withdrawal branch; no daily-limit check is implemented | `Session.setAmount()` accepts dispensable `$1,020` instead of throwing for exceeding the specified `$1,000` upper limit. |
| `rejectsZeroWithdrawalAmount` (supplemental) | `src/main/java/atm/utils/CashValidator.java`, `validateWithdrawal()`; no positive-amount check | Under the instructor-confirmed assumption that `$0` is not a withdrawal, the validator incorrectly accepts it because the denomination remainders are both zero. |

