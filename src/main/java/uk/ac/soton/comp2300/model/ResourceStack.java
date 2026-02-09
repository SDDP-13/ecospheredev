package uk.ac.soton.comp2300.model;

/*
Class that has an enum type and a quantity to represent an amount of resources
 */

public class ResourceStack {
    private Resource type;
    private int amount;

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

    @Override
    public String toString() {
        return type + " " + amount;
    }
}

