import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

public class Main {
    public interface Order {
        void send(String OrderData);
        void save(String OrderData) throws Exception;
        void refund(BigDecimal RefundValue);
    }

    public static class EmailOrder implements Order {
        public void send(String OrderData) { System.out.println("EMAIL " + OrderData); }
        public void save(String OrderData) throws Exception {
            Files.writeString(Path.of("orders.txt"), OrderData + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        public void refund(BigDecimal RefundValue) { throw new UnsupportedOperationException("Not supported"); }
    }

    static int orderCount = 0;
    static BigDecimal orderTotalPrice = new BigDecimal("0.00");
    static Map<String, String> orderHistory = new HashMap<>();

    static String DoReport(String orderDescription, int action, boolean verbose, boolean send, boolean save)
    {
                String x = "REPORT|" + orderCount + "|" + String.format(Locale.ROOT, "%.2f", orderTotalPrice);  
                if (verbose) System.out.println(x);
                return x;
    }
    static String DoOrder(String orderDescription, int action, boolean verbose, boolean send, boolean save)
    {
             String[] splitedOrderDescription = orderDescription.split("\\|", -1);
            String customerName = splitedOrderDescription[0], customerEmail = splitedOrderDescription[1], customerType = splitedOrderDescription[2];
            String[] productList = splitedOrderDescription[3].split(",");
            BigDecimal productTotal = new BigDecimal("0.00");
            StringBuilder detail = new StringBuilder();
            for (String productTouple : b) {
                String[] c = productTouple.split(":");
                BigDecimal product = switch (c[0]) {
                    case "BOOK" -> new BigDecimal(40);
                    case "PEN" -> new BigDecimal(5);
                    case "BAG" -> new BigDecimal(80);
                    default -> new BigDecimal(0);
                };
                productTotal = productTotal.add(product.multiply(new BigDecimal(Integer.parseInt(c[1]))));
                detail.append(product).append("x").append(c[1]).append(";");
            }
            if (customerType.equals("VIP")) productTotal = productTotal.multiply(new BigDecimal("0.9"));
            else if (customerType.equals("STUDENT")) productTotal = productTotal.multiply(new BigDecimal("0.95"));


            if (splitedOrderDescription[4].equals("SAVE10")) productTotal = productTotal.multiply(new BigDecimal("0.9"));

            if (productTotal.compareTo(new BigDecimal("100")) < 0) productTotal = productTotal.add(new BigDecimal("12"));

            productTotal = productTotal.multiply(new BigDecimal("1.23"));
            String x = "ORDER|" + customerName + "|" + detail + "|" + String.format(Locale.ROOT, "%.2f", productTotal);
            orderCount++;
            orderTotalPrice = orderTotalPrice.add(productTotal);
            orderHistory.put(customerEmail, x);
            EmailOrder obj = new EmailOrder();
            if (save) obj.save(x);
            if (send) obj.send(customerEmail + "|" + x);
            if (verbose) System.out.println(x);
            return x;
    }


    static String doIt(String orderDescription, int action, boolean verbose, boolean send, boolean save) {
        try {
            switch(action)
            {
                case 1 -> return DoOrder(orderDescription,action,verbose,send,save);
                case 2 -> return DoReport(orderDescription,action,verbose,send,save);
                default -> return "UNKNOWN";
            }
           
        } catch (Exception ex) { return "ERROR"; }
    }

    public static void main(String[] args) {
        doIt("Alice|alice@example.test|VIP|BOOK:2,PEN:3|SAVE10", 1, true, true, true);
        doIt("Bob|bob@example.test|REGULAR|BAG:2|NONE", 1, true, true, true);
        doIt("", 2, true, false, false);
    }
}
