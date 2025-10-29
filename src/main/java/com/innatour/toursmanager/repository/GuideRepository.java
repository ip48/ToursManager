package com.innatour.toursmanager.repository;

import com.innatour.toursmanager.model.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {
    
    Optional<Guide> findByEmail(String email);
    
    List<Guide> findByActive(Boolean active);
    
    List<Guide> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);
    
    List<Guide> findByLanguagesContainingIgnoreCase(String language);
}
