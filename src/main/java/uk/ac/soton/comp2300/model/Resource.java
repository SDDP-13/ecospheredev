package uk.ac.soton.comp2300.model;

import java.util.Optional;

public enum Resource {
    MONEY,
    WOOD,
    METAL;

    public static Optional<Resource> fromString(String value) {     // Constructor with Optional wrapper in case String doesn't match any enum
        try {
            return Optional.of(Resource.valueOf(value.toUpperCase().trim()));   // normalises the String input
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}
