import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════╗
 *  REAL-TIME CURRENCY CONVERTER
 *  API  : Frankfurter (api.frankfurter.dev) — no key needed
 *  Data : European Central Bank (ECB) rates, updated daily
 * ╚══════════════════════════════════════════════════════════╝
 *
 * Compile:  javac CurrencyConverter.java
 * Run:      java CurrencyConverter
 */
public class CurrencyConverter {

    // ── API base URL ──────────────────────────────────────────────────────
    private static final String API_BASE = "https://api.frankfurter.dev/v1";

    // ── Popular currencies shown in the quick-pick menu ───────────────────
    private static final String[][] POPULAR = {
        {"USD", "US Dollar",           "$"},
        {"EUR", "Euro",                "€"},
        {"GBP", "British Pound",       "£"},
        {"INR", "Indian Rupee",        "₹"},
        {"JPY", "Japanese Yen",        "¥"},
        {"AUD", "Australian Dollar",   "A$"},
        {"CAD", "Canadian Dollar",     "C$"},
        {"CHF", "Swiss Franc",         "₣"},
        {"CNY", "Chinese Yuan",        "¥"},
        {"SGD", "Singapore Dollar",    "S$"},
        {"AED", "UAE Dirham",          "د.إ"},
        {"SAR", "Saudi Riyal",         "﷼"},
        {"ZAR", "South African Rand",  "R"},
        {"MXN", "Mexican Peso",        "$"},
        {"BRL", "Brazilian Real",      "R$"},
    };

    // ── Symbols map: code → symbol ─────────────────────────────────────
    private static final Map<String, String> SYMBOLS = new LinkedHashMap<>();
    static {
        for (String[] row : POPULAR) SYMBOLS.put(row[0], row[2]);
    }

    // ── Scanner (shared) ───────────────────────────────────────────────
    private static final Scanner sc = new Scanner(System.in);

    // ═════════════════════════════════════════════════════════════════════
    // MAIN
    // ═════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        printBanner();
        boolean running = true;

        while (running) {
            try {
                // 1. Choose base currency
                String base   = chooseCurrency("BASE (from)");

                // 2. Choose target currency
                String target = chooseCurrency("TARGET (to)");

                if (base.equalsIgnoreCase(target)) {
                    printWarn("Base and target currencies are the same. Choose different ones.");
                    continue;
                }

                // 3. Fetch live rate
                System.out.println();
                System.out.println("  Fetching live exchange rate …");
                double rate = fetchRate(base, target);

                // 4. Enter amount
                System.out.printf("%n  Enter amount in %s : %s", base, symbolOf(base));
                double amount = readPositiveDouble();

                // 5. Convert & display
                double converted = amount * rate;
                printResult(base, target, amount, converted, rate);

            } catch (Exception e) {
                printWarn("Could not fetch rates. Check your internet connection.");
                printWarn("Detail: " + e.getMessage());
            }

            // 6. Convert again?
            System.out.print("\n  Convert another amount? (yes / no) : ");
            String ans = sc.nextLine().trim().toLowerCase();
            running = ans.equals("yes") || ans.equals("y");
        }

        printFooter();
        sc.close();
    }

    // ═════════════════════════════════════════════════════════════════════
    // CURRENCY SELECTION
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Shows the quick-pick table, lets the user pick by number
     * OR type any valid ISO 4217 code manually.
     */
    private static String chooseCurrency(String label) {
        printHeader("SELECT " + label + " CURRENCY");
        printCurrencyTable();
        System.out.println();
        System.out.println("  Type a number (1–" + POPULAR.length +
                           ") OR enter any ISO 4217 code (e.g. HKD, SEK, NOK …)");
        System.out.print("  Your choice : ");

        while (true) {
            String input = sc.nextLine().trim().toUpperCase();

            // Numeric pick from the table
            try {
                int idx = Integer.parseInt(input) - 1;
                if (idx >= 0 && idx < POPULAR.length) {
                    String code = POPULAR[idx][0];
                    System.out.printf("  ✔  Selected : %s – %s%n", code, POPULAR[idx][1]);
                    return code;
                }
                printWarn("Number out of range. Try again:");
                System.out.print("  Your choice : ");
                continue;
            } catch (NumberFormatException ignored) { /* not a number → treat as code */ }

            // Manual ISO code
            if (input.length() == 3 && input.matches("[A-Z]+")) {
                System.out.printf("  ✔  Selected : %s%n", input);
                return input;
            }

            printWarn("Invalid input. Enter a number (1–" + POPULAR.length +
                      ") or a 3-letter currency code:");
            System.out.print("  Your choice : ");
        }
    }

    private static void printCurrencyTable() {
        System.out.println();
        System.out.println("  ┌────┬──────┬───────────────────────┬──────┐");
        System.out.printf ("  │ %-2s │ %-4s │ %-21s │ %-4s │%n", "#", "Code", "Currency", "Sym.");
        System.out.println("  ├────┼──────┼───────────────────────┼──────┤");
        for (int i = 0; i < POPULAR.length; i++) {
            System.out.printf("  │ %-2d │ %-4s │ %-21s │ %-4s │%n",
                    i + 1, POPULAR[i][0], POPULAR[i][1], POPULAR[i][2]);
        }
        System.out.println("  └────┴──────┴───────────────────────┴──────┘");
    }

    // ═════════════════════════════════════════════════════════════════════
    // API CALL  — Frankfurter  /v1/latest?base=XXX&symbols=YYY
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Fetches the live exchange rate for base → target.
     * Example response:
     *   {"amount":1.0,"base":"USD","date":"2026-06-16","rates":{"INR":83.87}}
     */
    private static double fetchRate(String base, String target) throws Exception {
        String urlStr = API_BASE + "/latest?base=" + base + "&symbols=" + target;
        String json   = httpGet(urlStr);

        // Parse "rates":{"TARGET": VALUE} — simple manual parse
        String ratesKey = "\"" + target + "\":";
        int idx = json.indexOf(ratesKey);
        if (idx == -1)
            throw new Exception("Currency code '" + target +
                    "' not found. Check the code is a valid ISO 4217 code supported by ECB.");

        int start = idx + ratesKey.length();
        int end   = json.indexOf('}', start);
        String rateStr = json.substring(start, end).replaceAll("[^0-9.]", "");
        return Double.parseDouble(rateStr);
    }

    /** Minimal HTTP GET → returns response body as String. */
    private static String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8_000);
        conn.setReadTimeout(8_000);
        conn.setRequestProperty("Accept", "application/json");

        int status = conn.getResponseCode();
        if (status != 200)
            throw new Exception("HTTP " + status + " from API.");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br =
                new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        conn.disconnect();
        return sb.toString();
    }

    // ═════════════════════════════════════════════════════════════════════
    // DISPLAY
    // ═════════════════════════════════════════════════════════════════════

    private static void printResult(String base, String target,
                                    double amount, double converted, double rate) {
        String bSym = symbolOf(base);
        String tSym = symbolOf(target);

        System.out.println();
        System.out.println("  ╔════════════════════════════════════════════════════╗");
        System.out.printf ("  ║  CONVERSION RESULT          %-22s║%n",
                "(" + LocalDate.now() + ")");
        System.out.println("  ╠════════════════════════════════════════════════════╣");
        System.out.printf ("  ║  From  :  %-6s  %s%-23.2f  ║%n", base, bSym, amount);
        System.out.printf ("  ║  To    :  %-6s  %s%-23.2f  ║%n", target, tSym, converted);
        System.out.println("  ╠════════════════════════════════════════════════════╣");
        System.out.printf ("  ║  Rate  :  1 %s = %s %.6f %-18s║%n",
                base, tSym, rate, target);
        System.out.printf ("  ║  Rate  :  1 %s = %s %.6f %-18s║%n",
                target, bSym, 1.0 / rate, base);
        System.out.println("  ╠════════════════════════════════════════════════════╣");
        System.out.printf ("  ║  %s%-6.2f  =  %s%-6.2f %-27s║%n",
                bSym, amount, tSym, converted, "");
        System.out.println("  ╚════════════════════════════════════════════════════╝");
        System.out.println("  Data source: European Central Bank via Frankfurter API");
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║       REAL-TIME  CURRENCY  CONVERTER             ║");
        System.out.println("  ║   Powered by Frankfurter API (ECB rates)         ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("  ┌──────────────────────────────────────────────────┐");
        System.out.printf ("  │  %-48s│%n", "  " + title);
        System.out.println("  └──────────────────────────────────────────────────┘");
    }

    private static void printWarn(String msg) {
        System.out.println("  ⚠  " + msg);
    }

    private static void printFooter() {
        System.out.println();
        System.out.println("  Thank you for using Currency Converter. Goodbye!");
        System.out.println();
    }

    // ═════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════════════

    private static String symbolOf(String code) {
        return SYMBOLS.getOrDefault(code, code + " ");
    }

    private static double readPositiveDouble() {
        while (true) {
            try {
                double v = Double.parseDouble(sc.nextLine().trim());
                if (v > 0) return v;
                System.out.print("  ⚠  Amount must be positive. Try again : ");
            } catch (NumberFormatException e) {
                System.out.print("  ⚠  Invalid number. Enter a numeric amount : ");
            }
        }
    }
}
