import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @BeforeEach
    void resetState() {
        Main.sentOrderCount = 0;
        Main.orderTotalPrice = BigDecimal.ZERO;
        Main.orderHistory.clear();
    }

    @Test
    void shouldProcessDemonstrationOrdersCorrectly() throws Exception {

        String aliceOrder = Main.MakeOrder(
                "Alice|alice@example.test|VIP|BOOK:2,PEN:3|SAVE10",
                false, false, false
        );

        String bobOrder = Main.MakeOrder(
                "Bob|bob@example.test|REGULAR|BAG:2|NONE",
                false, false, false
        );

        assertEquals(
                "ORDER|Alice|BOOKx2;PENx3;|109.41",
                aliceOrder
        );

        assertEquals(
                "ORDER|Bob|BAGx2;|196.80",
                bobOrder
        );

        assertEquals(2, Main.sentOrderCount);

        assertEquals(
                "REPORT|2|306.21",
                Main.MakeReport(false)
        );
    }

    @Test
    void shouldApplyStudentDiscount() throws Exception {

        String result = Main.MakeOrder(
                "John|john@example.test|STUDENT|BOOK:1|NONE",
                false, false, false
        );

        assertEquals(
                "ORDER|John|BOOKx1;|61.50",
                result
        );
    }

    @Test
    void shouldApplyVipDiscount() throws Exception {

        String result = Main.MakeOrder(
                "Alice|alice@example.test|VIP|BOOK:1|NONE",
                false, false, false
        );

        assertEquals(
                "ORDER|Alice|BOOKx1;|59.04",
                result
        );
    }

    @Test
    void shouldApplyCouponAfterCustomerDiscount() throws Exception {

        String result = Main.MakeOrder(
                "Alice|alice@example.test|VIP|BOOK:2|SAVE10",
                false, false, false
        );

        assertEquals(
                "ORDER|Alice|BOOKx2;|94.46",
                result
        );
    }

    @Test
    void shouldAddDeliveryWhenDiscountedSubtotalIsBelow100()
            throws Exception {

        String result = Main.MakeOrder(
                "John|john@example.test|REGULAR|BAG:1,PEN:3|NONE",
                false, false, false
        );

        assertEquals(
                "ORDER|John|BAGx1;PENx3;|131.61",
                result
        );
    }

    @Test
    void shouldNotAddDeliveryWhenSubtotalIsExactly100()
            throws Exception {

        String result = Main.MakeOrder(
                "John|john@example.test|REGULAR|BAG:1,PEN:4|NONE",
                false, false, false
        );

        assertEquals(
                "ORDER|John|BAGx1;PENx4;|123.00",
                result
        );
    }

    @Test
    void shouldGenerateEmptyReport() {

        String result = Main.MakeReport(false);

        assertEquals("REPORT|0|0.00", result);
    }

    @Test
    void shouldGenerateReportAfterProcessingOrder()
            throws Exception {

        Main.MakeOrder(
                "John|john@example.test|REGULAR|BOOK:1|NONE",
                false, false, false
        );

        assertEquals(
                "REPORT|1|63.96",
                Main.MakeReport(false)
        );
    }

    @Test
    void shouldNotSendEmailWhenSendingIsDisabled()
            throws Exception {

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output));

            Main.MakeOrder(
                    "John|john@example.test|REGULAR|BOOK:1|NONE",
                    false, false, false
            );

        } finally {
            System.setOut(originalOut);
        }

        assertEquals("", output.toString());
    }
}
