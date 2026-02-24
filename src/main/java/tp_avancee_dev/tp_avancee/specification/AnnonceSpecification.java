package tp_avancee_dev.tp_avancee.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.model.Status;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class AnnonceSpecification {

    private AnnonceSpecification() {}

    public static Specification<Annonce> hasStatus(Status status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Annonce> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Annonce> hasAuthorId(Long authorId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), authorId);
    }

    public static Specification<Annonce> dateAfter(Instant fromDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), fromDate);
    }

    public static Specification<Annonce> dateBefore(Instant toDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), toDate);
    }

    /**
     * Recherche LIKE dynamique via introspection : détecte automatiquement
     * tous les champs String de l'entité Annonce et construit un OR de LIKE.
     */
    public static Specification<Annonce> searchOnStringFields(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            List<String> stringFieldNames = getStringFieldNames(Annonce.class);

            List<Predicate> predicates = new ArrayList<>();
            for (String fieldName : stringFieldNames) {
                predicates.add(cb.like(cb.lower(root.get(fieldName)), pattern));
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Annonce> fetchRelations() {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("author", JoinType.LEFT);
                root.fetch("category", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    /**
     * Introspection : détecte dynamiquement les champs de type String
     * déclarés dans l'entité pour construire la recherche LIKE.
     */
    public static List<String> getStringFieldNames(Class<?> entityClass) {
        List<String> names = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                names.add(field.getName());
            }
        }
        return names;
    }

    /**
     * Retourne la liste des champs filtrables/recherchables de l'entité,
     * utilisé par l'endpoint /api/meta/annonces.
     */
    public static List<String> getFilterableFields() {
        List<String> fields = new ArrayList<>();
        fields.add("status");
        fields.add("categoryId");
        fields.add("authorId");
        fields.add("fromDate");
        fields.add("toDate");
        fields.addAll(getStringFieldNames(Annonce.class));
        return fields;
    }

    /**
     * Validation par introspection : vérifie que le champ de tri demandé
     * existe dans l'entité Annonce (sécurité contre l'injection).
     */
    public static Set<String> getAllowedSortFields() {
        return Arrays.stream(Annonce.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }

    public static void validateSortFields(String... sortFields) {
        Set<String> allowed = getAllowedSortFields();
        for (String field : sortFields) {
            if (!allowed.contains(field)) {
                throw new IllegalArgumentException(
                        "Champ de tri non autorisé : '" + field + "'. Champs autorisés : " + allowed);
            }
        }
    }
}
