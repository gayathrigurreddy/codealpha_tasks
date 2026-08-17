import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class HotelReservationSystem {
    enum Category { STANDARD, DELUXE, SUITE }

    static class Room {
        int number;
        Category category;
        double price;

        Room(int number, Category category, double price) {
            this.number = number; this.category = category; this.price = price;
        }
    }

    static class Reservation {
        String id, guest;
        int roomNumber, nights;
        double total;
        LocalDate checkIn;

        Reservation(String id, String guest, int roomNumber, int nights,
                    double total, LocalDate checkIn) {
            this.id = id; this.guest = guest; this.roomNumber = roomNumber;
            this.nights = nights; this.total = total; this.checkIn = checkIn;
        }

        String serialize() {
            return String.join("|", id, guest.replace("|", "/"),
                    String.valueOf(roomNumber), String.valueOf(nights),
                    String.valueOf(total), checkIn.toString());
        }
    }

    static final Scanner scanner = new Scanner(System.in);
    static final List<Room> rooms = new ArrayList<>();
    static final Map<String, Reservation> reservations = new LinkedHashMap<>();
    static final String DATA_FILE = "reservations.txt";

    public static void main(String[] args) {
        seedRooms();
        loadReservations();
        System.out.println("=== Hotel Reservation System ===");

        while (true) {
            System.out.println("\n1. Search Available Rooms");
            System.out.println("2. Book Room");
            System.out.println("3. Cancel Reservation");
            System.out.println("4. View Reservation");
            System.out.println("5. List Reservations");
            System.out.println("6. Exit");

            int choice = readInt("Choose: ", 1, 6);
            switch (choice) {
                case 1 -> searchRooms();
                case 2 -> bookRoom();
                case 3 -> cancelReservation();
                case 4 -> viewReservation();
                case 5 -> listReservations();
                case 6 -> { saveReservations(); System.out.println("Goodbye."); return; }
            }
        }
    }

    static void seedRooms() {
        for (int i = 101; i <= 105; i++) rooms.add(new Room(i, Category.STANDARD, 2500));
        for (int i = 201; i <= 204; i++) rooms.add(new Room(i, Category.DELUXE, 4000));
        for (int i = 301; i <= 302; i++) rooms.add(new Room(i, Category.SUITE, 6500));
    }

    static void searchRooms() {
        LocalDate date = readDate("Check-in date (YYYY-MM-DD): ");
        int nights = readInt("Number of nights: ", 1, 365);
        System.out.println("\nAvailable rooms:");
        for (Room r : rooms) {
            if (isAvailable(r.number, date, nights))
                System.out.printf("Room %d | %-8s | Rs. %.2f/night%n", r.number, r.category, r.price);
        }
    }

    static void bookRoom() {
        LocalDate date = readDate("Check-in date (YYYY-MM-DD): ");
        int nights = readInt("Number of nights: ", 1, 365);

        searchRoomsForBooking(date, nights);
        int roomNumber = readInt("Room number: ", 101, 302);
        Room room = findRoom(roomNumber);

        if (room == null || !isAvailable(roomNumber, date, nights)) {
            System.out.println("Room is not available for those dates.");
            return;
        }

        String guest = readNonEmpty("Guest name: ");
        double total = room.price * nights;
        System.out.printf("Total amount: Rs. %.2f%n", total);

        String payment = readNonEmpty("Simulate payment (type PAY to confirm): ");
        if (!payment.equalsIgnoreCase("PAY")) {
            System.out.println("Payment cancelled. Booking not created.");
            return;
        }

        String id = "RES-" + System.currentTimeMillis();
        reservations.put(id, new Reservation(id, guest, roomNumber, nights, total, date));
        saveReservations();
        System.out.println("Booking confirmed. Reservation ID: " + id);
    }

    static void searchRoomsForBooking(LocalDate date, int nights) {
        System.out.println("\nAvailable rooms:");
        for (Room r : rooms)
            if (isAvailable(r.number, date, nights))
                System.out.printf("Room %d | %-8s | Rs. %.2f/night%n", r.number, r.category, r.price);
    }

    static boolean isAvailable(int roomNumber, LocalDate start, int nights) {
        LocalDate end = start.plusDays(nights);
        for (Reservation r : reservations.values()) {
            if (r.roomNumber != roomNumber) continue;
            LocalDate existingEnd = r.checkIn.plusDays(r.nights);
            if (start.isBefore(existingEnd) && r.checkIn.isBefore(end)) return false;
        }
        return true;
    }

    static void cancelReservation() {
        String id = readNonEmpty("Reservation ID: ");
        Reservation r = reservations.remove(id);
        if (r == null) {
            System.out.println("Reservation not found.");
            return;
        }
        saveReservations();
        System.out.println("Reservation cancelled. Payment refund simulated.");
    }

    static void viewReservation() {
        String id = readNonEmpty("Reservation ID: ");
        Reservation r = reservations.get(id);
        if (r == null) {
            System.out.println("Reservation not found.");
            return;
        }
        System.out.printf("%nID: %s%nGuest: %s%nRoom: %d%nCheck-in: %s%nNights: %d%nTotal: Rs. %.2f%n",
                r.id, r.guest, r.roomNumber, r.checkIn, r.nights, r.total);
    }

    static void listReservations() {
        if (reservations.isEmpty()) {
            System.out.println("No reservations.");
            return;
        }
        for (Reservation r : reservations.values())
            System.out.printf("%s | %s | Room %d | %s | %d nights | Rs. %.2f%n",
                    r.id, r.guest, r.roomNumber, r.checkIn, r.nights, r.total);
    }

    static Room findRoom(int number) {
        for (Room r : rooms) if (r.number == number) return r;
        return null;
    }

    static void saveReservations() {
        try (PrintWriter out = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Reservation r : reservations.values()) out.println(r.serialize());
        } catch (IOException e) {
            System.out.println("Could not save reservations: " + e.getMessage());
        }
    }

    static void loadReservations() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length != 6) continue;
                Reservation r = new Reservation(p[0], p[1], Integer.parseInt(p[2]),
                        Integer.parseInt(p[3]), Double.parseDouble(p[4]), LocalDate.parse(p[5]));
                reservations.put(r.id, r);
            }
        } catch (Exception e) {
            System.out.println("Could not load reservations: " + e.getMessage());
        }
    }

    static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String v = scanner.nextLine().trim();
            if (!v.isEmpty()) return v;
            System.out.println("Value cannot be empty.");
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

    static LocalDate readDate(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                LocalDate d = LocalDate.parse(scanner.nextLine().trim());
                if (!d.isBefore(LocalDate.now())) return d;
            } catch (DateTimeParseException ignored) {}
            System.out.println("Enter a valid date today or later in YYYY-MM-DD format.");
        }
    }
}
