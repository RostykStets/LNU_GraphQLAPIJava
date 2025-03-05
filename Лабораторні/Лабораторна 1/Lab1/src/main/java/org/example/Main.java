package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.models.Character;
import org.example.models.Spell;

import java.io.File;
import java.io.IOException;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private final String CHARACTERS_JSON_PATH = "resources/data/characters.json";
    private final String SPELLS_JSON_PATH = "resources/data/spells.json";
    private final String SPELLS_QUERY_PATH = "resources/graphql/queries/spells-query.graphql";
    private final String SPELL_BY_NAME_JSON_PATH = "resources/graphql/queries/spell_by_name-query.graphql";

    List<Character> characters;
    List<Spell> spells;

    public static void main(String[] args) {

    }

    private void loadData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.characters = objectMapper.readValue(new File(CHARACTERS_JSON_PATH), new TypeReference<>() {});
        this.spells = objectMapper.readValue(new File(SPELLS_JSON_PATH), new TypeReference<>() {});
    }
}