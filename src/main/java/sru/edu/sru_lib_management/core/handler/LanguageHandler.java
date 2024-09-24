/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.Language;
import sru.edu.sru_lib_management.core.domain.service.LanguageService;

@Controller
@RequestMapping("/api/v1/language")
@RequiredArgsConstructor
public class LanguageHandler {

    private final LanguageService languageService;

    @PostMapping
    public Mono<ServerResponse> addLanguage(ServerRequest request){
        return request.bodyToMono(Language.class).flatMap(language -> {
            if (language.getLanguageId().isBlank() || language.getLanguageName().isBlank())
                return ServerResponse.badRequest().build();
            return languageService.save(language)
                    .flatMap(result -> ServerResponse.ok().bodyValue(result))
                    .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage()));
        });
    }

    public Mono<ServerResponse> updateLanguage(ServerRequest request){
        var id = request.pathVariable("id");
        return request.bodyToMono(Language.class)
                .flatMap(language -> languageService.update(language, id))
                .flatMap(result -> ServerResponse.ok().bodyValue(result))
                .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> allLanguage(ServerRequest request){
        return ServerResponse.ok().body(languageService.findAll(), Language.class);
    }
    public Mono<ServerResponse> findLanguageById(ServerRequest request){
        return languageService.findById(request.pathVariable("id"))
                .flatMap(language -> ServerResponse.ok().bodyValue(language))
                .onErrorResume(e -> {
                    if(e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST){
                        return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue("Staff not found");
                    } else {
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(e.getMessage());
                    }
                });
    }

    public Mono<ServerResponse> deleteCollege(ServerRequest request) {
        return languageService.delete(request.pathVariable("id"))
                .flatMap(result -> result
                        ? ServerResponse.ok().bodyValue("language deleted successfully")
                        : ServerResponse.status(404).bodyValue("language not found")
                )
                .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage()));
    }
}
