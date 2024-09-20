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
import sru.edu.sru_lib_management.core.domain.model.College;
import sru.edu.sru_lib_management.core.domain.repository.CollegeRepository;
import sru.edu.sru_lib_management.core.domain.service.CollegeService;

@Component
@RequiredArgsConstructor
public class CollegeServiceImp implements CollegeService {

    private final CollegeRepository collegeRepository;

    private static final HttpStatus badRequest = HttpStatus.BAD_REQUEST;
    private static final HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;
    private static final HttpStatus notFound = HttpStatus.NOT_FOUND;

    @Override
    public Mono<Object> save(College college) {
        if (college.getCollegeId().isBlank() || college.getCollegeName().isBlank())
            return Mono.error(new ResponseStatusException(badRequest, "Field cannot be blank."));

        return collegeRepository.findById(college.getCollegeId())
                .flatMap(exist ->
                        Mono.error(
                                new ResponseStatusException(badRequest, "College ID already exist.")
                        )
                )
                .switchIfEmpty( collegeRepository.save(college)
                        .map(saved -> (Object) saved)
                )
                .onErrorResume(e ->
                        Mono.error(new ResponseStatusException(internalServerError, e.getMessage()))
                );
    }

    @Override
    public Mono<Object> update(College college, String id) {
        return collegeRepository.findById(college.getCollegeId())
                .flatMap(exist ->
                        collegeRepository.update(college, id)
                                .map(c -> (Object) c)
                )
                .switchIfEmpty(Mono.error(new ResponseStatusException(notFound, "Not found!")))
                .onErrorResume(e -> Mono.error(
                        new ResponseStatusException(internalServerError, e.getMessage())
                ));
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return collegeRepository.findById(id)
                .flatMap(exist ->
                        collegeRepository.delete(id)
                                .thenReturn(true)
                )
                .defaultIfEmpty(false)
                .onErrorReturn(false);
    }

    @Override
    public Flux<College> findAll() {
        return collegeRepository
                .findAll()
                .onErrorMap(e ->
                    new ResponseStatusException(
                            internalServerError,
                            e.getMessage()
                    )
                );
    }

    @Override
    public Mono<Object> findById(String id) {
        return collegeRepository.findById(id)
                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        notFound,
                                        "College not found!"
                                )
                        )
                )
                .map(college -> (Object) college)
                .onErrorResume(e -> Mono.error(new ResponseStatusException(internalServerError, e.getMessage())));
    }
}
