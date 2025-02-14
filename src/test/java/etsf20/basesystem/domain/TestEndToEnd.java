package etsf20.basesystem.domain;

import etsf20.basesystem.Config;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import etsf20.basesystem.Main;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleContains;

/**
 * Example of a Selenium end-to-end test
 * <p>
 * This test will start a real browser in background, command it to do things just as a real user would.
 * Known issue is that we did not get it to work for a CI pipeline, but works in desktop environments.
 */
public class TestEndToEnd {
    Javalin app;

    @BeforeEach
    void setUp() throws SQLException {
        Config testConfig = Config.testConfiguration();
        this.app = Main.javalin(testConfig);
    }

    @AfterEach
    void tearDown() throws SQLException {
        this.app.stop();
    }

    @Test
    public void login_ui_test() {
        JavalinTest.test(app, (server, client) -> {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox"); // required for CI pipeline
            options.addArguments("--disable-dev-shm-usage"); // required for CI pipeline
            options.addArguments("--disable-gpu");
            WebDriver driver = new ChromeDriver(options);
            System.out.println(client.getOrigin());
            driver.get(client.getOrigin() + "/");

            // Wait for the base page to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(titleContains("Base Journal System - Logon"));

            // Fill in the login form
            WebElement username = driver.findElement(By.id("username"));
            username.click();
            username.sendKeys("admin");

            WebElement password = driver.findElement(By.id("password"));
            password.click();
            password.sendKeys("Admin@1234");

            // Press the login button
            driver.findElement(By.id("login")).click();

            // Wait for the success page to load
            WebDriverWait waitLogin = new WebDriverWait(driver, Duration.ofSeconds(5));
            waitLogin.until(titleContains("Start"));

            // Make sure it contains what we expect
            assertTrue(driver.getPageSource().contains("Welcome Administrator!"));

            driver.quit();
        });
    }
}
