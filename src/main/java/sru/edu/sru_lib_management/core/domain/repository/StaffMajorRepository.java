/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.StaffMajor;
import sru.edu.sru_lib_management.core.domain.repository.crud.RCrudRepository;

import java.util.List;

@Repository
public interface StaffMajorRepository extends RCrudRepository<StaffMajor, Long> {
    Flux<StaffMajor> findStaffMajorByStaffId(Long id);
    Mono<Void> deleteAllByStaffIdAndMajorIds(Long staffId, List<String> majorIds);
}
