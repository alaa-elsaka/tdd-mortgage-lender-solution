import model.LoanApplication;
import model.LoanStatus;
import model.Qualification;

public class Lender {
    private long availableFund;

    public Lender(long initialFund) {
        this.availableFund = initialFund;
    }

    public long checkAvailableFund() {
        return availableFund;
    }

    public void addFund(long amount) {
        availableFund += amount;
    }

    public LoanApplication qualifyLoan(LoanApplication application) {
        if (application.getDti() >= 36 || application.getCreditScore() <= 620) {
            application.setQualification(Qualification.NOT_QUALIFIED);
            application.setLoanAmount(0);
            application.setStatus(LoanStatus.DENIED);
        } else if (application.getSavings() < (application.getRequestedAmount() * 0.25)) {
            application.setQualification(Qualification.PARTIALLY_QUALIFIED);
            application.setLoanAmount(application.getSavings() * 4);
            application.setStatus(LoanStatus.QUALIFIED);
        } else {
            application.setQualification(Qualification.FULLY_QUALIFIED);
            application.setLoanAmount(application.getRequestedAmount());
            application.setStatus(LoanStatus.QUALIFIED);
        }
        return application;
    }
}
