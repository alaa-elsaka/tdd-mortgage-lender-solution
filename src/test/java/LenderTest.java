import model.Loan;
import model.LoanStatus;
import model.Qualification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LenderTest {

    private Lender subject;
    private Loan fullyQualifiedLoan;
    private Loan lowDTILoan;
    private Loan lowCreditScoreLoan;
    private Loan partiallyQualifiedLoan;

    @BeforeEach
    void setUp() {
        subject = new Lender(100000);
        fullyQualifiedLoan = new Loan(250000, 21, 700, 100000);
        lowDTILoan = new Loan(250000, 37, 700, 100000);
        lowCreditScoreLoan = new Loan(250000, 30, 600, 100000);
        partiallyQualifiedLoan = new Loan(250000, 30, 700, 50000);
    }

    @Test
    void getAvailableFund() {
        long fund = subject.getAvailableFund();

        assertEquals(100000, fund);
    }

    @Test
    void addFund() {
        subject.addFund(50000);
        long fund = subject.getAvailableFund();

        assertEquals(150000, fund);
    }

    @Test
    void qualifyLoan_qualified() {
        Loan loan = subject.qualifyLoan(fullyQualifiedLoan);

        assertEquals(Qualification.FULLY_QUALIFIED, loan.getQualification());
        assertEquals(250000, loan.getLoanAmount());
        assertEquals(LoanStatus.QUALIFIED, loan.getStatus());
        assertEquals(loan, subject.getLoans().get(loan.getId()));
    }

    @Test
    void qualifyLoan_highDTI_denied() {
        Loan loan = subject.qualifyLoan(lowDTILoan);

        assertEquals(Qualification.NOT_QUALIFIED, loan.getQualification());
        assertEquals(0, loan.getLoanAmount());
        assertEquals(LoanStatus.DENIED, loan.getStatus());
    }

    @Test
    void qualifyLoan_lowCreditScore_denied() {
        Loan loan = subject.qualifyLoan(lowCreditScoreLoan);

        assertEquals(Qualification.NOT_QUALIFIED, loan.getQualification());
        assertEquals(0, loan.getLoanAmount());
        assertEquals(LoanStatus.DENIED, loan.getStatus());
    }

    @Test
    void qualifyLoan_partiallyQualified() {
        Loan loan = subject.qualifyLoan(partiallyQualifiedLoan);

        assertEquals(Qualification.PARTIALLY_QUALIFIED, loan.getQualification());
        assertEquals(200000, loan.getLoanAmount());
        assertEquals(LoanStatus.QUALIFIED, loan.getStatus());
    }

    @Test
    void process_approved() throws Exception {
        subject.addFund(150000);
        subject.qualifyLoan(fullyQualifiedLoan);

        Loan loan = subject.process(fullyQualifiedLoan.getId());

        assertEquals(LoanStatus.APPROVED, loan.getStatus());
        assertEquals(0, subject.getAvailableFund());
        assertEquals(250000, subject.getPendingFund());
    }

    @Test
    void process_insufficientFund_onHold() throws Exception {
        subject.qualifyLoan(fullyQualifiedLoan);

        Loan loan = subject.process(fullyQualifiedLoan.getId());

        assertEquals(LoanStatus.ON_HOLD, loan.getStatus());
    }

    @Test
    void process_notQualified_throwsException() {
        subject.qualifyLoan(lowDTILoan);

        LoanProcessException exception =
            assertThrows(LoanProcessException.class, () -> subject.process(lowDTILoan.getId()));

        assertEquals("Do not process unqualified loan", exception.getMessage());
    }

    @Test
    void applicantReply_accept() throws Exception {
        subject.addFund(150000);
        subject.qualifyLoan(fullyQualifiedLoan);
        subject.process(fullyQualifiedLoan.getId());

        Loan loan = subject.applicantReply(fullyQualifiedLoan.getId(), true);

        assertEquals(LoanStatus.ACCEPTED, loan.getStatus());
        assertEquals(0, subject.getAvailableFund());
        assertEquals(0, subject.getPendingFund());
    }

    @Test
    void applicantReply_reject() throws Exception {
        subject.addFund(150000);
        subject.qualifyLoan(partiallyQualifiedLoan);
        subject.process(partiallyQualifiedLoan.getId());

        Loan loan = subject.applicantReply(partiallyQualifiedLoan.getId(), false);

        assertEquals(LoanStatus.REJECTED, loan.getStatus());
        assertEquals(250000, subject.getAvailableFund());
        assertEquals(0, subject.getPendingFund());
    }

    @Test
    void applicantReply_notApprovedLoans_throwsException() {
        subject.qualifyLoan(lowDTILoan);

        LoanProcessException exception =
            assertThrows(LoanProcessException.class, () -> subject.applicantReply(lowDTILoan.getId(), true));

        assertEquals("Applicant cannot accept unapproved loan", exception.getMessage());
    }
}
