import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

class MainTest {

    @BeforeEach
    void resetState() {
        Main.processedOrderCount = 0;
        Main.orderTotalPrice = BigDecimal.ZERO;
        Main.orderHistory.clear();
    }

    @Test
    void shouldProcessDemonstrationOrdersCorrectly() throws Exception {

        String aliceOrder = Main.makeOrder(
                "Alice|alice@example.test|VIP|BOOK:2,PEN:3|SAVE10",
                false, false, false
        );

        String bobOrder = Main.makeOrder(
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

        assertEquals(2, Main.processedOrderCount);

        assertEquals(
                "REPORT|2|306.21",
                Main.makeReport(false)
        );
    }

    @Test
    void shouldApplyCouponAfterCustomerDiscount() throws Exception {

        String result = Main.makeOrder(
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

        String result = Main.makeOrder(
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

        String result = Main.makeOrder(
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

        String result = Main.makeReport(false);

        assertEquals("REPORT|0|0.00", result);
    }

    @Test
    void shouldGenerateReportAfterProcessingOrder()
            throws Exception {

        Main.makeOrder(
                "John|john@example.test|REGULAR|BOOK:1|NONE",
                false, false, false
        );

        assertEquals(
                "REPORT|1|63.96",
                Main.makeReport(false)
        );
    }

    @Test
    void shouldNotSendEmailWhenSendingIsDisabled()
            throws Exception {

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output));

            Main.makeOrder(
                    "John|john@example.test|REGULAR|BOOK:1|NONE",
                    false, false, false
            );

        } finally {
            System.setOut(originalOut);
        }

        assertEquals("", output.toString());
    }

    @Test
    void shouldCalculateTotalForRegularCustomer() {
        BigDecimal result = Main.calculateOrderTotal(
                new String[]{"BOOK:2", "PEN:3"},
                "REGULAR",
                "NONE"
        );

        assertEquals(new BigDecimal("137.76"), result);
    }

    @Test
    void shouldApplyVipDiscount() {
        BigDecimal result = Main.calculateOrderTotal(
                new String[]{"BOOK:2"},
                "VIP",
                "NONE"
        );

        assertEquals(new BigDecimal("88.56"), result);
    }

    @Test
    void shouldApplyStudentDiscount() {
        BigDecimal result = Main.calculateOrderTotal(
                new String[]{"BOOK:2"},
                "STUDENT",
                "NONE"
        );

        assertEquals(new BigDecimal("92.84"), result);
    }

    @Test
    void shouldApplyCouponDiscount() {
        BigDecimal result = Main.calculateOrderTotal(
                new String[]{"BOOK:2"},
                "REGULAR",
                "SAVE10"
        );

        assertEquals(new BigDecimal("103.32"), result);
    }

    @Test
    void shouldNotAddDeliveryFeeWhenThresholdIsReached() {
        BigDecimal result = Main.calculateOrderTotal(
                new String[]{"BAG:1", "PEN:4"},
                "REGULAR",
                "NONE"
        );

        assertEquals(new BigDecimal("123.00"), result);
    }
}
