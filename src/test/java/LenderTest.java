import model.Loan;
import model.LoanStatus;
import model.Qualification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LenderTest {

    private Lender subject;

    @BeforeEach
    void setUp() {
        subject = new Lender(100000);
    }

    @Test
    void checkAvailableFund() {
        long fund = subject.checkAvailableFund();

        assertEquals(100000, fund);
    }

    @Test
    void addFund() {
        subject.addFund(50000);
        long fund = subject.checkAvailableFund();

        assertEquals(150000, fund);
    }

    @Test
    void qualifyLoan_qualified() {
        Loan loan = subject.qualifyLoan(
            new Loan(250000, 21, 700, 100000)
        );

        assertEquals(Qualification.FULLY_QUALIFIED, loan.getQualification());
        assertEquals(250000, loan.getLoanAmount());
        assertEquals(LoanStatus.QUALIFIED, loan.getStatus());
        assertEquals(loan, subject.getLoans().get(0));
    }

    @Test
    void qualifyLoan_highDTI_denied() {
        Loan loan = subject.qualifyLoan(
            new Loan(250000, 37, 700, 100000)
        );

        assertEquals(Qualification.NOT_QUALIFIED, loan.getQualification());
        assertEquals(0, loan.getLoanAmount());
        assertEquals(LoanStatus.DENIED, loan.getStatus());
    }

    @Test
    void qualifyLoan_lowCreditScore_denied() {
        Loan loan = subject.qualifyLoan(
            new Loan(250000, 30, 600, 100000)
        );

        assertEquals(Qualification.NOT_QUALIFIED, loan.getQualification());
        assertEquals(0, loan.getLoanAmount());
        assertEquals(LoanStatus.DENIED, loan.getStatus());
    }

    @Test
    void qualifyLoan_partiallyQualified() {
        Loan loan = subject.qualifyLoan(
            new Loan(250000, 30, 700, 50000)
        );

        assertEquals(Qualification.PARTIALLY_QUALIFIED, loan.getQualification());
        assertEquals(200000, loan.getLoanAmount());
        assertEquals(LoanStatus.QUALIFIED, loan.getStatus());
    }
}
