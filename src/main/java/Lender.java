public class Lender {
    private long availableFund;

    public Lender(long initialFund) {
        this.availableFund = initialFund;
    }

    public long checkAvailableFund() {
        return availableFund;
    }
}
