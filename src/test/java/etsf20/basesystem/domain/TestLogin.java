package etsf20.basesystem.domain;

import etsf20.basesystem.Config;
import etsf20.basesystem.Main;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.CookieManager;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Black-box testing of a login
 */
public class TestLogin {
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
    public void testLogin() {
        JavalinTest.test(app, (server, client) -> {

            // Maintain our cookies that also tracks the session
            BasicCookieStore cookieStore = new BasicCookieStore();

            try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build()) {
                // 1. Request start page to get a login page.
                ClassicHttpRequest loginRequest = ClassicRequestBuilder.get(client.getOrigin() + "/").build();

                // Verify that we are expected to log in, i.e. we must authenticate.
                // This also sets the session cookie.
                assertEquals(401, httpclient.execute(loginRequest, HttpResponse::getCode));

                // 2. Post login details
                var loginPostRequest = ClassicRequestBuilder.post(client.getOrigin() + "/session/login")
                        .setEntity(new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair("username", "admin"),
                                new BasicNameValuePair("password", "Admin@1234"))))
                        .build();

                // 3. Verify that we successfully logged in!
                record LoginResponse(int code, String responseHtml) {}
                var resp = httpclient.execute(loginPostRequest,
                        response -> new LoginResponse(
                                response.getCode(),
                                EntityUtils.toString(response.getEntity())
                        )
                );

                assertEquals(200, resp.code);
                assertTrue(resp.responseHtml.contains("Welcome Administrator!"));
            }
        });
    }

}
