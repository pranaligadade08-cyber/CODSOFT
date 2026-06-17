import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StudentGradeCalculator {

    // ── Grade boundaries ──────────────────────────────────────────────────
    static final double A_PLUS  = 90;
    static final double A       = 80;
    static final double B_PLUS  = 70;
    static final double B       = 60;
    static final double C_PLUS  = 50;
    static final double C       = 40;
    // below 40 → F

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        printBanner();

        boolean continueProgram = true;

        while (continueProgram) {

            // ── Student details ───────────────────────────────────────────
            System.out.print("\n  Enter student name          : ");
            String studentName = scanner.nextLine().trim();

            System.out.print("  Enter number of subjects    : ");
            int numSubjects = readPositiveInt(scanner);

            // ── Subject marks input ───────────────────────────────────────
            List<String> subjectNames = new ArrayList<>();
            List<Integer> marks       = new ArrayList<>();

            System.out.println();
            System.out.println("  ─── Enter subject names and marks (0 – 100) ─────");

            for (int i = 1; i <= numSubjects; i++) {
                System.out.printf("  Subject %d name              : ", i);
                String subjectName = scanner.nextLine().trim();
                if (subjectName.isEmpty()) subjectName = "Subject " + i;

                System.out.printf("  Marks obtained in %-12s: ", subjectName);
                int mark = readMarkInRange(scanner);

                subjectNames.add(subjectName);
                marks.add(mark);
                System.out.println();
            }

            // ── Calculations ──────────────────────────────────────────────
            int    totalMarks      = marks.stream().mapToInt(Integer::intValue).sum();
            int    totalPossible   = numSubjects * 100;
            double avgPercentage   = (double) totalMarks / numSubjects;
            String grade           = calculateGrade(avgPercentage);
            String remarks         = calculateRemarks(grade);

            // ── Result display ────────────────────────────────────────────
            printResultCard(studentName, subjectNames, marks,
                            totalMarks, totalPossible, avgPercentage, grade, remarks);

            // ── Calculate another? ────────────────────────────────────────
            System.out.print("\n  Calculate for another student? (yes / no) : ");
            String response = scanner.nextLine().trim().toLowerCase();
            continueProgram = response.equals("yes") || response.equals("y");
        }

        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════╗");
        System.out.println("  ║   Thank you for using Grade Calculator!      ║");
        System.out.println("  ╚══════════════════════════════════════════════╝");
        scanner.close();
    }

    // ── Grade logic ───────────────────────────────────────────────────────

    static String calculateGrade(double avg) {
        if (avg >= A_PLUS) return "A+";
        if (avg >= A)      return "A";
        if (avg >= B_PLUS) return "B+";
        if (avg >= B)      return "B";
        if (avg >= C_PLUS) return "C+";
        if (avg >= C)      return "C";
        return "F";
    }

    static String calculateRemarks(String grade) {
        switch (grade) {
            case "A+": return "Outstanding  🏆";
            case "A":  return "Excellent    ★";
            case "B+": return "Very Good    ✔";
            case "B":  return "Good         ✔";
            case "C+": return "Average      –";
            case "C":  return "Satisfactory –";
            default:   return "Fail  – Needs Improvement  ✘";
        }
    }

    // ── Display ───────────────────────────────────────────────────────────

    static void printBanner() {
        System.out.println("  ╔══════════════════════════════════════════════╗");
        System.out.println("  ║        STUDENT  GRADE  CALCULATOR            ║");
        System.out.println("  ╠══════════════════════════════════════════════╣");
        System.out.println("  ║  Grading Scale:                              ║");
        System.out.println("  ║   A+  ≥ 90   │   A  ≥ 80   │   B+ ≥ 70      ║");
        System.out.println("  ║   B   ≥ 60   │   C+ ≥ 50   │   C  ≥ 40      ║");
        System.out.println("  ║   F   < 40   (Fail)                          ║");
        System.out.println("  ╚══════════════════════════════════════════════╝");
    }

    static void printResultCard(String name,
                                 List<String> subjects,
                                 List<Integer> marks,
                                 int total,
                                 int possible,
                                 double avg,
                                 String grade,
                                 String remarks) {

        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════╗");
        System.out.printf ("  ║  RESULT CARD  %-31s║%n", "");
        System.out.printf ("  ║  Student : %-35s║%n", name);
        System.out.println("  ╠══════════════════════════════════════════════╣");
        System.out.printf ("  ║  %-20s  %6s  %6s  %5s  ║%n",
                "Subject", "Marks", "  /  ", "  %  ");
        System.out.println("  ╠══════════════════════════════════════════════╣");

        for (int i = 0; i < subjects.size(); i++) {
            double pct = marks.get(i);
            System.out.printf("  ║  %-20s  %6d  %6d  %4.1f%%  ║%n",
                    truncate(subjects.get(i), 20), marks.get(i), 100, pct);
        }

        System.out.println("  ╠══════════════════════════════════════════════╣");
        System.out.printf ("  ║  %-20s  %6d  %6d  %4.1f%%  ║%n",
                "TOTAL", total, possible, (double) total / possible * 100);
        System.out.println("  ╠══════════════════════════════════════════════╣");
        System.out.printf ("  ║  Average Percentage   :  %-20s║%n",
                String.format("%.2f%%", avg));
        System.out.printf ("  ║  Grade                :  %-20s║%n", grade);
        System.out.printf ("  ║  Remarks              :  %-20s║%n", remarks);
        System.out.println("  ╚══════════════════════════════════════════════╝");
    }

    // ── Input helpers ─────────────────────────────────────────────────────

    /** Reads an integer ≥ 1, re-prompting on bad input. */
    static int readPositiveInt(Scanner sc) {
        while (true) {
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val >= 1) return val;
                System.out.print("  ⚠  Must be at least 1. Try again: ");
            } catch (NumberFormatException e) {
                System.out.print("  ⚠  Invalid input. Enter a whole number: ");
            }
        }
    }

    /** Reads an integer in the range [0, 100]. */
    static int readMarkInRange(Scanner sc) {
        while (true) {
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val >= 0 && val <= 100) return val;
                System.out.print("  ⚠  Marks must be between 0 and 100. Try again: ");
            } catch (NumberFormatException e) {
                System.out.print("  ⚠  Invalid input. Enter a number (0-100): ");
            }
        }
    }

    /** Truncates a string to maxLen characters. */
    static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }
}
