/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.utils;

import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux

fun <T> Flow<T>.asFlux(): Flux<T> {
    return mono { this@asFlux.toList() }
        .flatMapMany { Flux.fromIterable(it) }
}