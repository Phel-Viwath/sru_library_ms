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
import sru.edu.sru_lib_management.core.domain.model.College;
import sru.edu.sru_lib_management.core.domain.service.CollegeService;

import java.util.Objects;

@RequiredArgsConstructor
@Component
public class CollegeHandler {

    private final CollegeService collegeService;

    public Mono<ServerResponse> addNewCollege(ServerRequest request){
        return request.bodyToMono(College.class).flatMap(college -> {
            if (college.getCollegeId() == null || college.getCollegeName() == null)
                return ServerResponse.badRequest().build();
            return collegeService.save(college)
                    .flatMap(result ->
                            ServerResponse.ok().bodyValue(result)
                    )
                    .onErrorResume(this::handleError);
        });

    }

    public Mono<ServerResponse> getAllCollege(ServerRequest request){
        return ServerResponse.ok().body(collegeService.findAll(), College.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request){
        var id = request.pathVariable("id");
        return collegeService.findById(id)
                .flatMap(result ->
                        ServerResponse.ok().bodyValue(result)
                )
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> updateCollege(ServerRequest request){
        var id = request.pathVariable("id");
        return request.bodyToMono(College.class)
                .flatMap(college -> collegeService.update(college, id)
                .flatMap(result ->
                        ServerResponse.ok().bodyValue(result)
                )
                .onErrorResume(this::handleError));
    }

    public Mono<ServerResponse> deleteCollege(ServerRequest request) {
        var id = request.pathVariable("id");
        return collegeService.delete(id)
                .flatMap(result -> result
                        ? ServerResponse.ok().bodyValue("College deleted successfully")
                        : ServerResponse.status(404).bodyValue("College not found")
                )
                .onErrorResume(this::handleError);
    }

    private Mono<ServerResponse> handleError(Throwable e) {
        if (e instanceof ResponseStatusException rse) {
            return ServerResponse.status(rse.getStatusCode()).bodyValue(Objects.requireNonNull(rse.getReason()));
        } else {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(e.getMessage());
        }
    }
}
