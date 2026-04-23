package uk.ac.soton.comp2300.model;

/*
Class that has an enum type and a quantity to represent an amount of resources
 */

public class ResourceStack {
    private Resource type;
    private int amount;
    private static final int max_stored = 99999;

    public ResourceStack(Resource type, int amount) {
        this.type = type;
        this.amount = amount;

    }

    public Resource getType() {
        return type;
    }
    public int getAmount() {
        return amount;
    }
    public void add(int value) {
        this.amount = Math.min(max_stored, this.amount + value); }
    public void subtract(int value) {
        this.amount = Math.max(0, this.amount - value); }


    @Override
    public String toString() {
        return type + " " + amount;
    }
}

