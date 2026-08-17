import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StockTradingPlatform {
    static class Stock {
        String symbol, company;
        double price;

        Stock(String symbol, String company, double price) {
            this.symbol = symbol; this.company = company; this.price = price;
        }
    }

    static class Holding {
        int quantity;
        double averageBuyPrice;

        void buy(int qty, double price) {
            double total = quantity * averageBuyPrice + qty * price;
            quantity += qty;
            averageBuyPrice = total / quantity;
        }

        double marketValue(double price) { return quantity * price; }
        double profitLoss(double price) { return marketValue(price) - quantity * averageBuyPrice; }
    }

    static class User {
        String name;
        double cash;
        Map<String, Holding> portfolio = new HashMap<>();

        User(String name, double cash) { this.name = name; this.cash = cash; }
    }

    static final Scanner scanner = new Scanner(System.in);
    static final Map<String, Stock> market = new LinkedHashMap<>();
    static final User user = new User("Investor", 100000.00);
    static final String LOG_FILE = "transactions.txt";

    public static void main(String[] args) {
        seedMarket();
        System.out.println("=== Stock Trading Platform ===");

        while (true) {
            System.out.println("\n1. Market Data");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. Portfolio");
            System.out.println("5. Simulate Market Change");
            System.out.println("6. Exit");

            int choice = readInt("Choose: ", 1, 6);
            switch (choice) {
                case 1 -> showMarket();
                case 2 -> buy();
                case 3 -> sell();
                case 4 -> showPortfolio();
                case 5 -> simulateMarket();
                case 6 -> { System.out.println("Goodbye."); return; }
            }
        }
    }

    static void seedMarket() {
        market.put("AAPL", new Stock("AAPL", "Apple", 225.00));
        market.put("MSFT", new Stock("MSFT", "Microsoft", 520.00));
        market.put("GOOG", new Stock("GOOG", "Alphabet", 190.00));
        market.put("AMZN", new Stock("AMZN", "Amazon", 220.00));
        market.put("TSLA", new Stock("TSLA", "Tesla", 310.00));
    }

    static void showMarket() {
        System.out.printf("%n%-8s %-15s %12s%n", "Symbol", "Company", "Price");
        for (Stock s : market.values())
            System.out.printf("%-8s %-15s $%11.2f%n", s.symbol, s.company, s.price);
    }

    static void buy() {
        showMarket();
        String symbol = readSymbol();
        Stock stock = market.get(symbol);
        int qty = readInt("Quantity: ", 1, 1_000_000);
        double cost = qty * stock.price;

        if (cost > user.cash) {
            System.out.println("Insufficient cash.");
            return;
        }

        Holding holding = user.portfolio.computeIfAbsent(symbol, k -> new Holding());
        holding.buy(qty, stock.price);
        user.cash -= cost;
        log("BUY", symbol, qty, stock.price);
        System.out.printf("Bought %d %s for $%.2f%n", qty, symbol, cost);
    }

    static void sell() {
        showPortfolio();
        String symbol = readSymbol();
        Holding holding = user.portfolio.get(symbol);
        if (holding == null || holding.quantity == 0) {
            System.out.println("You do not own this stock.");
            return;
        }

        int qty = readInt("Quantity to sell: ", 1, holding.quantity);
        Stock stock = market.get(symbol);
        double proceeds = qty * stock.price;

        holding.quantity -= qty;
        user.cash += proceeds;
        log("SELL", symbol, qty, stock.price);
        if (holding.quantity == 0) user.portfolio.remove(symbol);

        System.out.printf("Sold %d %s for $%.2f%n", qty, symbol, proceeds);
    }

    static void showPortfolio() {
        double total = user.cash;
        System.out.println("\n=== Portfolio ===");
        System.out.printf("Cash: $%.2f%n", user.cash);

        if (user.portfolio.isEmpty()) {
            System.out.println("No holdings.");
        } else {
            System.out.printf("%-8s %8s %14s %14s%n", "Symbol", "Qty", "Value", "P/L");
            for (Map.Entry<String, Holding> e : user.portfolio.entrySet()) {
                Stock s = market.get(e.getKey());
                Holding h = e.getValue();
                double value = h.marketValue(s.price);
                total += value;
                System.out.printf("%-8s %8d $%13.2f $%13.2f%n",
                        s.symbol, h.quantity, value, h.profitLoss(s.price));
            }
        }
        System.out.printf("Total Account Value: $%.2f%n", total);
    }

    static void simulateMarket() {
        Random random = new Random();
        for (Stock s : market.values()) {
            double change = (random.nextDouble() - 0.5) * 0.10;
            s.price = Math.max(1, s.price * (1 + change));
        }
        System.out.println("Market prices updated.");
        showMarket();
    }

    static String readSymbol() {
        while (true) {
            System.out.print("Stock symbol: ");
            String symbol = scanner.nextLine().trim().toUpperCase();
            if (market.containsKey(symbol)) return symbol;
            System.out.println("Unknown symbol.");
        }
    }

    static int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int v = Integer.parseInt(scanner.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.printf("Enter a number between %d and %d.%n", min, max);
        }
    }

    static void log(String action, String symbol, int qty, double price) {
        try (FileWriter out = new FileWriter(LOG_FILE, true)) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            out.write(String.format("%s | %s | %s | Qty=%d | Price=%.2f%n",
                    time, action, symbol, qty, price));
        } catch (IOException e) {
            System.out.println("Could not save transaction: " + e.getMessage());
        }
    }
}
