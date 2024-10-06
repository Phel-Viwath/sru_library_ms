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
import sru.edu.sru_lib_management.core.domain.mapper.dto.StaffDto;
import sru.edu.sru_lib_management.core.domain.model.Staff;
import sru.edu.sru_lib_management.core.domain.service.StaffService;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StaffHandler {

    private final StaffService staffService;

    public Mono<ServerResponse> addNewStaff(ServerRequest request){

        return request.bodyToMono(StaffDto.class)
                .flatMap(staffDto -> {
                    if (staffDto.getStaffName()== null || staffDto.getGender() == null)
                        return ServerResponse.badRequest().build();
                    String majorId = Arrays.stream(staffDto.getMajorId())
                            .collect(Collectors.joining(", "));
                    Staff staff = new Staff(
                            staffDto.getStaffId(),
                            staffDto.getStaffName(),
                            staffDto.getGender(),
                            staffDto.getPosition(),
                            staffDto.getDegreeLevel(),
                            majorId,
                            staffDto.getYear(),
                            staffDto.getShiftWork(),
                            staffDto.getIsActive()
                    );
                    return staffService.save(staff)
                            .flatMap(result -> ServerResponse.ok().bodyValue(result))
                            .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage()));
                });
    }

    public Mono<ServerResponse> getAllStaff(ServerRequest request){
        return ServerResponse.ok().body(staffService.findAll(), StaffDto.class);
    }

    public Mono<ServerResponse> getByStaffById(ServerRequest request){
        var id = Long.parseLong(request.pathVariable("id"));
        return staffService.findById(id)
                .flatMap(result -> ServerResponse.ok().bodyValue(result))
                .onErrorResume(e -> {
                   if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST){
                       return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue("Not found!");
                   }else {
                       return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(e.getMessage());
                   }
                });
    }

    public Mono<ServerResponse> updateStaff(ServerRequest request){

        return request.bodyToMono(StaffDto.class)
                .flatMap(staffDto -> {
                    String majorId = Arrays.stream(staffDto.getMajorId())
                            .collect(Collectors.joining(", "));

                    Staff staff = new Staff(
                            staffDto.getStaffId(),
                            staffDto.getStaffName(),
                            staffDto.getGender(),
                            staffDto.getPosition(),
                            staffDto.getDegreeLevel(),
                            majorId,
                            staffDto.getYear(),
                            staffDto.getShiftWork(),
                            staffDto.getIsActive()
                    );
                    return staffService.update(staff)
                            .flatMap(s -> ServerResponse.ok().bodyValue(s))
                            .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage()));
                });
    }

    public Mono<ServerResponse> deleteStaff(ServerRequest request){
        var id = Long.parseLong(request.pathVariable("id"));
        return staffService.delete(id)
                .flatMap(result -> result
                        ? ServerResponse.accepted().bodyValue("Staff deleted successfully")
                        : ServerResponse.status(404).bodyValue("College not found")
                )
                .onErrorResume(e -> ServerResponse.status(500).bodyValue(e.getMessage()));
    }
}
