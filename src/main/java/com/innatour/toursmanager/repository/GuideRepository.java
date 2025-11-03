package com.innatour.toursmanager.repository;

import com.innatour.toursmanager.model.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {
    
    Optional<Guide> findByEmail(String email);
    
    List<Guide> findByActive(Boolean active);
    
    List<Guide> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);
    
    /**
     * Find guides by language code (proper join query)
     * This is now efficient because languages are in a separate table
     */
    @Query("SELECT DISTINCT g FROM Guide g JOIN g.languages l WHERE LOWER(l.code) = LOWER(:languageCode)")
    List<Guide> findByLanguageCode(@Param("languageCode") String languageCode);
}
