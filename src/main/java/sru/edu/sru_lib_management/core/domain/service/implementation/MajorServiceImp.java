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
import sru.edu.sru_lib_management.core.domain.model.Major;
import sru.edu.sru_lib_management.core.domain.repository.MajorRepository;
import sru.edu.sru_lib_management.core.domain.service.CollegeService;
import sru.edu.sru_lib_management.core.domain.service.MajorService;

@SuppressWarnings("CallToPrintStackTrace")
@Component
@RequiredArgsConstructor
public class MajorServiceImp implements MajorService {

    private final MajorRepository majorRepository;
    private final CollegeService collegeService;

    @Override
    public Mono<Object> save(Major entity) {
        // Validate required fields
        if (entity.getMajorId() == null || entity.getMajorId().isBlank() ||
                entity.getMajorName() == null || entity.getMajorName().isBlank() ||
                entity.getCollegeId() == null || entity.getCollegeId().isBlank()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "majorId, majorName, and collegeId are required"
            ));
        }

        return collegeService.findById(entity.getCollegeId())
            .switchIfEmpty(
                Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "College not found!"))
            )
            .then(
                majorRepository.findById(entity.getMajorId())
                    .flatMap(exist -> Mono.just((Object) "ID already in use."))
                    .switchIfEmpty(
                            majorRepository.save(entity).map(result -> (Object) result)
                    ).onErrorResume(e -> {
                        e.printStackTrace();
                        return Mono.just(e.getMessage());
                    })
            );

    }

    @Override
    public Mono<Object> update(Major entity, String id) {
        return majorRepository.findById(id)
                .flatMap(exist -> majorRepository.update(entity, id).map(result -> (Object) result))
                .switchIfEmpty(
                        Mono.just((Object) "Not found!")
                ).onErrorResume(e -> Mono.just(e.getMessage()));
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return majorRepository.findById(id)
                .flatMap(exist -> majorRepository.delete(id).thenReturn(true))
                .defaultIfEmpty(false)
                .onErrorReturn(false);
    }

    @Override
    public Flux<Major> findAll() {
        return majorRepository.findAll()
                .onErrorMap(e ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                e.getMessage()
                        )
                );
    }

    @Override
    public Mono<Object> findById(String id) {
        return majorRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Not found"
                        )
                ))
                .map(result -> (Object) result)
                .onErrorResume(e -> Mono.just(e.getMessage()));
    }
}
