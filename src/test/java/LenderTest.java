import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LenderTest {

    @Test
    void checkAvailableFund() {
        Lender subject = new Lender(100000);

        long fund = subject.checkAvailableFund();

        assertEquals(100000, fund);
    }

    @Test
    void addFund() {
        Lender subject = new Lender(100000);

        subject.addFund(50000);
        long fund = subject.checkAvailableFund();

        assertEquals(150000, fund);
    }
}
