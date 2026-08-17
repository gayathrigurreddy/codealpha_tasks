import java.util.*;

public class StudentGradeTracker {
    static class Student {
        private final String name;
        private final List<Double> grades = new ArrayList<>();

        Student(String name) { this.name = name; }
        void addGrade(double grade) { grades.add(grade); }
        double average() { return grades.stream().mapToDouble(Double::doubleValue).average().orElse(0); }
        double highest() { return grades.stream().mapToDouble(Double::doubleValue).max().orElse(0); }
        double lowest() { return grades.stream().mapToDouble(Double::doubleValue).min().orElse(0); }
        String getName() { return name; }
        List<Double> getGrades() { return grades; }
    }

    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Student> students = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=== Student Grade Tracker ===");

        int count = readInt("Enter number of students: ", 1, 1000);
        for (int i = 1; i <= count; i++) {
            String name = readNonEmpty("Enter name for student " + i + ": ");
            Student student = new Student(name);

            int subjects = readInt("Enter number of subjects for " + name + ": ", 1, 50);
            for (int j = 1; j <= subjects; j++) {
                student.addGrade(readDouble("Enter score for subject " + j + " (0-100): ", 0, 100));
            }
            students.add(student);
        }

        printReport();
    }

    private static void printReport() {
        System.out.println("\n================ SUMMARY REPORT ================");
        System.out.printf("%-25s %-10s %-10s %-10s %-10s%n",
                "Student", "Average", "Highest", "Lowest", "Result");
        System.out.println("---------------------------------------------------------------");

        double classTotal = 0;
        double classHighest = 0;
        double classLowest = 100;

        for (Student s : students) {
            double avg = s.average();
            classTotal += avg;
            classHighest = Math.max(classHighest, s.highest());
            classLowest = Math.min(classLowest, s.lowest());

            System.out.printf("%-25s %-10.2f %-10.2f %-10.2f %-10s%n",
                    s.getName(), avg, s.highest(), s.lowest(), result(avg));
        }

        System.out.println("---------------------------------------------------------------");
        System.out.printf("Class Average : %.2f%n", classTotal / students.size());
        System.out.printf("Class Highest : %.2f%n", classHighest);
        System.out.printf("Class Lowest  : %.2f%n", classLowest);
        System.out.println("===============================================================");
    }

    private static String result(double average) {
        return average >= 40 ? "PASS" : "FAIL";
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Value cannot be empty.");
        }
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {}
            System.out.printf("Enter a whole number between %d and %d.%n", min, max);
        }
    }

    private static double readDouble(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {}
            System.out.printf("Enter a number between %.0f and %.0f.%n", min, max);
        }
    }
}
