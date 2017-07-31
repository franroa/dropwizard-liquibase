package com.franroa.liquibase;

import com.franroa.liquibase.models.MyModel;
import com.franroa.liquibase.resources.MyResource;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Environment;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.javalite.activejdbc.Base;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.sql.DataSource;
import javax.ws.rs.InternalServerErrorException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;

public class LiquibaseApplication extends Application<LiquibaseConfiguration> {

    private static final String MIGRATIONS_MASTER_FILE_PATH = "changelog/master.xml";

    private Environment environment;
    private LiquibaseConfiguration configuration;
    private ManagedDataSource dataSource;

    public static void main(final String[] args) throws Exception {
        new LiquibaseApplication().run(args);
    }

    public void run(final LiquibaseConfiguration configuration, final Environment environment) throws Exception {
        this.configuration = configuration;
        this.environment = environment;

        initializeDb();
        runMigrations();

        environment.jersey().register(MyResource.class);
        configureCors(environment);
    }

    private void initializeDb() throws ClassNotFoundException {
        DataSourceFactory dataSourceFactory = configuration.getDataSourceFactory();
        dataSource = dataSourceFactory.build(environment.metrics(), "pool");

        if (!Base.hasConnection()) {
            Base.open(dataSource);

            try {
                java.sql.Connection connection = Base.connection();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new InternalServerErrorException();
            }
        }

    }

    private void runMigrations() {
        try {
            if (! Base.hasConnection()) {
                Base.open(dataSource);

                try {
                    java.sql.Connection connection = Base.connection();
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new InternalServerErrorException();
                }
            }
            Liquibase migrator = new Liquibase(MIGRATIONS_MASTER_FILE_PATH, new ClassLoaderResourceAccessor(), new JdbcConnection(Base.connection()));
            migrator.update("");
        } catch (LiquibaseException e) {
            e.printStackTrace();
        } finally {
            Base.close();
        }
    }

    private void configureCors(Environment environment) {
        FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filter.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        filter.setInitParameter("allowCredentials", "true");
    }
}

