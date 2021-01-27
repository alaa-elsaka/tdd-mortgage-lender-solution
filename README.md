# Instructions

## TDD Mortgage Lender

Every day, potential buyers are looking for a lender to finance their new home. Let's build an app to simulate a
potential (and grossly over-simplified) process for lenders to qualify and approve loan applicants. The process can be summarized:

An applicant submits an application for a loan. The lender will then check that the applicant qualifies before approving the application. If the application is approved, the lender offers a loan amount for which the candidate qualifies. For example, if an applicant applies for $100,000, the lender may decide that the applicant only qualifies for $75,000 and send them an offer for that amount. The applicant must then decide to accept or reject the loan offered. Once an offer is accepted, the lender gives the money to the candidate to purchase their new home.

### Objectives

Use what you've learned about pair programming, TDD, and OOP to fulfill the acceptance criteria.

Your final code should have:

1. No compile errors.
2. No failing tests.
3. Descriptive commit messages.
4. Multiple commits.
5. Meet acceptance criteria.
6. Have appropriate use of the four pillars of OOP.

### Tips

Apply what you've learned so far.

- Write tests first
- Use SEAT to guide you
- Use red green refactor
- Apply OOP where appropriate

### Instructions

Submit a link to your completed code at the [checkpoint](../../checkpoints/mortgage_lender.md). Every member of the group should submit the same link.

### Stories and Acceptance Criteria

As a lender, I want to be able to check my available funds, so that I know how much money I can offer for loans.

```gherkin
When I check my available funds
Then I should see how much funds I currently have
```

As a lender, I want to add money to my available funds, so that I can offer loans to potential home buyers.

```gherkin
Given I have <current_amount> available funds
When I add <deposit_amount>
Then my available funds should be <total>

Examples:
| current_amount | deposit_amount |   total  |
|     100,000    |      50,000    | 150,000  |
|     200,000    |      30,000    | 230,000  |
```

As a lender, I want to accept and qualify loan applications, so that I can ensure I get my money back.

```gherkin
Rule: To qualify for the full amount, candidates must have debt-to-income (DTI) less than 36%, credit score above 620 
and savings worth 25% of requested loan amount.

Rule: To partially qualify, candidates must still meet the same dti and credit score thresholds. 
The loan amount for partial qualified applications is four times the applicant's savings.

Given a loan applicant with <dti>, <credit_score>, and <savings>
When they apply for a loan with <requested_amount>
Then their qualification is <qualification>
And their loan amount is <loan_amount>
And their loan status is <status>

Example:
|  requested_amount  |   dti  |  credit_score  |  savings  |     qualification    |  loan_amount  |   status   |
|      250,000       |   21   |       700      | 100,000   |       qualified      |   250,000     |  qualified |
|      250,000       |   37   |       700      | 100,000   |     not qualified    |         0     |  denied    |
|      250,000       |   30   |       600      | 100,000   |     not qualified    |         0     |  denied    |
|      250,000       |   30   |       700      |  50,000   |  partially qualified |   200,000     |  qualified |
```

As a lender, I want to only approve loans when I have available funds, so that I don't go bankrupt.

```gherkin
Given I have <available_funds> in available funds
When I process a qualified loan
Then the loan status is set to <status>

Example:
| loan_amount | available_funds |    status  |
|   125,000   |    100,000      |   on hold  |
|   125,000   |    200,000      |  approved  |
|   125,000   |    125,000      |  approved  |

When I process a not qualified loan
Then I should see a warning to not proceed
```

As a lender, I want to keep pending loan amounts in a separate account, so I don't extend too many offers and bankrupt myself.

```gherkin
Given I have approved a loan
Then the requested loan amount is moved from available funds to pending funds
And I see the available and pending funds reflect the changes accordingly
```

As a lender, I want to process response for approved loans, so that I can move forward with the loan.

```gherkin
Given I have an approved loan
When the applicant accepts my loan offer
Then the loan amount is removed from the pending funds
And the loan status is marked as accepted

Given I have an approved loan
When the applicant rejects my loan offer
Then the loan amount is moved from the pending funds back to available funds
And the loan status is marked as rejected
```

As a lender, I want to check if there are any undecided loans, so that I can manage my time and money wisely.

```gherkin
Rule: approved loans expired in 3 days

Given there is an approved loan offered more than 3 days ago
When I check for expired loans
Then the loan amount is move from the pending funds back to available funds
And the loan status is marked as expired
```

As a lender, I want to filter loans by status, so that I can have an overview.

```gherkin
Given there are loans in my system
When I search by loan status (qualified, denied, on hold, approved, accepted, rejected, expired)
Then I should see a list of loans and their details
```
