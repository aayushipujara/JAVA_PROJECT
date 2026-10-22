package com.signbridge.model;

public class SignItem {
    private int id;
    private String symbol;
    private String category; // "Alphabet", "Number", "Word"
    private String description;
    private String gestureType; // Vector key or visual template identifier
    private String hint;

    public SignItem() {}

    public SignItem(int id, String symbol, String category, String description, String gestureType, String hint) {
        this.id = id;
        this.symbol = symbol;
        this.category = category;
        this.description = description;
        this.gestureType = gestureType;
        this.hint = hint;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGestureType() { return gestureType; }
    public void setGestureType(String gestureType) { this.gestureType = gestureType; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    @Override
    public String toString() {
        return symbol + " (" + category + ")";
    }
}
