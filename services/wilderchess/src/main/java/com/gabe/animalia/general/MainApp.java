package com.gabe.animalia.general;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.gabe.animalia.ml.server.GameLogger;

// import com.gabe.animalia.ml.HeavyStateTest;
// import com.gabe.animalia.ml.SequentialLogTest;

public class MainApp extends AbstractHandler {
    private static final int PAGE_SIZE = 3000;
    private static final String INDEX_HTML = loadIndex();

    private static String loadIndex() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(MainApp.class.getResourceAsStream("/index.html")))) {
            final StringBuilder page = new StringBuilder(PAGE_SIZE);
            String line = null;

            while ((line = reader.readLine()) != null) {
                page.append(line);
            }

            return page.toString();
        } catch (final Exception exception) {
            return getStackTrace(exception);
        }
    }

    private static String getStackTrace(final Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter, true);
        throwable.printStackTrace(printWriter);

        return stringWriter.getBuffer().toString();
    }

    private static int getPort() {
        String portEnv = System.getProperty("port");
        return portEnv != null ? Integer.parseInt(portEnv) : 5000;
        // return Integer.parseInt(System.getenv().get("PORT"));
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        // ONLY serve index.html if the request is exactly "/" or "/index.html"
        if (!target.equals("/") && !target.equals("/index.html")) {
            return; // Let Jetty try other handlers or return a 404
        }

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println(INDEX_HTML);
    }

    public static void main(String[] args) throws Exception {

        boolean botVsBot = false;
        int numBotGamesAtOnce = 1;
        int totalGameCount = 0;
        ArrayList<Game> games = new ArrayList<Game>();
        if (botVsBot) {
            GameLogger gameLogger = new GameLogger();
            while (totalGameCount < numBotGamesAtOnce) {
                User a = new User(null, -1);
                User b = new User(null, -1);
                a.setDifficulty("hard");
                b.setDifficulty("hard");
                Game game = new Game(
                        a,
                        b, gameLogger);
                games.add(game);
                totalGameCount++;
            }
        } else {

            Server server = new Server(getPort());
            ResourceHandler resourceHandler = new ResourceHandler();
            // resourceHandler.setDirectoriesListed(true);
            // resourceHandler.setWelcomeFiles(new String[] { "index.html" });
            resourceHandler.setResourceBase(".");
            // HeavyStateTest.runHeavyStateVerification();

            WebSocketHandler wsHandler = new WebSocketHandler() {
                @Override
                public void configure(WebSocketServletFactory factory) {
                    factory.register(MyWebSocketHandler.class);
                }
            };
            // Inside your main method...

            // 1. Create a ResourceHandler that looks INSIDE the JAR (Classpath)
            ResourceHandler assetsResourceHandler = new ResourceHandler();

            // Use the classloader to find the assets folder inside your JAR
            assetsResourceHandler.setBaseResource(
                    org.eclipse.jetty.util.resource.Resource.newClassPathResource("/assets"));

            // 2. Create the Context Handler for /assets
            ContextHandler assetsContextHandler = new ContextHandler("/assets");
            assetsContextHandler.setHandler(assetsResourceHandler);

            // 3. IMPORTANT: Update your MainApp.handle to ONLY serve HTML on the root path
            // This prevents it from serving HTML when a JS file is missing

            // For serving the main application (including index.html from resources)
            // Your existing MainApp handler already handles this

            // Update your handlers list
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] {
                    wsHandler,
                    assetsContextHandler,
                    new MainApp() // Your existing handler that serves index.html from resources
            });

            server.setHandler(handlers);

            // server.setHandler(new WebSocketTest());

            server.start();
            server.join();
        }

    }
}
