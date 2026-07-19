package com.payflow.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ExpenseCategorizationService {

    public static final String FOOD_DINING = "Food & Dining";
    public static final String GROCERIES = "Groceries";
    public static final String SHOPPING = "Shopping";
    public static final String BILLS_UTILITIES = "Bills & Utilities";
    public static final String ENTERTAINMENT = "Entertainment";
    public static final String TRAVEL = "Travel & Transport";
    public static final String RENT = "Rent & Housing";
    public static final String HEALTHCARE = "Healthcare";
    public static final String EDUCATION = "Education";
    public static final String INVESTMENT = "Investment & Savings";
    public static final String TRANSFER = "Transfer";
    public static final String OTHERS = "Others";

    /** Ordered so the first matching category wins; order reflects specificity. */
    private static final Map<String, String[]> KEYWORD_RULES = new LinkedHashMap<>();

    static {
        KEYWORD_RULES.put(FOOD_DINING, new String[]{
                "swiggy", "zomato", "restaurant", "cafe", "coffee", "food", "dining", "eatery",
                "pizza", "burger", "dominos", "mcdonald", "kfc", "starbucks", "dine", "lunch", "dinner", "breakfast"
        });
        KEYWORD_RULES.put(GROCERIES, new String[]{
                "grocery", "groceries", "bigbasket", "blinkit", "zepto", "dmart", "supermarket", "kirana", "vegetable", "milk"
        });
        KEYWORD_RULES.put(SHOPPING, new String[]{
                "amazon", "flipkart", "myntra", "ajio", "shopping", "mall", "store", "purchase", "nykaa", "meesho"
        });
        KEYWORD_RULES.put(BILLS_UTILITIES, new String[]{
                "electricity", "water bill", "gas bill", "broadband", "wifi", "internet", "dth", "fastag",
                "recharge", "utility", "bill payment", "credit card bill"
        });
        KEYWORD_RULES.put(ENTERTAINMENT, new String[]{
                "netflix", "prime video", "hotstar", "spotify", "movie", "cinema", "bookmyshow", "pvr", "inox", "gaming", "game"
        });
        KEYWORD_RULES.put(TRAVEL, new String[]{
                "uber", "ola", "rapido", "irctc", "train", "flight", "airlines", "indigo", "cab", "taxi",
                "petrol", "diesel", "fuel", "toll", "metro", "bus", "travel", "trip", "hotel", "makemytrip", "goibibo"
        });
        KEYWORD_RULES.put(RENT, new String[]{
                "rent", "landlord", "lease", "housing society", "maintenance charge"
        });
        KEYWORD_RULES.put(HEALTHCARE, new String[]{
                "hospital", "pharmacy", "medical", "clinic", "doctor", "medicine", "apollo", "diagnostic", "health"
        });
        KEYWORD_RULES.put(EDUCATION, new String[]{
                "school", "college", "university", "tuition", "course", "udemy", "coursera", "fees", "exam"
        });
        KEYWORD_RULES.put(INVESTMENT, new String[]{
                "mutual fund", "sip", "stock", "zerodha", "groww", "investment", "insurance premium", "fd", "fixed deposit"
        });
    }

    /**
     * @param note          the payment note entered by the user (nullable)
     * @param counterpartyIdentifier the VPA, name, or biller name to also scan for keywords (nullable)
     * @param transactionType the Transaction.TransactionType name(), used as a fallback signal
     */
    public String categorize(String note, String counterpartyIdentifier, String transactionType) {
        String haystack = (safe(note) + " " + safe(counterpartyIdentifier)).toLowerCase();

        for (Map.Entry<String, String[]> rule : KEYWORD_RULES.entrySet()) {
            for (String keyword : rule.getValue()) {
                if (haystack.contains(keyword)) {
                    return rule.getKey();
                }
            }
        }

        // Fallback signals from the transaction's own type, when no keyword matched.
        if (transactionType != null) {
            return switch (transactionType) {
                case "WALLET_ADD", "WALLET_TO_BANK", "SELF_TRANSFER" -> TRANSFER;
                case "BILL_PAYMENT" -> BILLS_UTILITIES;
                default -> OTHERS;
            };
        }
        return OTHERS;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}