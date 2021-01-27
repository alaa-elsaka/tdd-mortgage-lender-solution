import model.LoanApplication;
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
        LoanApplication loanApplication = subject.qualifyLoan(
            new LoanApplication(250000, 21, 700, 100000)
        );

        assertEquals(Qualification.FULLY_QUALIFIED, loanApplication.getQualification());
        assertEquals(250000, loanApplication.getLoanAmount());
        assertEquals(LoanStatus.QUALIFIED, loanApplication.getStatus());
    }

    @Test
    void qualifyLoan_highDTI_denied() {
        LoanApplication loanApplication = subject.qualifyLoan(
            new LoanApplication(250000, 37, 700, 100000)
        );

        assertEquals(Qualification.NOT_QUALIFIED, loanApplication.getQualification());
        assertEquals(0, loanApplication.getLoanAmount());
        assertEquals(LoanStatus.DENIED, loanApplication.getStatus());
    }

    @Test
    void qualifyLoan_lowCreditScore_denied() {
        LoanApplication loanApplication = subject.qualifyLoan(
            new LoanApplication(250000, 30, 600, 100000)
        );

        assertEquals(Qualification.NOT_QUALIFIED, loanApplication.getQualification());
        assertEquals(0, loanApplication.getLoanAmount());
        assertEquals(LoanStatus.DENIED, loanApplication.getStatus());
    }

    @Test
    void qualifyLoan_partiallyQualified() {
        LoanApplication loanApplication = subject.qualifyLoan(
            new LoanApplication(250000, 30, 700, 50000)
        );

        assertEquals(Qualification.PARTIALLY_QUALIFIED, loanApplication.getQualification());
        assertEquals(200000, loanApplication.getLoanAmount());
        assertEquals(LoanStatus.QUALIFIED, loanApplication.getStatus());
    }
}
