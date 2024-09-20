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
import sru.edu.sru_lib_management.core.domain.model.SruStaff;
import sru.edu.sru_lib_management.core.domain.repository.SruStaffRepository;
import sru.edu.sru_lib_management.core.domain.service.SruStaffService;


@SuppressWarnings("CallToPrintStackTrace")
@Component
@RequiredArgsConstructor
public class SruStaffServiceImp implements SruStaffService {

    private final SruStaffRepository sruStaffRepository;

    @Override
    public Mono<Object> save(SruStaff entity) {
        return sruStaffRepository.findById(entity.getSruStaffId())
                .flatMap(exist -> Mono.just((Object) "ID already exist!"))
                .switchIfEmpty(
                        sruStaffRepository.save(entity)
                                .map(saved -> (Object) saved)
                )
                .onErrorResume(e -> {
                   e.printStackTrace();
                   return Mono.just(e.getMessage());
                });
    }

    @Override
    public Mono<Object> update(SruStaff entity, String id) {
        return sruStaffRepository.findById(entity.getSruStaffId())
                .flatMap(exist ->
                        sruStaffRepository.update(entity, id).map(saved -> (Object) saved)
                )
                .switchIfEmpty(Mono.just((Object) "ID already exist!"))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(e.getMessage());
                });
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return sruStaffRepository.findById(id)
                .flatMap(exist ->
                        sruStaffRepository.delete(id).thenReturn(true)
                )
                .defaultIfEmpty(false)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(false);
                });
    }

    @Override
    public Flux<SruStaff> findAll() {
        return sruStaffRepository.findAll()
                .onErrorMap(e ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                e.getMessage()
                        )
                );
    }

    @Override
    public Mono<Object> findById(String id) {
        return sruStaffRepository.findById(id)
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
