package ua.edu.lnu.stelmashchuk;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import ua.edu.lnu.stelmashchuk.ecommerce.Product;
import ua.edu.lnu.stelmashchuk.ecommerce.User;
import ua.edu.lnu.stelmashchuk.utils.FileUtils;
import ua.edu.lnu.stelmashchuk.utils.JsonUtils;

import java.util.List;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class Main {

    private static final String HELLO_SCHEMA_PATH = "/graphql/schema/hello.graphqls";
    private static final String HELLO_QUERY_PATH = "/graphql/query/hello-query.graphql";

    private static final String ECOMMERCE_SCHEMA_PATH = "/graphql/schema/e-commerce.graphqls";
    private static final String PRODUCTS_QUERY_PATH = "/graphql/query/e-commerce/products-query.graphql";
    private static final String USERS_QUERY_PATH = "/graphql/query/e-commerce/users-query.graphql";
    private static final String USERS_AND_PRODUCTS_QUERY_PATH = "/graphql/query/e-commerce/users-and-products-query.graphql";

    public static void main(String[] args) {
        helloWorld();
        //helloWorldSchemaByCode();
        //eCommerce();
    }

    private static void helloWorld() {
        TypeDefinitionRegistry typeDefinitionRegistry = parseSchema(HELLO_SCHEMA_PATH);
        RuntimeWiring runtimeWiring = buildRuntimeWiring();
        GraphQL graphQL = createGraphQLEntryPoint(typeDefinitionRegistry, runtimeWiring);

        String query = FileUtils.readFileContent(HELLO_QUERY_PATH);
        ExecutionResult executionResult = graphQL.execute(query);
        System.out.println(JsonUtils.serializeToJson(executionResult.toSpecification()));
    }

    private static void helloWorldSchemaByCode() {
        GraphQLSchema graphQLSchema = buildHelloWorldSchema();
        GraphQL graphQL = createGraphQLEntryPoint(graphQLSchema);

        String query = FileUtils.readFileContent(HELLO_QUERY_PATH);
        ExecutionResult executionResult = graphQL.execute(query);
        System.out.println(JsonUtils.serializeToJson(executionResult.toSpecification()));
    }

    private static void eCommerce() {
        TypeDefinitionRegistry typeDefinitionRegistry = parseSchema(ECOMMERCE_SCHEMA_PATH);
        RuntimeWiring runtimeWiring = buildRuntimeWiringECommerce();
        GraphQL graphQL = createGraphQLEntryPoint(typeDefinitionRegistry, runtimeWiring);

        String productsQuery = FileUtils.readFileContent(PRODUCTS_QUERY_PATH);
        ExecutionResult executionResult = graphQL.execute(productsQuery);
        System.out.println(JsonUtils.serializeToJson(executionResult.toSpecification()));

        String usersQuery = FileUtils.readFileContent(USERS_QUERY_PATH);
        executionResult = graphQL.execute(usersQuery);
        System.out.println(JsonUtils.serializeToJson(executionResult.toSpecification()));

        String usersAndProductsQuery = FileUtils.readFileContent(USERS_AND_PRODUCTS_QUERY_PATH);
        executionResult = graphQL.execute(usersAndProductsQuery);
        System.out.println(JsonUtils.serializeToJson(executionResult.toSpecification()));
    }

    private static TypeDefinitionRegistry parseSchema(String schemaPath) {
        String schema = FileUtils.readFileContent(schemaPath);
        SchemaParser schemaParser = new SchemaParser();
        return schemaParser.parse(schema);
    }

    private static GraphQLSchema buildHelloWorldSchema() {
        GraphQLObjectType queryType = newObject()
                .name("Query")
                .field(newFieldDefinition()
                        .name("hello")
                        .type(GraphQLString))
                .build();

        GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry.newCodeRegistry()
                .dataFetcher(
                        coordinates("Query", "hello"),
                        (DataFetcher<?>) environment -> "world")
                .build();

        return GraphQLSchema.newSchema()
                .query(queryType)
                .codeRegistry(codeRegistry)
                .build();
    }

    private static RuntimeWiring buildRuntimeWiring() {
        return newRuntimeWiring()
                .type("Query",
                        builder -> builder.dataFetcher(
                                "hello",
                                new StaticDataFetcher("world"))
                )
                .build();
    }

    private static RuntimeWiring buildRuntimeWiringECommerce() {
        return newRuntimeWiring()
                .type("Query",
                        builder -> builder.dataFetcher(
                                "products",
                                new StaticDataFetcher(loadProducts()))
                )
                .type("Query",
                        builder -> builder.dataFetcher(
                                "user",
                                buildUserDataFetcher())
                )
                .build();
    }

    private static GraphQL createGraphQLEntryPoint(TypeDefinitionRegistry typeDefinitionRegistry,
                                                   RuntimeWiring runtimeWiring) {
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private static GraphQL createGraphQLEntryPoint(GraphQLSchema graphQLSchema) {
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private static DataFetcher<User> buildUserDataFetcher() {
        return new DataFetcher<>() {
            @Override
            public User get(DataFetchingEnvironment environment) {
                String userId = environment.getArgument("id");
                if (userId == null) {
                    throw new IllegalArgumentException("User id is required");
                }
                return loadUsers().stream()
                        .filter(user -> user.id().equals(userId))
                        .findFirst()
                        .orElse(null);
            }
        };
    }

    private static List<Product> loadProducts() {
        return JsonUtils.loadListFromJsonFile("/data/products.json", Product[].class);
    }

    private static List<User> loadUsers() {
        return JsonUtils.loadListFromJsonFile("/data/users.json", User[].class);
    }
}
