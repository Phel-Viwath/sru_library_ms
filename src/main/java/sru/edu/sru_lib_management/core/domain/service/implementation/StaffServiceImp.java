/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service.implementation;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.mapper.StaffMapper;
import sru.edu.sru_lib_management.core.domain.mapper.dto.StaffDto;
import sru.edu.sru_lib_management.core.domain.model.Staff;
import sru.edu.sru_lib_management.core.domain.model.StaffMajor;
import sru.edu.sru_lib_management.core.domain.repository.StaffMajorRepository;
import sru.edu.sru_lib_management.core.domain.repository.StaffRepository;
import sru.edu.sru_lib_management.core.domain.service.StaffService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@SuppressWarnings("CallToPrintStackTrace")
@Component
@RequiredArgsConstructor
public class StaffServiceImp implements StaffService {

    private final StaffRepository staffRepository;
    private final StaffMajorRepository staffMajorRepository;

    private final Logger logger = LoggerFactory.getLogger(StaffServiceImp.class);

    @Override
    public Mono<Object> save(Staff staff) {
        if (staff.getStaffName().isBlank() || staff.getGender().isBlank()){
            return Mono.just("Invalid Input");
        }
        String[] str = staff.getMajorId().split(", ");

        Staff staffToSave = new Staff(
                staff.getStaffId(),
                staff.getStaffName(),
                staff.getGender(),
                staff.getPosition(),
                staff.getDegreeLevel(),
                str[0],
                staff.getYear(),
                staff.getShiftWork(),
                staff.getIsActive()
        );

        return staffRepository.save(staffToSave)
                .flatMap(saved ->{
                    logger.info("{}", saved);
                    String[] majors = staff.getMajorId().split(", ");
                    List<Mono<StaffMajor>> saveStaffMajor= Arrays.stream(majors)
                            .map(majorId -> {
                                StaffMajor staffMajor = new StaffMajor(saved.getStaffId(), majorId);
                                return staffMajorRepository.save(staffMajor);
                            }).collect(Collectors.toList());
                    return Mono.when(saveStaffMajor)
                            .then(Mono.just(saved));

                })
                .map(saved -> (Object) saved)
                .onErrorResume(e ->{
                    e.printStackTrace();
                    return Mono.just(e.getMessage());
                });
    }

    @Override
    public Mono<Object> update(Staff staff) {

        String[] newMajorId = staff.getMajorId().split(", ");
        Staff staffUpdate = new Staff(
                staff.getStaffId(),
                staff.getStaffName(),
                staff.getGender(),
                staff.getPosition(),
                staff.getDegreeLevel(),
                newMajorId[0],
                staff.getYear(),
                staff.getShiftWork(),
                staff.getIsActive()
        );

        return staffRepository.findById(staff.getStaffId())
                .flatMap(exist ->
                     staffRepository.update(staffUpdate, null)
                            .flatMap(updated ->
                                    staffMajorRepository.findStaffMajorByStaffId(staff.getStaffId())
                                            .collectList()
                                            .flatMap(existingStaffMajors -> {
                                                List<String> existingMajorIds  = existingStaffMajors.stream()
                                                        .map(StaffMajor::getMajorId)
                                                        .toList();
                                                List<String> majorToDelete = existingMajorIds.stream()
                                                        .filter(majorId -> Arrays.asList(newMajorId).contains(majorId))
                                                        .collect(Collectors.toList());
                                                Mono<Void> deleteOutdatedMajors = staffMajorRepository.deleteAllByStaffIdAndMajorIds(staff.getStaffId(), majorToDelete);

                                                List<Mono<StaffMajor>> saveNewMajors = Arrays.stream(newMajorId)
                                                        .filter(majorId -> !existingMajorIds.contains(majorId))
                                                        .map(majorId -> {
                                                            StaffMajor staffMajor = new StaffMajor(staff.getStaffId(), majorId);
                                                            return staffMajorRepository.save(staffMajor);
                                                        })
                                                        .collect(Collectors.toList());
                                                return deleteOutdatedMajors.thenMany(Flux.concat(saveNewMajors)).collectList();
                                        })
                            )
                )
                .map(updatedMajors ->(Object) "Update successful")
                .switchIfEmpty(Mono.just("Not found!"))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(e.getMessage());
                });
    }

    @Override
    public Mono<Object> findById(Long staffId) {
        return staffRepository.findById(staffId)
                .map(staff -> (Object) staff)
                .switchIfEmpty(
                        Mono.error(
                            new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "College not found!"
                        ))
                )
                .onErrorResume(e -> Mono.just(e.getMessage()));
    }

    @Override
    public Mono<Boolean> delete(Long staffId) {
        return staffRepository.findById(staffId)
                .flatMap(staff ->
                        staffRepository.delete(staffId)
                                .map(deletionResult ->  true)
                )
                .switchIfEmpty(Mono.just(false))
                .onErrorResume(e -> {
                    logger.info(e.getMessage());
                    return Mono.just(false);
                });
    }

    @Override
    public Flux<StaffDto> findAll() {
        return staffRepository
                .findAll()
                .map(StaffMapper::toStaffDto)
                .onErrorMap(e ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                e.getMessage()
                        )
                );
    }
}
