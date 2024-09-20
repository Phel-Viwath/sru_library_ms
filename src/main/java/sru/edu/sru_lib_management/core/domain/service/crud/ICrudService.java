/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service.crud;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICrudService<T, ID> {
    Mono<Object> save(T entity);
    Mono<Object> update(T entity, ID id);
    Mono<Boolean> delete(ID id);
    Flux<T> findAll();
    Mono<Object> findById(ID id);
}
