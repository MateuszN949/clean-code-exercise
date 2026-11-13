import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

public class Main {
//    Price multipliers
    private static final BigDecimal VIP_DISCOUNT = new BigDecimal("0.10");
    private static final BigDecimal STUDENT_DISCOUNT = new BigDecimal("0.05");
    private static final BigDecimal COUPON_DISCOUNT = new BigDecimal("0.10");
    private static final BigDecimal EMPLOYEE_DISCOUNT = new BigDecimal("0.15");
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("12.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.23");

//    Order fields numbering
    private static final int CUSTOMER_NAME_INDEX = 0;
    private static final int CUSTOMER_EMAIL_INDEX = 1;
    private static final int CUSTOMER_TYPE_INDEX = 2;
    private static final int PRODUCTS_INDEX = 3;
    private static final int COUPON_INDEX = 4;

//    Product fields numbering
    private static final int PRODUCT_CODE_INDEX = 0;
    private static final int PRODUCT_QUANTITY_INDEX = 1;

    public interface Order {
        void send(String orderDescription);
        void save(String description) throws Exception;
        void refund(BigDecimal refundAmount);
    }

    public static class EmailOrder implements Order {
        public void send(String orderDescription) { System.out.println("EMAIL " + orderDescription); }
        public void save(String description) throws Exception {
            Files.writeString(Path.of("orders.txt"), description + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        public void refund(BigDecimal refundAmount) { throw new UnsupportedOperationException("Not supported"); }
    }

    static int processedOrderCount = 0;
    static BigDecimal orderTotalPrice = new BigDecimal("0.00");
    static Map<String, String> orderHistory = new HashMap<>();

    static String makeReport(boolean verbose)
    {
        String report = "REPORT|" + processedOrderCount + "|" + String.format(Locale.ROOT, "%.2f", orderTotalPrice);
        if (verbose) System.out.println(report);
        return report;
    }

    static BigDecimal calculateOrderTotal(String[] productList, String customerType, String couponCode) {

        BigDecimal orderTotal = new BigDecimal("0.00");

        for (String productTuple : productList) {
            String[] productFields = productTuple.split(":");

            String productCode = productFields[PRODUCT_CODE_INDEX];
            int quantity = Integer.parseInt(productFields[PRODUCT_QUANTITY_INDEX]);

            BigDecimal productPrice = switch (productCode) {
                case "BOOK" -> new BigDecimal("40.00");
                case "PEN" -> new BigDecimal("5.00");
                case "BAG" -> new BigDecimal("80.00");
                case "MUG" -> new BigDecimal("25.00");
                default -> new BigDecimal("0.00");
            };

            orderTotal = orderTotal.add(productPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        orderTotal = switch (customerType) {
            case "VIP" -> orderTotal.multiply(BigDecimal.ONE.subtract(VIP_DISCOUNT));
            case "STUDENT" -> orderTotal.multiply(BigDecimal.ONE.subtract(STUDENT_DISCOUNT));
            case "EMPLOYEE" -> orderTotal.multiply(BigDecimal.ONE.subtract(EMPLOYEE_DISCOUNT));
            default -> orderTotal;
        };

        if (couponCode.equals("SAVE10")) {
            orderTotal = orderTotal.multiply(BigDecimal.ONE.subtract(COUPON_DISCOUNT));
        }

        if (orderTotal.compareTo(FREE_DELIVERY_THRESHOLD) < 0) {
            orderTotal = orderTotal.add(DELIVERY_FEE);
        }

        return orderTotal.multiply(BigDecimal.ONE.add(TAX_RATE));
    }

    private static String formatOrder(String customerName, StringBuilder orderDetails, BigDecimal orderTotal) {
        return "ORDER|" + customerName + "|" + orderDetails + "|" + String.format(Locale.ROOT, "%.2f", orderTotal);
    }

    static String makeOrder(String orderDescription, boolean verbose, boolean sendNotification, boolean saveOrder) throws Exception {
        String[] orderFields = orderDescription.split("\\|", -1);
        String customerName = orderFields[CUSTOMER_NAME_INDEX];
        String customerEmail = orderFields[CUSTOMER_EMAIL_INDEX];
        String customerType = orderFields[CUSTOMER_TYPE_INDEX];

        String[] productList = orderFields[PRODUCTS_INDEX].split(",");

        BigDecimal orderTotal = calculateOrderTotal(productList, customerType, orderFields[COUPON_INDEX]);

        StringBuilder orderDetails = new StringBuilder();

        for (String productEntry : productList) {
            String[] productFields = productEntry.split(":");
            String productCode = productFields[PRODUCT_CODE_INDEX];
            String quantity = productFields[PRODUCT_QUANTITY_INDEX];

            orderDetails.append(productCode).append("x").append(quantity).append(";");
        }

        String orderResult = formatOrder(customerName, orderDetails, orderTotal);

        processedOrderCount++;
        orderTotalPrice = orderTotalPrice.add(orderTotal);
        orderHistory.put(customerEmail, orderResult);

        EmailOrder orderHandler = new EmailOrder();
        if (saveOrder) orderHandler.save(orderResult);
        if (sendNotification) orderHandler.send(customerEmail + "|" + orderResult);
        if (verbose) System.out.println(orderResult);

        return orderResult;
    }

    static String makeOrderOrReport(String orderDescription, int actionCode, boolean verbose, boolean sendNotification, boolean saveOrder) {
        try {
            switch(actionCode)
            {
                case 1 -> {
                    return makeOrder(orderDescription, verbose, sendNotification, saveOrder);
                }
                case 2 -> {
                    return makeReport(verbose);
                }
                default -> {
                    return "UNKNOWN";
                }
            }
           
        } catch (Exception exception) { return "ERROR"; }
    }

    public static void main(String[] args) {
        makeOrderOrReport("Alice|alice@example.test|VIP|BOOK:2,PEN:3|SAVE10", 1, true, true, true);
        makeOrderOrReport("Bob|bob@example.test|REGULAR|BAG:2|NONE", 1, true, true, true);
        makeOrderOrReport("", 2, true, false, false);
    }
}
