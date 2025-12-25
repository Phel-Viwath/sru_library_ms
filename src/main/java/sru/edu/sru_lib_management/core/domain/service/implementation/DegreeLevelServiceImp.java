/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.DegreeLevel;
import sru.edu.sru_lib_management.core.domain.repository.DegreeLevelRepository;
import sru.edu.sru_lib_management.core.domain.service.DegreeLevelService;

@SuppressWarnings("CallToPrintStackTrace")
@Component
@RequiredArgsConstructor
public class DegreeLevelServiceImp implements DegreeLevelService {

    private final DegreeLevelRepository degreeLevelRepository;

    @Override
    public Mono<Object> save(DegreeLevel degreeLevel) {
        if (degreeLevel.getDegreeLevelId().isBlank() || degreeLevel.getDegreeLevel().isBlank())
            return Mono.just("Field can not be blank");
        return degreeLevelRepository.findById(degreeLevel.getDegreeLevelId())
                .flatMap(exist -> Mono.just((Object) "ID already exist."))
                .switchIfEmpty(degreeLevelRepository.save(degreeLevel)
                                .map(saved -> (Object) saved)
                ).onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(e.getMessage());
                });
    }

    @Override
    public Mono<Object> update(DegreeLevel degreeLevel, String id) {
        return degreeLevelRepository.findById(id)
                .flatMap(exist ->
                        degreeLevelRepository.update(degreeLevel, id)
                                .map(d -> (Object) d)
                )
                .switchIfEmpty(Mono.just("Not found!"))
                .onErrorResume(e -> Mono.just(e.getMessage()));
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return degreeLevelRepository.findById(id)
                .flatMap(exist ->
                        degreeLevelRepository.delete(id)
                                .thenReturn(true)
                )
                .defaultIfEmpty(false)
                .onErrorReturn(false);
    }

    @Override
    public Flux<DegreeLevel> findAll() {
        return degreeLevelRepository
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
        return degreeLevelRepository.findById(id)
                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "College not found!"
                                )
                        )
                )
                .map(college -> (Object) college)
                .onErrorResume(e -> Mono.just(e.getMessage()));
    }
}
