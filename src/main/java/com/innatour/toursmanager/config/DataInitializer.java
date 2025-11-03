package com.innatour.toursmanager.config;

import com.innatour.toursmanager.model.Language;
import com.innatour.toursmanager.repository.LanguageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes the languages table with ISO 639-1 standard language codes
 * Runs once on application startup
 */
@Configuration
public class DataInitializer {
    
    @Bean
    CommandLineRunner initLanguages(LanguageRepository languageRepository) {
        return args -> {
            // Only initialize if the table is empty
            if (languageRepository.count() == 0) {
                // ISO 639-1 language codes - matching frontend/src/constants/languages.ts
                languageRepository.save(new Language("en", "English"));
                languageRepository.save(new Language("es", "Spanish"));
                languageRepository.save(new Language("fr", "French"));
                languageRepository.save(new Language("de", "German"));
                languageRepository.save(new Language("it", "Italian"));
                languageRepository.save(new Language("pt", "Portuguese"));
                languageRepository.save(new Language("zh", "Chinese"));
                languageRepository.save(new Language("ja", "Japanese"));
                languageRepository.save(new Language("ko", "Korean"));
                languageRepository.save(new Language("ar", "Arabic"));
                languageRepository.save(new Language("he", "Hebrew"));
                languageRepository.save(new Language("ru", "Russian"));
                languageRepository.save(new Language("hi", "Hindi"));
                languageRepository.save(new Language("tr", "Turkish"));
                languageRepository.save(new Language("nl", "Dutch"));
                languageRepository.save(new Language("pl", "Polish"));
                languageRepository.save(new Language("sv", "Swedish"));
                languageRepository.save(new Language("no", "Norwegian"));
                languageRepository.save(new Language("da", "Danish"));
                languageRepository.save(new Language("fi", "Finnish"));
                languageRepository.save(new Language("cs", "Czech"));
                languageRepository.save(new Language("el", "Greek"));
                languageRepository.save(new Language("th", "Thai"));
                languageRepository.save(new Language("vi", "Vietnamese"));
                languageRepository.save(new Language("id", "Indonesian"));
                
                System.out.println("✓ Initialized " + languageRepository.count() + " languages");
            }
        };
    }
}
