/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.Major;
import sru.edu.sru_lib_management.core.domain.service.MajorService;

@Controller
@RequestMapping("/api/v1/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    @GetMapping
    public Flux<Major> getAllMajor(){
        return majorService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getMajorById(@PathVariable String id){
        return majorService.findById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST){
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found"));
                    }
                    else {
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
                    }
                });
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> addNewMajor(@RequestBody Major major){
        return majorService.save(major)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateMajor(@PathVariable String id, @RequestBody Major major){
        return majorService.update(major, id)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteMajor(@PathVariable String id){
        return majorService.delete(id)
                .map(result -> result
                        ? ResponseEntity.ok().body("Major deleted successfully.")
                        : ResponseEntity.status(404).body("Major not found.")
                )
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

}
