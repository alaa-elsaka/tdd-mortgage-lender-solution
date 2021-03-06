import model.Loan;
import model.LoanStatus;
import model.Qualification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LenderTest {

    private Lender subject;
    private Loan fullyQualifiedLoan;
    private Loan highDTILoan;
    private Loan lowCreditScoreLoan;
    private Loan partiallyQualifiedLoan;

    @BeforeEach
    void setUp() {
        subject = new Lender(100000);
        fullyQualifiedLoan = new Loan(250000, 21, 700, 100000);
        highDTILoan = new Loan(250000, 37, 700, 100000);
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
        Loan loan = subject.qualifyLoan(highDTILoan);

        assertEquals(Qualification.NOT_QUALIFIED, loan.getQualification());
        assertEquals(0, loan.getLoanAmount());
        assertEquals(LoanStatus.DENIED, loan.getStatus());

        highDTILoan = new Loan(250000, 36, 700, 100000);
        loan = subject.qualifyLoan(highDTILoan);

        assertEquals(Qualification.NOT_QUALIFIED, loan.getQualification());
    }

    @Test
    void qualifyLoan_lowCreditScore_denied() {
        Loan loan = subject.qualifyLoan(lowCreditScoreLoan);

        assertEquals(Qualification.NOT_QUALIFIED, loan.getQualification());
        assertEquals(0, loan.getLoanAmount());
        assertEquals(LoanStatus.DENIED, loan.getStatus());

        lowCreditScoreLoan = new Loan(250000, 30, 620, 100000);
        loan = subject.qualifyLoan(lowCreditScoreLoan);

        assertEquals(Qualification.NOT_QUALIFIED, loan.getQualification());
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
        assertEquals(LocalDate.now(), loan.getApprovedDate());
    }

    @Test
    void process_insufficientFund_onHold() throws Exception {
        subject.qualifyLoan(fullyQualifiedLoan);

        Loan loan = subject.process(fullyQualifiedLoan.getId());

        assertEquals(LoanStatus.ON_HOLD, loan.getStatus());
    }

    @Test
    void process_notQualified_throwsException() {
        subject.qualifyLoan(highDTILoan);

        LoanProcessException exception =
            assertThrows(LoanProcessException.class, () -> subject.process(highDTILoan.getId()));

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
        subject.qualifyLoan(highDTILoan);

        LoanProcessException exception =
            assertThrows(LoanProcessException.class, () -> subject.applicantReply(highDTILoan.getId(), true));

        assertEquals("Applicant cannot accept unapproved loan", exception.getMessage());
    }

    @Test
    void checkExpired() throws Exception {
        subject.addFund(1000000);
        subject.qualifyLoan(fullyQualifiedLoan);
        subject.process(fullyQualifiedLoan.getId());
        subject.getLoans().get(fullyQualifiedLoan.getId()).setApprovedDate(LocalDate.now().minusDays(4));

        Loan nonExpiredLoan = new Loan(250000, 21, 700, 100000);
        subject.qualifyLoan(nonExpiredLoan);
        subject.process(nonExpiredLoan.getId());

        subject.checkExpired();

        assertEquals(LoanStatus.EXPIRED, subject.getLoans().get(fullyQualifiedLoan.getId()).getStatus());
        assertEquals(LoanStatus.APPROVED, subject.getLoans().get(nonExpiredLoan.getId()).getStatus());
        assertEquals(250000, subject.getPendingFund());
        assertEquals(850000, subject.getAvailableFund());
    }

    @Test
    void findLoanByStatus() throws LoanProcessException {
        subject.qualifyLoan(fullyQualifiedLoan);
        subject.qualifyLoan(highDTILoan);
        subject.qualifyLoan(lowCreditScoreLoan);
        subject.qualifyLoan(partiallyQualifiedLoan);

        assertEquals(
            Set.of(fullyQualifiedLoan, partiallyQualifiedLoan),
            subject.find(LoanStatus.QUALIFIED)
        );

        assertEquals(
            Set.of(highDTILoan, lowCreditScoreLoan),
            subject.find(LoanStatus.DENIED)
        );

        subject.process(fullyQualifiedLoan.getId());

        assertEquals(
            Set.of(fullyQualifiedLoan),
            subject.find(LoanStatus.ON_HOLD)
        );
    }
}
