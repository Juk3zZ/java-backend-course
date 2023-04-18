package ru.tinkoff.edu.java.scrapperjooq;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;

public class JooqPGCodeGenerator extends PostgreSqlEnvironment {

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration()
                .withJdbc(new Jdbc()
                        .withDriver(POSTGRESQL_CONTAINER.getDriverClassName())
                        .withUrl(POSTGRESQL_CONTAINER.getJdbcUrl())
                        .withUser(POSTGRESQL_CONTAINER.getUsername())
                        .withPassword(POSTGRESQL_CONTAINER.getPassword()))
                .withGenerator(new Generator()
                        .withDatabase(new Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withInputSchema("app")
                        )
                        .withTarget(new Target()
                                .withPackageName("ru.tinkoff.edu.java.scrapper.domain.jooq")
                                .withDirectory("scrapper/src/main/java")));

        GenerationTool.generate(configuration);
    }
}
