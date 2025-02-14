package etsf20.basesystem;

import io.javalin.config.Key;

/**
 * Contains configuration for server settings.
 *
 * <p>In a "real" application this would be a more sophisticated solution, the config parameters should come from a file
 * or something that is actually possible to configure without recompiling the application.
 * There should also be support for error checking.
 * </p>
 */
public class Config {
    public static final Key<Config> Key = new Key<>("db.config");

    /**
     * Will expose the internal H2 database as a server allowing multiple clients, even outside the base system.
     */
    private static final boolean DEFAULT_MIXED_MODE = true;
    private static final int DEFAULT_PORT = 8080;
    private static final String DATABASE_DRIVER = "org.h2.Driver";
    private static final String DATABASE_JDBC_URL = "jdbc:h2:./data";
    private static final String DATABASE_USERNAME = "sa";
    private static final String DATABASE_PASSWORD = "" ;

    public static Config defaultConfiguration() {
        return new Config(
                DEFAULT_PORT,
                DATABASE_DRIVER,
                DATABASE_JDBC_URL,
                DATABASE_USERNAME,
                DATABASE_PASSWORD,
                DEFAULT_MIXED_MODE
        );
    }

    public static Config testConfiguration() {
        return new Config(
                DEFAULT_PORT,
                DATABASE_DRIVER,
                "jdbc:h2:mem:testdb",
                DATABASE_USERNAME,
                DATABASE_PASSWORD,
                false
        );
    }

    public static Config testConfigurationSingleConnection() {
        return new Config(
                DEFAULT_PORT,
                DATABASE_DRIVER,
                "jdbc:h2:mem:",
                DATABASE_USERNAME,
                DATABASE_PASSWORD,
                false
        );
    }

    private int port;
    private final String jdbcUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final String dbDriver;
    private final boolean mixedMode;

    /**
     *
     * @param port       port to listen to
     * @param dbDriver   jdbc driver path
     * @param jdbcUrl    jdbc connection string
     * @param dbUsername database username
     * @param dbPassword database password
     */
    private Config(int port, String dbDriver, String jdbcUrl, String dbUsername, String dbPassword, boolean mixedMode) {
        if(mixedMode) {
            jdbcUrl += ";AUTO_SERVER=TRUE";
        }

        this.port = port;
        this.dbDriver = dbDriver;
        this.jdbcUrl = jdbcUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.mixedMode = mixedMode;
    }

    /** Get port to listen on for the web server */
    public int getPort() {
        return port;
    }

    /** Change the port to listen to */
    public void setPort(int port) {
        this.port = port;
    }

    /** Get jdbc url */
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    /** Get database username */
    public String getDbUsername() {
        return dbUsername;
    }

    /** Get database password */
    public String getDbPassword() {
        return dbPassword;
    }

    /** Is using mixed mode */
    public boolean isMixedMode() {
        return mixedMode;
    }
}