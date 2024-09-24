/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.Major;
import sru.edu.sru_lib_management.core.domain.service.MajorService;

@Component
@RequiredArgsConstructor
public class MajorHandler {

    private final MajorService majorService;

    public Mono<ServerResponse> getAllMajor(ServerRequest request){
        return ServerResponse.ok().body(majorService.findAll(), Major.class);
    }

    public Mono<ServerResponse> getMajorById(ServerRequest request){
        return majorService.findById(request.pathVariable("id"))
                .flatMap(major -> ServerResponse.ok().bodyValue(this))
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST){
                        return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue("Not found");
                    }
                    else {
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(e.getMessage());
                    }
                });
    }

    public Mono<ServerResponse> addNewMajor(ServerRequest request){
        return request.bodyToMono(Major.class).flatMap(major -> {
            if (major.getMajorName().isBlank() || major.getCollegeId().isBlank() || major.getMajorId().isBlank())
                return ServerResponse.badRequest().build();
            return majorService.save(major)
                    .flatMap(result -> ServerResponse.ok().bodyValue(result))
                    .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage()));
        });
    }

    public Mono<ServerResponse> updateMajor(ServerRequest request){
        var id = request.pathVariable("id");
        return request.bodyToMono(Major.class)
                .flatMap(major -> majorService.update(major, id))
                .flatMap(result -> ServerResponse.ok().bodyValue(result))
                .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> deleteMajor(ServerRequest request){
        return majorService.delete(request.pathVariable("id"))
                .flatMap(result -> result
                        ? ServerResponse.ok().bodyValue("Major deleted successfully.")
                        : ServerResponse.status(404).bodyValue("Major not found.")
                )
                .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage()));
    }

}
