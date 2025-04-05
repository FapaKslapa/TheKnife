package src.com.example;

import com.google.gson.Gson;
import javafx.scene.web.WebEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JavaScriptBridge {
    private final WebEngine webEngine;
    private final Gson gson = new Gson();
    private final Map<String, BridgeMethod> methods = new HashMap<>();

    public JavaScriptBridge(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    public interface BridgeMethod {
        Object execute(String jsonArgs);
    }

    public void registerMethod(String methodName, BridgeMethod method) {
        methods.put(methodName, method);
        injectMethod(methodName);
    }

    private void injectMethod(String methodName) {
        String script = String.format(
                "if (!window.javaConnector) window.javaConnector = {};" +
                        "window.javaConnector.%s = function(jsonArgs) {" +
                        "   return JSON.parse(" +
                        "      eval('(' + window.javaConnectorInvoke('{\\'method\\': \\''%s\\'', \\'args\\': ' + " +
                        "           JSON.stringify(jsonArgs || {}) + '}') + ')')" +
                        "   );" +
                        "};",
                methodName, methodName
        );
        webEngine.executeScript(script);
    }

    public String invoke(String jsonPayload) {
        try {
            Map<String, Object> payload = gson.fromJson(jsonPayload, Map.class);
            String methodName = (String) payload.get("method");
            String args = gson.toJson(payload.get("args"));

            if (methods.containsKey(methodName)) {
                Object result = methods.get(methodName).execute(args);
                return gson.toJson(result);
            }
            return gson.toJson(Map.of("error", "Method not found: " + methodName));
        } catch (Exception e) {
            return gson.toJson(Map.of("error", e.getMessage()));
        }
    }

    public void injectBridge() {
        webEngine.executeScript(
                "window.javaConnectorInvoke = function(jsonPayload) {" +
                        "   return window.javaInvoke(jsonPayload);" +
                        "};"
        );
    }
}