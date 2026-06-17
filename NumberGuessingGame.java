import java.util.Random;
import java.util.Scanner;

public class NumberGuessingGame {

    static final int MIN_NUMBER    = 1;
    static final int MAX_NUMBER    = 100;
    static final int MAX_ATTEMPTS  = 7;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random  random  = new Random();

        int totalRounds = 0;
        int roundsWon   = 0;
        int totalScore  = 0;

        printBanner();

        boolean playAgain = true;

        while (playAgain) {
            totalRounds++;

            // ── generate secret number ────────────────────────────────────
            int secretNumber = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            int attemptsLeft = MAX_ATTEMPTS;
            int attemptsUsed = 0;
            boolean guessedCorrectly = false;

            System.out.printf("%n╔══════════════════════════════════════╗%n");
            System.out.printf("║         R O U N D   %-3d               ║%n", totalRounds);
            System.out.printf("╚══════════════════════════════════════╝%n");
            System.out.printf("I'm thinking of a number between %d and %d.%n", MIN_NUMBER, MAX_NUMBER);
            System.out.printf("You have %d attempts. Good luck!%n%n", MAX_ATTEMPTS);

            // ── guessing loop ─────────────────────────────────────────────
            while (attemptsLeft > 0) {
                System.out.printf("Attempt %d / %d  ▶  Enter your guess: ",
                        attemptsUsed + 1, MAX_ATTEMPTS);

                int guess = readInt(scanner);

                if (guess < MIN_NUMBER || guess > MAX_NUMBER) {
                    System.out.printf("  ⚠  Please enter a number between %d and %d.%n%n",
                            MIN_NUMBER, MAX_NUMBER);
                    continue;          // don't consume an attempt for out-of-range input
                }

                attemptsUsed++;
                attemptsLeft--;

                if (guess == secretNumber) {
                    guessedCorrectly = true;
                    break;
                } else if (guess < secretNumber) {
                    System.out.println("  ↑  Too low!  Try higher.");
                } else {
                    System.out.println("  ↓  Too high! Try lower.");
                }

                if (attemptsLeft > 0) {
                    System.out.printf("     %d attempt%s remaining.%n%n",
                            attemptsLeft, attemptsLeft == 1 ? "" : "s");
                }
            }

            // ── round result ──────────────────────────────────────────────
            if (guessedCorrectly) {
                int roundScore = calculateScore(attemptsUsed);
                totalScore += roundScore;
                roundsWon++;

                System.out.println();
                System.out.println("  ✔  Correct! The number was " + secretNumber + ".");
                System.out.printf("  You guessed it in %d attempt%s.%n",
                        attemptsUsed, attemptsUsed == 1 ? "" : "s");
                System.out.printf("  Round score: %d points%n", roundScore);
            } else {
                System.out.println();
                System.out.println("  ✘  Out of attempts! The number was " + secretNumber + ".");
                System.out.println("  Better luck next round!");
            }

            printScoreboard(totalRounds, roundsWon, totalScore);

            // ── play again? ───────────────────────────────────────────────
            System.out.print("\nWould you like to play again? (yes / no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            playAgain = response.equals("yes") || response.equals("y");
        }

        // ── final summary ─────────────────────────────────────────────────
        System.out.println();
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║           G A M E   O V E R          ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.printf("  Rounds played : %d%n", totalRounds);
        System.out.printf("  Rounds won    : %d%n", roundsWon);
        System.out.printf("  Final score   : %d points%n", totalScore);
        System.out.println("  Thanks for playing!");

        scanner.close();
    }

    // ── helpers ───────────────────────────────────────────────────────────

    /**
     * Score formula: more points for fewer attempts used.
     *   Attempts 1 → 700 pts | Attempts 7 → 100 pts
     */
    static int calculateScore(int attemptsUsed) {
        return Math.max(100, (MAX_ATTEMPTS - attemptsUsed + 1) * 100);
    }

    static void printScoreboard(int rounds, int won, int score) {
        System.out.println();
        System.out.println("  ─── Scoreboard ──────────────────────");
        System.out.printf("  Rounds played: %-4d  Rounds won: %d%n", rounds, won);
        System.out.printf("  Total score  : %d points%n", score);
        System.out.println("  ─────────────────────────────────────");
    }

    static void printBanner() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   NUMBER GUESSING GAME  (1 – 100)    ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.printf("║  Max attempts per round  :  %-3d       ║%n", MAX_ATTEMPTS);
        System.out.println("║  Scoring: 700→100 pts (fewest wins)  ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    /** Reads an integer from stdin, re-prompting on invalid input. */
    static int readInt(Scanner scanner) {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("  ⚠  Invalid input. Please enter a whole number: ");
            }
        }
    }
}
