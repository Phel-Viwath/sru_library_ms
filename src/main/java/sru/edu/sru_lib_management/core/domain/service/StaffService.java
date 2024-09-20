/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.mapper.dto.StaffDto;
import sru.edu.sru_lib_management.core.domain.model.Staff;

@Service
public interface StaffService {
    Mono<Object> save(Staff staff);
    Mono<Object> update(Staff staff);
    Mono<Object> findById(Long staffId);
    Mono<Boolean> delete(Long staffId);
    Flux<StaffDto> findAll();
}
