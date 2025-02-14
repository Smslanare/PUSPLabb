package etsf20.basesystem;

import com.zaxxer.hikari.HikariDataSource;
import etsf20.basesystem.domain.models.User;
import etsf20.basesystem.domain.models.UserRole;
import etsf20.basesystem.domain.repositories.Repositories;
import etsf20.basesystem.domain.repositories.UserRepository;
import etsf20.basesystem.persistance.Database;
import etsf20.basesystem.web.ValidationException;
import etsf20.basesystem.web.controllers.*;
import etsf20.basesystem.web.pages.ErrorPage;
import etsf20.basesystem.web.pages.Session;
import etsf20.basesystem.web.pages.TemplatePage;
import etsf20.basesystem.web.pages.session.LoginPage;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import org.eclipse.jetty.server.session.*;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static io.javalin.apibuilder.ApiBuilder.path;

/**
 * Main application class
 */
public class Main {

    private static boolean production = true;

    /**
     * Database initialization routine
     * @throws SQLException if creation fails for any reason
     */
    public static void createSchemaIfNotExists(Connection conn) throws SQLException {
        try(var stmt = conn.createStatement()) {
            Logger logger = LoggerFactory.getLogger(Main.class);

            // Check if a table named "journals" exist in the database - by default it does not.
            String sql = "SELECT COUNT(*) > 0 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'NOTES'";
            ResultSet result = stmt.executeQuery(sql);
            if(result.next()) {
                if(!result.getBoolean(1)) {
                    // We could not find the journals table and assumes the database requires initialization
                    logger.info("Initializing database schema...");

                    try(InputStream scriptResource = Main.class.getResourceAsStream("/schema.sql")) {
                        RunScript.execute(conn, new InputStreamReader(Objects.requireNonNull(scriptResource)));

                        // Add default users
                        User admin = User.createWithPassword(
                                "admin",
                                "Administrator",
                                "Admin@1234",
                                UserRole.ADMIN);

                        UserRepository userRepo = Repositories.from(conn).users();
                        userRepo.create(admin);

                        // Save changes
                        conn.commit();
                        logger.info("Success");
                    }
                    catch (IOException ex) {
                        throw new IOError(ex);
                    }
                } else {
                    // Database is assumed to be initialized
                    logger.info("Found existing database.");
                }
            }
        }
    }

    /**
     * Constuct SQL based session handler
     * @param driver JDBC Driver name
     * @param url JDBC connection string
     * @return session handler
     */
    public static SessionHandler getSqlSessionHandler(HikariDataSource pool, String driver, String url, boolean mixedMode) {
        SessionHandler sessionHandler = new SessionHandler();
        SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
        sessionCache.setSessionDataStore(
                getJdbcDataStoreFactory(pool, driver, url, mixedMode).getSessionDataStore(sessionHandler)
        );
        sessionHandler.setSessionCache(sessionCache);
        sessionHandler.setHttpOnly(true);
        sessionHandler.setMaxInactiveInterval(20*60); // 20 minutes
        return sessionHandler;
    }

    /**
     * Helper method to create JDBC based data store factory
     * @param driver JDBC Driver name
     * @param url JDBC connection string
     * @return JDBC Session Data Store Factory
     */
    private static JDBCSessionDataStoreFactory getJdbcDataStoreFactory(HikariDataSource pool, String driver, String url, boolean mixedMode) {
        DatabaseAdaptor databaseAdaptor = new DatabaseAdaptor();
        databaseAdaptor.setDriverInfo(driver, url);


        // Use our connection pool for performance
        databaseAdaptor.setDatasource(pool);

        JDBCSessionDataStoreFactory jdbcSessionDataStoreFactory = new JDBCSessionDataStoreFactory();
        jdbcSessionDataStoreFactory.setDatabaseAdaptor(databaseAdaptor);
        return jdbcSessionDataStoreFactory;
    }

    /**
     * Configure javalin specifics such as how to handle sessions, setup template engine, static files and build routes
     * @param javalinConfig Javalin configuration
     */
    private static void configure(JavalinConfig javalinConfig, Config config) {
        if(config.isMixedMode()) {
            String url = config.getJdbcUrl();
            String path = url.substring(url.indexOf("h2:"));
            int paramPos = path.indexOf(';');
            if(paramPos != -1) {
                path = path.substring(3,paramPos);
            }

            LoggerFactory.getLogger(Main.class).info("Mixed mode Database/Connection URL: \n\n{}\n", "jdbc:h2:" + Paths.get(path).toAbsolutePath().normalize() + ";AUTO_SERVER=TRUE");
        }

        HikariDataSource pool = Database.createPool(config);

        try(Connection conn = pool.getConnection()) {
            createSchemaIfNotExists(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Persistent session handling - required to keep sessions between restarts of server
        javalinConfig.jetty.modifyServletContextHandler(handler -> {
            SessionHandler sessionHandler = getSqlSessionHandler(
                    pool,
                    config.getDbDriver(),
                    config.getJdbcUrl() + ";USER=" + config.getDbUsername() +";PASSWORD=" + config.getDbPassword(),
                    config.isMixedMode()
            );
            handler.setSessionHandler(sessionHandler);
        });

        JavalinJte jte;

        // Hack: detect if we are running standalone compiled version or from a development
        //       version started by an IDE - works in most situations.

        String path = Objects.requireNonNull(Main.class.getResource("Main.class")).toString();
        Main.production = path.startsWith("jar:");
        if(Main.production) {
            // We are running from a compiled version - all templates are precompiled for performance
            jte = new JavalinJte(TemplateEngine.createPrecompiled(ContentType.Html));
        } else {
            // We are running from a development version
            jte = new JavalinJte();
        }

        javalinConfig.fileRenderer(jte);

        javalinConfig.staticFiles.add(staticFiles -> {
            staticFiles.hostedPath = "/";                   // change to host files on a subpath, like '/assets'
            staticFiles.directory = "/public";              // the directory where your files are located
            staticFiles.location = Location.CLASSPATH;      // Location.CLASSPATH (jar) or Location.EXTERNAL (file system)
            staticFiles.precompress = production;           // if the files should be pre-compressed and cached in memory (optimization)
            staticFiles.headers = Map.of(Header.CACHE_CONTROL, "max-age=86400"); //24 hour cache
        });

        // Enables web jars
        javalinConfig.staticFiles.enableWebjars();

        javalinConfig.router.apiBuilder(() -> {
            path("/user", UserController::configure);
            path("/session", SessionController::configure);
            path("/admin", AdminController::configure);
            path("/notes", NoteController::configure);
            path("/projects", ProjectController::configure);
        });

        javalinConfig.appData(Config.Key, config);
        javalinConfig.appData(Database.PoolKey, pool);

        javalinConfig.events(event -> {
            event.serverStopping(pool::close);
        });

        javalinConfig.requestLogger.http((ctx, ms) -> {
            String timeString = String.format("%7s", String.format("%5.2f",ms));

            System.out.println(
                "[ " + Instant.now().toString() + " | "
                     + ctx.ip() + " | "
                     + timeString + " ms" + " | "
                     + ctx.statusCode() + " ] "
                     + ctx.method() + " "
                     + ctx.path()
            );
        });
    }

    /**
     * Access control - verify that client is authorized and has required privileges to access resource
     * @param ctx context
     */
    private static void accessControl(Context ctx) {
        // Access control
        Session session = Session.from(ctx);

        // Check that a logged-in user is still valid
        Repositories repos = Repositories.from(ctx);
        if(session.isLoggedIn() && repos.users().get(session.username()).isEmpty()) {
            // User has been removed - logout the user.
            // This will invalidate current session and all its data.
            session.logout();

            // Create new clean session for the continuing code
            session = Session.from(ctx);
        }

        var userRole = session.userRole();

        if(ctx.routeRoles().isEmpty()) {
            // No permissions required
            return;
        }

        if (!ctx.routeRoles().contains(userRole)) { // routeRoles are provided through the routing interface at startup
            if(!Session.from(ctx).isLoggedIn()) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                LoginPage loginPage = new LoginPage(ctx, URLEncoder.encode(ctx.fullUrl(), StandardCharsets.UTF_8));
                loginPage.render();
                ctx.skipRemainingHandlers();
                return;
            }

            ctx.status(HttpStatus.FORBIDDEN);
            ErrorPage errorPage = new ErrorPage(ctx, "403 Forbidden", "You lack the privileges to access this page.");
            errorPage.render();
            ctx.skipRemainingHandlers();
        }
    }

    /**
     * Client has sent invalid parameters - generate error page
     * @param e   exception
     * @param ctx context
     */
    private static void validationException(ValidationException e, Context ctx) {
        ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
        ctx.render("error.jte", Collections.singletonMap("page", new ErrorPage(ctx, e.getTitle(), e.getMessage())));
        LoggerFactory.getLogger(Main.class).info("Unprocessable content", e);
    }

    /**
     * Client requests a page that could not be found
     * @param ctx context
     */
    private static void notFound(Context ctx) {
        ErrorPage errorPage = new ErrorPage(ctx, "404 Not Found", "Could not find requested page.");
        errorPage.render();
    }

    /**
     * Index page
     * @param ctx context
     */
    private static void index(Context ctx) {
        TemplatePage.from(ctx, "index.jte").render();
    }

    private static void exceptionHandler(Throwable ex, Context ctx) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        String errorMessage = "request failed: " + ctx.fullUrl();
        LoggerFactory.getLogger(Main.class).error(errorMessage, ex);

        String sb = "<html><head><link href=\"/webjars/bootstrap/5.3.3/css/bootstrap.min.css\" rel=\"stylesheet\" /></head><body><div class=\"container-lg\">" +
                "<h1 class=\"mt-3\">Server Error</h1>" +
                "<p>Exception was thrown during request handling at path: <b>" + ctx.path() + " </b></p><pre class=\"border bg-light p-2\">" +
                sw +
                "</pre></div></body></html>";

        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        ctx.html(sb);
    }

    /**
     * Javalin App builder
     */
    public static Javalin javalin(Config config) {
        var server = Javalin.create(javalinConfig -> configure(javalinConfig, config))
                            .beforeMatched(Main::accessControl)
                            .get("/", Main::index, UserRole.loggedIn())
                            .exception(ValidationException.class, Main::validationException)
                            .error(HttpStatus.NOT_FOUND, "html", Main::notFound)
                            .after(Database::dispose); // rollbacks any non-commited transaction and closes active connections

        if(!Main.production) {
            // Show exceptions for the user
            server = server.exception(Exception.class, Main::exceptionHandler);
        }

        return server;
    }

    /**
     * Main entry point
     *
     * @exception SQLException if database could not be created
     */
    public static void main(String[] args) throws SQLException {
        System.out.println("ETSF20 Base Journal System");

        Config config = Config.defaultConfiguration();
        Javalin javalin = javalin(config);
        javalin.start(config.getPort());

        // Do a clean shutdown when the JVM terminates
        Runtime.getRuntime().addShutdownHook(new Thread(javalin::stop));
    }
}
