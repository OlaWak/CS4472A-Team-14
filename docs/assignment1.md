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
Line	5.9%
Branch	9.3%
Method	13.8%