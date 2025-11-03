package com.innatour.toursmanager.service;

import com.innatour.toursmanager.model.Guide;
import com.innatour.toursmanager.model.Language;
import com.innatour.toursmanager.repository.GuideRepository;
import com.innatour.toursmanager.repository.LanguageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GuideService {
    
    private final GuideRepository guideRepository;
    private final LanguageRepository languageRepository;
    
    public GuideService(GuideRepository guideRepository, LanguageRepository languageRepository) {
        this.guideRepository = guideRepository;
        this.languageRepository = languageRepository;
    }
    
    /**
     * Convert comma-separated language codes to Set of Language entities
     */
    private Set<Language> convertLanguageCodesToEntities(String languageCodes) {
        if (languageCodes == null || languageCodes.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> codes = Arrays.stream(languageCodes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        
        Set<Language> languages = languageRepository.findByCodeIn(codes);
        
        // Validate that all requested language codes exist
        if (languages.size() != codes.size()) {
            Set<String> foundCodes = languages.stream()
                    .map(Language::getCode)
                    .collect(Collectors.toSet());
            Set<String> missingCodes = codes.stream()
                    .filter(code -> !foundCodes.contains(code))
                    .collect(Collectors.toSet());
            throw new IllegalArgumentException("Invalid language codes: " + missingCodes);
        }
        
        return languages;
    }
    
    public List<Guide> getAllGuides() {
        return guideRepository.findAll();
    }
    
    public List<Guide> getActiveGuides() {
        return guideRepository.findByActive(true);
    }
    
    public Optional<Guide> getGuideById(Long id) {
        return guideRepository.findById(id);
    }
    
    public Optional<Guide> getGuideByEmail(String email) {
        return guideRepository.findByEmail(email);
    }
    
    public List<Guide> searchGuides(String searchTerm) {
        return guideRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm);
    }
    
    public List<Guide> searchGuidesByLanguage(String languageCode) {
        return guideRepository.findByLanguageCode(languageCode);
    }
    
    /**
     * Create guide with language codes (comma-separated string)
     */
    public Guide createGuide(Guide guide, String languageCodes) {
        // Check if email already exists
        if (guideRepository.findByEmail(guide.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + guide.getEmail());
        }
        
        // Convert language codes to entities
        Set<Language> languages = convertLanguageCodesToEntities(languageCodes);
        guide.setLanguages(languages);
        
        return guideRepository.save(guide);
    }
    
    /**
     * Update guide with language codes (comma-separated string)
     */
    public Guide updateGuide(Long id, Guide guideDetails, String languageCodes) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with id: " + id));
        
        // Check if email is being changed and if new email already exists
        if (!guide.getEmail().equals(guideDetails.getEmail())) {
            if (guideRepository.findByEmail(guideDetails.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + guideDetails.getEmail());
            }
        }
        
        guide.setFirstName(guideDetails.getFirstName());
        guide.setLastName(guideDetails.getLastName());
        guide.setEmail(guideDetails.getEmail());
        guide.setPhoneNumber(guideDetails.getPhoneNumber());
        guide.setProfile(guideDetails.getProfile());
        guide.setActive(guideDetails.getActive());
        
        // Convert and set languages
        Set<Language> languages = convertLanguageCodesToEntities(languageCodes);
        guide.setLanguages(languages);
        
        return guideRepository.save(guide);
    }
    
    public void deleteGuide(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with id: " + id));
        guideRepository.delete(guide);
    }
    
    public void deactivateGuide(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with id: " + id));
        guide.setActive(false);
        guideRepository.save(guide);
    }
    
    public void activateGuide(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with id: " + id));
        guide.setActive(true);
        guideRepository.save(guide);
    }
}
