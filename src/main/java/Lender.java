import model.Loan;
import model.LoanStatus;
import model.Qualification;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Lender {
    private long availableFund;
    private final Map<UUID, Loan> loans;

    public Lender(long initialFund) {
        this.availableFund = initialFund;
        this.loans = new HashMap<>();
    }

    public long checkAvailableFund() {
        return availableFund;
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
        } else {
            loan.setStatus(LoanStatus.ON_HOLD);
        }

        return loan;
    }
}
