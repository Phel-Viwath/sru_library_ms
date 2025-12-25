/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.College;
import sru.edu.sru_lib_management.core.domain.model.DegreeLevel;
import sru.edu.sru_lib_management.core.domain.service.DegreeLevelService;

@Controller
@RequestMapping()
@RequiredArgsConstructor
public class DegreeLevelHandler {

    private final DegreeLevelService degreeLevelService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Mono<ServerResponse> addDegreeLevel(ServerRequest request){
        return request.bodyToMono(DegreeLevel.class).flatMap(degreeLevel -> {
            if (degreeLevel.getDegreeLevelId().isBlank() || degreeLevel.getDegreeLevel().isBlank())
                return ServerResponse.badRequest().build();
            return degreeLevelService.save(degreeLevel)
                    .flatMap(result -> ServerResponse.ok().bodyValue(result))
                    .onErrorResume(e -> ServerResponse.status(500).bodyValue(e));
        });

    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public Mono<ServerResponse> getAllDegreeLevel(ServerRequest request){
        var allDegreeLevel =  degreeLevelService.findAll();
        return ServerResponse.ok().body(allDegreeLevel, College.class);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public Mono<ServerResponse> getById(ServerRequest request){
        var id = request.pathVariable("id");
        return degreeLevelService.findById(id)
                .flatMap(it -> ServerResponse.ok().bodyValue(it))
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue("Staff not found");
                    } else {
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(e.getMessage());
                    }
                });
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Mono<ServerResponse> updateDegreeLevel(ServerRequest request){
        var id = request.pathVariable("id");
        return request.bodyToMono(DegreeLevel.class)
                .flatMap(degreeLevel -> degreeLevelService.update(degreeLevel, id)
                .flatMap(result -> ServerResponse.accepted().bodyValue(result))
                .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage())));

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public Mono<ServerResponse> deleteDegreeLevel(ServerRequest request) {
        var id = request.pathVariable("id");
        return degreeLevelService.delete(id)
                .flatMap(result -> result
                        ? ServerResponse.ok().bodyValue("Degree deleted successfully")
                        :ServerResponse.status(404).bodyValue("Degree not found")
                )
                .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage()));
    }
}
