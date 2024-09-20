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
import sru.edu.sru_lib_management.core.domain.model.Language;
import sru.edu.sru_lib_management.core.domain.service.LanguageService;

@Controller
@RequestMapping("/api/v1/language")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @PostMapping
    public Mono<ResponseEntity<Object>> addLanguage(@RequestBody Language language){
        return languageService.save(language)
                .map(result -> ResponseEntity.ok().body(result))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateLanguage(@RequestBody Language language, @PathVariable String id){
        return languageService.update(language, id)
                .map(result -> ResponseEntity.accepted().body(result))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

    @GetMapping
    public Flux<Language> allLanguage(){
        return languageService.findAll();
    }
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> findLanguageById(@PathVariable String id){
        return languageService.findById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if(e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST){
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Staff not found"));
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
                    }
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteCollege(@PathVariable String id) {
        return languageService.delete(id)
                .map(result -> result
                        ? ResponseEntity.ok().body("language deleted successfully")
                        : ResponseEntity.status(404).body("language not found")
                )
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }
}
