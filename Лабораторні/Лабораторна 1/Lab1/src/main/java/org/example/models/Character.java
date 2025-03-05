package org.example.models;

import java.util.List;

public record Character(
        String id,
        String name,
        List<String> alternateNames,
        String species,
        String gender,
        String house,
        String dateOfBirth,
        Integer yearOfBirth,
        boolean wizard,
        String ancestry,
        String eyeColour,
        String hairColour,
        Wand wand,
        String patronus,
        boolean hogwartsStudent,
        boolean hogwartsStaff,
        String actor, List<String> alternateActors,
        boolean alive,
        String image) {
}
