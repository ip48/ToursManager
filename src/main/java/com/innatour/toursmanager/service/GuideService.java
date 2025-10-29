package com.innatour.toursmanager.service;

import com.innatour.toursmanager.model.Guide;
import com.innatour.toursmanager.repository.GuideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GuideService {
    
    private final GuideRepository guideRepository;
    
    public GuideService(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
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
    
    public List<Guide> searchGuidesByLanguage(String language) {
        return guideRepository.findByLanguagesContainingIgnoreCase(language);
    }
    
    public Guide createGuide(Guide guide) {
        // Check if email already exists
        if (guideRepository.findByEmail(guide.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + guide.getEmail());
        }
        return guideRepository.save(guide);
    }
    
    public Guide updateGuide(Long id, Guide guideDetails) {
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
        guide.setLanguages(guideDetails.getLanguages());
        guide.setActive(guideDetails.getActive());
        
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
