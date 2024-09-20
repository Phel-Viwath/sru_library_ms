/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.BlackList;
import sru.edu.sru_lib_management.core.domain.model.BlackListDto;
import sru.edu.sru_lib_management.core.domain.service.BlacklistService;

@Controller
@RequestMapping("/api/v1/blacklist")
@RequiredArgsConstructor
public class BlacklistController{

    private final BlacklistService blacklistService;

    @GetMapping
    public Flux<BlackListDto> allInBlackListDetail(){
        return blacklistService.getBlackListDetail();
    }

    @PutMapping("/{blacklistId}")
    public Mono<ResponseEntity<BlackList>> updateBlackList(
            @PathVariable Integer blacklistId,
            @RequestBody BlackList blacklist
    ){
        boolean areFieldBlank = blacklist.getBlacklistId() == null || blacklist.getStudentId() == null || blacklist.getBookId() == null;
        boolean id = blacklistId == 0;
        if (areFieldBlank || id){
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return blacklistService.update(blacklistId, blacklist)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{blacklistId}")
    public Mono<ResponseEntity<BlackList>> getById(@PathVariable Integer blacklistId){
        return blacklistService.findById(blacklistId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{blacklistId}")
    public Mono<ResponseEntity<Void>> delete(
            @PathVariable Integer blacklistId
    ){
        return blacklistService.delete(blacklistId)
                .map(monoVoid -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<BlackList>> addToBlackList(@RequestBody BlackList blacklist){
        boolean areFieldBlank = blacklist.getStudentId() == null || blacklist.getBookId() == null;
        if (areFieldBlank){
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return blacklistService.exist(blacklist.getStudentId(), blacklist.getBookId())
                .flatMap(exist -> {
                    if (exist) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    } else {
                        BlackList bl = new BlackList(null, blacklist.getStudentId(), blacklist.getBookId());
                        return blacklistService.save(bl)
                                .map(ResponseEntity::ok);
                    }
                });
    }


}
