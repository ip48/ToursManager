package com.innatour.toursmanager.repository;

import com.innatour.toursmanager.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    
    /**
     * Find a language by its ISO 639-1 code (e.g., "en", "es")
     */
    Optional<Language> findByCode(String code);
    
    /**
     * Find multiple languages by their codes
     */
    Set<Language> findByCodeIn(Set<String> codes);
    
    /**
     * Check if a language code exists
     */
    boolean existsByCode(String code);
}
