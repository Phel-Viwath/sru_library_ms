/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service.implementation;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.Language;
import sru.edu.sru_lib_management.core.domain.repository.LanguageRepository;
import sru.edu.sru_lib_management.core.domain.service.LanguageService;

@SuppressWarnings("CallToPrintStackTrace")
@Component
@RequiredArgsConstructor
public class LanguageServiceImp implements LanguageService {

    private final LanguageRepository languageRepository;
    private final Logger logger = LoggerFactory.getLogger(LanguageServiceImp.class);

    @Override
    public Mono<Object> save(Language language) {
        if (language.getLanguageId().isBlank() || language.getLanguageName().isBlank())
            return Mono.just("Field can not be blank");
        return languageRepository.findById(language.getLanguageId())
                .flatMap(exist -> Mono.just((Object) "ID already exist."))
                .switchIfEmpty(languageRepository.save(language)
                        .map(saved -> (Object) saved)
                ).onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(e.getMessage());
                });
    }

    @Override
    public Mono<Object> update(Language language, String id) {
        return languageRepository.findById(id)
                .flatMap(exist ->
                        languageRepository.update(language, id)
                                .map(d -> (Object) d)
                )
                .switchIfEmpty(Mono.just("Not found!"))
                .onErrorResume(e -> Mono.just(e.getMessage()));
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return languageRepository.findById(id)
                .flatMap(exist ->
                        languageRepository.delete(id)
                                .thenReturn(true)
                )
                .defaultIfEmpty(false)
                .onErrorReturn(false);
    }

    @Override
    public Flux<Language> findAll() {
        return languageRepository
            .findAll()
            .onErrorMap(e ->
                new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
                )
            );
    }

    @Override
    public Mono<Object> findById(String id) {
        logger.info("Find by id: {}", id);
        return languageRepository.findById(id)
            .map(college -> (Object) college)
            .switchIfEmpty(Mono.empty());
    }
}
