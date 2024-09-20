/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository.crud;

import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface RCrudRepository <T, ID>{
    Mono<T> save(T entity);
    Mono<T> update(T entity, ID id);
    Mono<T> findById(ID id);
    Mono<Boolean> delete(ID id);
    Flux<T> findAll();
}
