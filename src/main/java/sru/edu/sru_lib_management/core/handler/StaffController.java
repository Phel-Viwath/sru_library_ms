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
import sru.edu.sru_lib_management.core.domain.mapper.dto.StaffDto;
import sru.edu.sru_lib_management.core.domain.model.Staff;
import sru.edu.sru_lib_management.core.domain.service.StaffService;

import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    public Mono<ResponseEntity<Object>> addNewStaff(@RequestBody StaffDto staffDto){

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
                .map(result -> ResponseEntity.ok().body(result))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e)));
    }

    @GetMapping
    public Flux<StaffDto> getAllStaff(){
        return staffService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getByStaffById(@PathVariable Long id){
        return staffService.findById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                   if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST){
                       return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not found!"));
                   }else {
                       return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
                   }
                });
    }

    @PutMapping
    public Mono<ResponseEntity<Object>> updateStaff(@RequestBody StaffDto staffDto){
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
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(e.getMessage())));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> deleteStaff(@PathVariable Long id){
        return staffService.delete(id)
                .map(result -> result
                        ? ResponseEntity.accepted().body("Staff deleted successfully")
                        : ResponseEntity.status(404).body("College not found")
                )
                .onErrorResume(e ->
                        Mono.just((ResponseEntity.status(500).body(e.getMessage()))
                ));
    }
}
