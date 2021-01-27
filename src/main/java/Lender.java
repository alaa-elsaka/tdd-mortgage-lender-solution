import model.Loan;
import model.LoanStatus;
import model.Qualification;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Lender {
    private long availableFund;
    private long pendingFund;
    private final Map<UUID, Loan> loans;

    public Lender(long initialFund) {
        this.availableFund = initialFund;
        this.pendingFund = 0;
        this.loans = new HashMap<>();
    }

    public long getAvailableFund() {
        return availableFund;
    }

    public long getPendingFund() {
        return pendingFund;
    }

    public void addFund(long amount) {
        availableFund += amount;
    }

    public Loan qualifyLoan(Loan loan) {
        if (loan.getDti() >= 36 || loan.getCreditScore() <= 620) {
            loan.setQualification(Qualification.NOT_QUALIFIED);
            loan.setLoanAmount(0);
            loan.setStatus(LoanStatus.DENIED);
        } else if (loan.getSavings() < (loan.getRequestedAmount() * 0.25)) {
            loan.setQualification(Qualification.PARTIALLY_QUALIFIED);
            loan.setLoanAmount(loan.getSavings() * 4);
            loan.setStatus(LoanStatus.QUALIFIED);
        } else {
            loan.setQualification(Qualification.FULLY_QUALIFIED);
            loan.setLoanAmount(loan.getRequestedAmount());
            loan.setStatus(LoanStatus.QUALIFIED);
        }
        loans.put(loan.getId(), loan);
        return loan;
    }

    public Map<UUID, Loan> getLoans() {
        return loans;
    }

    public Loan process(UUID id) throws LoanProcessException {
        Loan loan = loans.get(id);

        if (loan.getStatus() != LoanStatus.QUALIFIED) {
            throw new LoanProcessException("Do not process unqualified loan");
        }

        if (loan.getLoanAmount() <= availableFund) {
            loan.setStatus(LoanStatus.APPROVED);
            loan.setApprovedDate(LocalDate.now());
            availableFund -= loan.getLoanAmount();
            pendingFund += loan.getLoanAmount();
        } else {
            loan.setStatus(LoanStatus.ON_HOLD);
        }

        return loan;
    }

    public Loan applicantReply(UUID id, boolean accept) throws LoanProcessException {
        Loan loan = loans.get(id);

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new LoanProcessException("Applicant cannot accept unapproved loan");
        }

        pendingFund -= loan.getLoanAmount();

        if (accept) {
            loan.setStatus(LoanStatus.ACCEPTED);
        } else {
            loan.setStatus(LoanStatus.REJECTED);
            availableFund += loan.getLoanAmount();
        }

        return loan;
    }
}
