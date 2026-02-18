package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class TestDataLoader {

    private TestDataLoader() {
    }

    public static void loadDefaultDataset(EntityManager em) {
        runSqlScript(em, "/sql/test-dataset.sql");
    }

    private static void runSqlScript(EntityManager em, String path) {
        InputStream input = TestDataLoader.class.getResourceAsStream(path);
        if (input == null) {
            throw new IllegalStateException("Script SQL introuvable: " + path);
        }

        String sql;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de lire le script SQL: " + path, e);
        }

        em.getTransaction().begin();
        for (String statement : sql.split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                em.createNativeQuery(trimmed).executeUpdate();
            }
        }
        em.getTransaction().commit();
        em.clear();
    }
}
