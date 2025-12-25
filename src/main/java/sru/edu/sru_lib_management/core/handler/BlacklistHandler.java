/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.BlackList;
import sru.edu.sru_lib_management.core.domain.model.BlackListDto;
import sru.edu.sru_lib_management.core.domain.service.BlacklistService;

@Component
@RequiredArgsConstructor
public class BlacklistHandler {

    private final BlacklistService blacklistService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public Mono<ServerResponse> allInBlackListDetail(ServerRequest request) {
        var data = blacklistService.getBlackListDetail();
        return ServerResponse.ok().body(data, BlackListDto.class);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Mono<ServerResponse> updateBlackList(
            ServerRequest request
    ){
        int blacklistId = Integer.parseInt(request.pathVariable("blacklistId"));
        Mono<BlackList> blacklistMono = request.bodyToMono(BlackList.class);

        return blacklistMono.flatMap(blackList -> {
            boolean areFieldBlank = blackList.getBlacklistId() == null || blackList.getStudentId() == null || blackList.getBookId() == null;
            boolean id = blacklistId == 0;
            if (areFieldBlank || id){
                return ServerResponse.badRequest().build();
            }

            return blacklistService.update(blacklistId, blackList)
                    .flatMap(updateBlackList ->
                            ServerResponse.ok().bodyValue(updateBlackList)
                    )
                    .switchIfEmpty(ServerResponse.badRequest().build());
        });
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public Mono<ServerResponse> getById(ServerRequest request){
        int blacklistId = Integer.parseInt(request.pathVariable("blacklistId"));
        return blacklistService.findById(blacklistId)
                .flatMap(it ->
                        ServerResponse.ok().bodyValue(it)
                )
                .switchIfEmpty(ServerResponse.badRequest().build());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public Mono<ServerResponse> delete(
            ServerRequest request
    ){
        var blacklistId = Integer.parseInt(request.pathVariable("blacklistId"));
        return blacklistService.delete(blacklistId)
                .flatMap(monoVoid ->
                        ServerResponse.ok().build()
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Mono<ServerResponse> addToBlackList(ServerRequest request){
        return request.bodyToMono(BlackList.class)
                .flatMap(blacklist ->{
                    boolean areFieldBlank = blacklist.getStudentId() == null || blacklist.getBookId() == null;
                    if (areFieldBlank){
                        return ServerResponse.badRequest().build();
                    }
                    return blacklistService.exist(blacklist.getStudentId(), blacklist.getBookId())
                            .flatMap(exist -> {
                                if (exist) {
                                    return ServerResponse.badRequest().build();
                                } else {
                                    BlackList bl = new BlackList(null, blacklist.getStudentId(), blacklist.getBookId());
                                    return blacklistService.save(bl)
                                            .flatMap(it ->
                                                    ServerResponse.ok().bodyValue(it)
                                            );
                                }
                            });
                });
    }

    public Mono<ServerResponse> search(ServerRequest request){
        String keyword = request.queryParam("keyword").orElse(null);
        Flux<BlackList> blackLists = blacklistService.search(keyword);
        return ServerResponse.ok().bodyValue(blackLists);
    }


}
