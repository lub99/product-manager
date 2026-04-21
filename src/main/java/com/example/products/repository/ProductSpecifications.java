package com.example.products.repository;

import com.example.products.domain.Product;
import com.example.products.dto.request.ProductFilter;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@UtilityClass
public final class ProductSpecifications {

    public static Specification<Product> matches(ProductFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.priceEurFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priceEur"), filter.priceEurFrom()));
            }
            if (filter.priceEurTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("priceEur"), filter.priceEurTo()));
            }
            if (filter.priceUsdFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priceUsd"), filter.priceUsdFrom()));
            }
            if (filter.priceUsdTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("priceUsd"), filter.priceUsdTo()));
            }

            if (StringUtils.hasText(filter.code())) {
                String pattern = escapeLikePattern(filter.code().trim()) + "%";
                predicates.add(cb.like(cb.lower(root.get("code")),
                        pattern.toLowerCase(Locale.ROOT), '\\'));
            }
            if (StringUtils.hasText(filter.name())) {
                String pattern = escapeLikePattern(filter.name().trim()) + "%";
                predicates.add(cb.like(cb.lower(root.get("name")),
                        pattern.toLowerCase(Locale.ROOT), '\\'));
            }
            if (filter.isAvailable() != null) {
                predicates.add(cb.equal(root.get("isAvailable"), filter.isAvailable()));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Escapes {@code %}, {@code _} and {@code \} for use in LIKE with escape char {@code \}.
     */
    static String escapeLikePattern(String raw) {
        return raw.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
