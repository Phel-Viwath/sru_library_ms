/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.BlackList;
import sru.edu.sru_lib_management.core.domain.model.BlackListDto;
import sru.edu.sru_lib_management.core.domain.repository.crud.RCrudRepository;

import java.util.List;

@Repository
public interface BlackListRepository extends RCrudRepository<BlackList, Integer> {
    Mono<List<BlackList>> findByStudentIdAndBookId(Long studentId, String bookId);
    Flux<BlackListDto> getAllBlackListDetail();
}
