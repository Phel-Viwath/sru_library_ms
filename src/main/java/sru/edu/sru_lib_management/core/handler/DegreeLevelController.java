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
import sru.edu.sru_lib_management.core.domain.model.DegreeLevel;
import sru.edu.sru_lib_management.core.domain.service.DegreeLevelService;

@Controller
@RequestMapping("/api/v1/degree-level")
@RequiredArgsConstructor
public class DegreeLevelController {

    private final DegreeLevelService degreeLevelService;

    @PostMapping
    public Mono<ResponseEntity<Object>> addNewCollege(@RequestBody DegreeLevel degreeLevel){
        return degreeLevelService.save(degreeLevel)
                .map(result -> ResponseEntity.accepted().body(result))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(e)));
    }

    @GetMapping
    public Flux<DegreeLevel> getAllCollege(){
        return degreeLevelService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getById(@PathVariable String id){
        return degreeLevelService.findById(id)
                .map(it -> ResponseEntity.ok().body(it))
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Staff not found"));
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
                    }
                });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateCollege(@RequestBody DegreeLevel degreeLevel, @PathVariable String id){
        return degreeLevelService.update(degreeLevel, id)
                .map(result -> ResponseEntity.accepted().body(result))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(e)));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteCollege(@PathVariable String id) {
        return degreeLevelService.delete(id)
                .map(result -> result
                        ? ResponseEntity.ok().body("Degree deleted successfully")
                        : ResponseEntity.status(404).body("Degree not found")
                )
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }
}
