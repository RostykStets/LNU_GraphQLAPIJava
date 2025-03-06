package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.*;
import org.example.models.Character;
import org.example.models.Spell;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class Main {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static final String CHARACTERS_JSON_PATH = "./src/main/resources/data/characters.json";
    private static final String SPELLS_JSON_PATH = "./src/main/resources/data/spells.json";
    //private static final String HARRY_POTTER_SCHEMA_PATH = "/home/rostyk/Desktop/uni/IV course/II semestr/LNU_GraphQLAPIJava/Лабораторні/Лабораторна 1/Lab1/src/main/resources/graphql/schemas/harry_potter.graphqls";
    private static final String HARRY_POTTER_SCHEMA_PATH = "./src/main/resources/graphql/schemas/harry_potter.graphqls";

    private static final String SPELLS_QUERY_PATH = "./src/main/resources/graphql/queries/spells_query.graphql";
    private static final String SPELL_BY_NAME_QUERY_PATH = "./src/main/resources/graphql/queries/spell_by_name_query.graphql";
    private static final String CHARACTER_BY_NAME_QUERY_PATH = "./src/main/resources/graphql/queries/character_by_name_query.graphql";
    private static final String CHARACTERS_BY_HOUSE_AND_GENDER_QUERY_PATH = "./src/main/resources/graphql/queries/characters_by_house_and_gender_query.graphql";
    private static final String ADD_SPELL_MUTATION_QUERY_PATH = "./src/main/resources/graphql/queries/add_spell_mutation.graphql";

    static List<Character> characters = List.of();
    static List<Spell> spells = List.of();

    public static void main(String[] args) throws IOException {
        loadCharactersData();
        loadSpellsData();

        //executeSpellsQuery();
        //executeSpellByNameQuery();
        //executeCharacterByNameQuery();
        //executeCharactersByHouseAndGenderQuery();
        //executeAddSpellMutation();
    }

    private static void executeSpellsQuery() throws IOException {
        executeQuery(buildSpellsWiring(), SPELLS_QUERY_PATH);
    }

    private static void executeSpellByNameQuery() throws IOException {
        executeQuery(buildSpellByNameWiring(), SPELL_BY_NAME_QUERY_PATH);
    }

    private static void executeCharacterByNameQuery() throws IOException {
        executeQuery(buildCharacterByNameWiring(), CHARACTER_BY_NAME_QUERY_PATH);
    }

    private static void executeCharactersByHouseAndGenderQuery() throws IOException {
        executeQuery(buildCharactersByHouseAndGenderWiring(), CHARACTERS_BY_HOUSE_AND_GENDER_QUERY_PATH);
    }

    private static void executeAddSpellMutation() throws IOException {
        executeQuery(buildAddSpellWiring(), ADD_SPELL_MUTATION_QUERY_PATH);
    }

    private static void executeQuery(RuntimeWiring wiring, String queryPath) throws IOException {
        TypeDefinitionRegistry typeRegistry = parseSchema();
        GraphQL graphQL = buildGraphQL(typeRegistry, wiring);

        String query = FileUtils.readFileContent(queryPath);

        ExecutionResult result = graphQL.execute(query);

        System.out.println(OBJECT_MAPPER.writeValueAsString(result));
    }

    private static void loadCharactersData() throws IOException {
        //loadData(CHARACTERS_JSON_PATH, characters);
        characters = OBJECT_MAPPER.readValue(new File(CHARACTERS_JSON_PATH), new TypeReference<>() {
        });
    }

    private static void loadSpellsData() throws IOException {
        spells = OBJECT_MAPPER.readValue(new File(SPELLS_JSON_PATH), new TypeReference<>() {
        });
    }

    private static TypeDefinitionRegistry parseSchema() {
        String schema = FileUtils.readFileContent(HARRY_POTTER_SCHEMA_PATH);
        SchemaParser schemaParser = new SchemaParser();
        return schemaParser.parse(schema);
    }

    private static GraphQL buildGraphQL(TypeDefinitionRegistry typeDefinitionRegistry, RuntimeWiring runtimeWiring) {
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private static RuntimeWiring buildSpellsWiring() {
        return newRuntimeWiring()
                .type("Query",
                        builder -> builder.dataFetcher(
                                "spells",
                                new StaticDataFetcher(spells))
                )
                .build();
    }

    private static RuntimeWiring buildSpellByNameWiring() {
        return newRuntimeWiring()
                .type("Query",
                        builder -> builder.dataFetcher(
                                "spell",
                                buildSpellByNameDataFetcher())
                )
                .build();
    }

    private static RuntimeWiring buildCharacterByNameWiring() {
        return newRuntimeWiring()
                .type("Query",
                        builder -> builder.dataFetcher(
                                "character",
                                buildCharacterByNameDataFetcher())
                )
                .build();
    }

    private static RuntimeWiring buildCharactersByHouseAndGenderWiring() {
        return newRuntimeWiring()
                .type("Query",
                        builder -> builder.dataFetcher(
                                "charactersByHouseAndGender",
                                buildCharactersByHouseAndGenderDataFetcher())
                )
                .build();
    }

    private static RuntimeWiring buildAddSpellWiring() {
        return newRuntimeWiring()
                .type("Mutation",
                        builder -> builder.dataFetcher(
                                "addSpell",
                                buildAddSpellDataFetcher())
                )
                .build();
    }

    private static DataFetcher<Spell> buildSpellByNameDataFetcher() {
        return environment -> {
            String spellName = environment.getArgument("name");
            if (spellName == null) {
                throw new IllegalArgumentException("Spell name is required");
            }
            return spells.stream()
                    .filter(spell -> spell.name().equals(spellName))
                    .findFirst()
                    .orElse(null);
        };
    }

    private static DataFetcher<Character> buildCharacterByNameDataFetcher() {
        return environment -> {
            String characterName = environment.getArgument("name");
            if (characterName == null) {
                throw new IllegalArgumentException("Character name is required");
            }
            return characters.stream()
                    .filter(character -> character.name().equals(characterName))
                    .findFirst()
                    .orElse(null);
        };
    }

    private static DataFetcher<List<Character>> buildCharactersByHouseAndGenderDataFetcher() {
        return environment -> {
            String characterHouse = environment.getArgument("house");
            String characterGender = environment.getArgument("gender");
            if (characterHouse == null) {
                throw new IllegalArgumentException("Character house is required");
            }
            if (characterGender == null) {
                throw new IllegalArgumentException("Character gender is required");
            }

            return characters.stream()
                    .filter(character -> character.house().equals(characterHouse))
                    .filter(character -> character.gender().equals(characterGender)).toList();
        };
    }

    private static DataFetcher<Spell> buildAddSpellDataFetcher() {
        return environment -> {

            Map<String, Object> input = environment.getArgument("newSpell");
            if (input == null) {
                throw new IllegalArgumentException("Spell parameters are required");
            }

            String id = input.containsKey("id") ? (String) input.get("id") : null;
            if (id == null) {
                throw new IllegalArgumentException("Spell id is required");
            }
            String name = (String) input.get("name");
            if (name == null) {
                throw new IllegalArgumentException("Spell name is required");
            }
            String description = (String) input.get("description");
            if (description == null) {
                throw new IllegalArgumentException("Spell description is required");
            }

            Spell newSpell = new Spell(id, name, description);
            spells.add(newSpell);

            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(SPELLS_JSON_PATH), spells);

            return newSpell;
        };
    }
}