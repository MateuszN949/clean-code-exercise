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
         String[] a = orderDescription.split("\\|", -1);
            String n = a[0], e = a[1], t = a[2];
            String[] b = a[3].split(",");
            BigDecimal z = new BigDecimal("0.00");
            StringBuilder detail = new StringBuilder();
            for (String q : b) {
                String[] c = q.split(":");
                BigDecimal p = switch (c[0]) {
                    case "BOOK" -> new BigDecimal(40);
                    case "PEN" -> new BigDecimal(5);
                    case "BAG" -> new BigDecimal(80);
                    default -> new BigDecimal(0);
                };
                z = z.add(p.multiply(new BigDecimal(Integer.parseInt(c[1]))));
                detail.append(c[0]).append("x").append(c[1]).append(";");
            }
            if (t.equals("VIP")) z = z.multiply(new BigDecimal("0.9"));
            else if (t.equals("STUDENT")) z = z.multiply(new BigDecimal("0.95"));
            if (a[4].equals("SAVE10")) z = z.multiply(new BigDecimal("0.9"));
            if (z.compareTo(new BigDecimal("100")) < 0) z = z.add(new BigDecimal("12"));
            z = z.multiply(new BigDecimal("1.23"));
            String x = "ORDER|" + n + "|" + detail + "|" + String.format(Locale.ROOT, "%.2f", z);
            orderCount++;
            orderTotalPrice = orderTotalPrice.add(z);
            orderHistory.put(e, x);
            EmailOrder obj = new EmailOrder();
            if (save) obj.save(x);
            if (send) obj.send(e + "|" + x);
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
