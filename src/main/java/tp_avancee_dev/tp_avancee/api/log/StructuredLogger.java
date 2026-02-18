package tp_avancee_dev.tp_avancee.api.log;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class StructuredLogger {

    private static final Logger LOGGER = Logger.getLogger("tp_avancee_api");

    private StructuredLogger() {
    }

    public static void info(String event, Map<String, Object> data) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ts", Instant.now().toString());
        payload.put("level", "INFO");
        payload.put("event", event);
        payload.putAll(data);

        LOGGER.info(toJson(payload));
    }

    private static String toJson(Map<String, Object> map) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append('"').append(':');
            Object value = entry.getValue();
            if (value == null) {
                builder.append("null");
            } else if (value instanceof Number || value instanceof Boolean) {
                builder.append(value);
            } else {
                builder.append('"').append(escape(String.valueOf(value))).append('"');
            }
        }
        builder.append('}');
        return builder.toString();
    }

    private static String escape(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
