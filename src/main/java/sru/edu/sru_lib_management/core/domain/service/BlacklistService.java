/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.BlackList;
import sru.edu.sru_lib_management.core.domain.model.BlackListDto;

@Service
public interface BlacklistService {
    Mono<BlackList> save(BlackList blackList);
    Mono<BlackList> update(Integer blacklistId, BlackList blackList);
    Mono<Boolean> delete(Integer id);
    Flux<BlackList> findAll();
    Mono<BlackList> findById(Integer id);
    Mono<Boolean> exist(Long studentId, String bookId);

    Flux<BlackListDto> getBlackListDetail();
}
