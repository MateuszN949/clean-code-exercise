import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

public class Main {
    public interface Order {
        void send(String description);
        void save(String description) throws Exception;
        void refund(BigDecimal RefundValue);
    }

    public static class EmailOrder implements Order {
        public void send(String description) { System.out.println("EMAIL " + description); }
        public void save(String description) throws Exception {
            Files.writeString(Path.of("orders.txt"), description + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        public void refund(BigDecimal RefundValue) { throw new UnsupportedOperationException("Not supported"); }
    }

    static int sentOrderCount = 0;
    static BigDecimal orderTotalPrice = new BigDecimal("0.00");
    static Map<String, String> orderHistory = new HashMap<>();

    static String MakeReport(boolean verbose)
    {
        String x = "REPORT|" + sentOrderCount + "|" + String.format(Locale.ROOT, "%.2f", orderTotalPrice);
        if (verbose) System.out.println(x);
        return x;
    }

    static String MakeOrder(String orderDescription, boolean verbose, boolean send, boolean save) throws Exception {
        String[] splitOrderDescription = orderDescription.split("\\|", -1);
        String customerName = splitOrderDescription[0], customerEmail = splitOrderDescription[1], customerType = splitOrderDescription[2];
        String[] productList = splitOrderDescription[3].split(",");
        BigDecimal productTotal = new BigDecimal("0.00");
        StringBuilder detail = new StringBuilder();

        for (String productTuple : productList) {
            String[] c = productTuple.split(":");
            BigDecimal product = switch (c[0]) {
                case "BOOK" -> new BigDecimal(40);
                case "PEN" -> new BigDecimal(5);
                case "BAG" -> new BigDecimal(80);
                default -> new BigDecimal(0);
            };

            productTotal = productTotal.add(product.multiply(new BigDecimal(Integer.parseInt(c[1]))));
            detail.append(c[0]).append("x").append(c[1]).append(";");
        }

        if (customerType.equals("VIP")) productTotal = productTotal.multiply(new BigDecimal("0.9"));
        else if (customerType.equals("STUDENT")) productTotal = productTotal.multiply(new BigDecimal("0.95"));

        if (splitOrderDescription[4].equals("SAVE10")) productTotal = productTotal.multiply(new BigDecimal("0.9"));

        if (productTotal.compareTo(new BigDecimal("100")) < 0) productTotal = productTotal.add(new BigDecimal("12"));

        productTotal = productTotal.multiply(new BigDecimal("1.23"));
        String x = "ORDER|" + customerName + "|" + detail + "|" + String.format(Locale.ROOT, "%.2f", productTotal);

        sentOrderCount++;
        orderTotalPrice = orderTotalPrice.add(productTotal);
        orderHistory.put(customerEmail, x);

        EmailOrder obj = new EmailOrder();
        if (save) obj.save(x);
        if (send) obj.send(customerEmail + "|" + x);
        if (verbose) System.out.println(x);

        return x;
    }

    static String MakeOrderOrReport(String orderDescription, int action, boolean verbose, boolean send, boolean save) {
        try {
            switch(action)
            {
                case 1 -> {
                    return MakeOrder(orderDescription, verbose, send, save);
                }
                case 2 -> {
                    return MakeReport(verbose);
                }
                default -> {
                    return "UNKNOWN";
                }
            }
           
        } catch (Exception ex) { return "ERROR"; }
    }

    public static void main(String[] args) {
        MakeOrderOrReport("Alice|alice@example.test|VIP|BOOK:2,PEN:3|SAVE10", 1, true, true, true);
        MakeOrderOrReport("Bob|bob@example.test|REGULAR|BAG:2|NONE", 1, true, true, true);
        MakeOrderOrReport("", 2, true, false, false);
    }
}
