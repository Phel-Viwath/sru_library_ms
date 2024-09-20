/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sru.edu.sru_lib_management.core.domain.model.BlackList;
import sru.edu.sru_lib_management.core.domain.model.BlackListDto;
import sru.edu.sru_lib_management.core.domain.repository.BlackListRepository;
import sru.edu.sru_lib_management.core.domain.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.common.APIException;
import sru.edu.sru_lib_management.utils.IndochinaDateTime;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class BlacklistServiceImp implements BlacklistService {

    private final BlackListRepository blackListRepository;
    private static final Logger logger = LoggerFactory.getLogger(BlacklistServiceImp.class);

    @Override
    public Mono<BlackList> save(BlackList blackList) {
        return blackListRepository.save(blackList);
    }

    @Override
    public Mono<BlackList> update(Integer blacklistId, BlackList blackList) {
        return blackListRepository.findById(blacklistId)
                .flatMap(existingBl ->{
                    existingBl.setStudentId(blackList.getStudentId());
                    existingBl.setBookId(blackList.getBookId());
                    return blackListRepository.update(existingBl, null);
                })
                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(HttpStatus.BAD_REQUEST, "BlackList entity not found with id: " + blackList.getBlacklistId())
                        )
                )
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException){
                        return Mono.error(e);
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the BlackList entity: " + e.getMessage()));
                });
    }

    @Override
    public Mono<Boolean> delete(Integer id) {
        return blackListRepository.findById(id)
                .flatMap(existingBlacklist -> blackListRepository.delete(id))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "BlackList entity not found with id: " + id)))
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException){
                        return Mono.error(e);
                    }
                    return Mono.error(new APIException("An error occurred while deleting the BlackList entity: " + e.getMessage()));
                });
    }

    @Override
    public Flux<BlackList> findAll() {
        return blackListRepository.findAll()
                .onErrorResume(throwable -> {
                   throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<BlackList> findById(Integer id) {
        return blackListRepository.findById(id)
                .onErrorResume(e -> {
                   throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Override
    public Mono<Boolean> exist(Long studentId, String bookId){
        return blackListRepository.findByStudentIdAndBookId(studentId, bookId)
                .map(list -> !list.isEmpty());
    }

    @Override
    public Flux<BlackListDto> getBlackListDetail() {
        return blackListRepository.getAllBlackListDetail()
                .map(blackListDto -> {
                    LocalDate currenDate = IndochinaDateTime.INSTANCE.indoChinaDate();
                    logger.info(currenDate.toString());
                    LocalDate giveBackDate = blackListDto.getGiveBackDate();
                    long overDay = ChronoUnit.DAYS.between(giveBackDate, currenDate);
                    logger.info("Over day {}", overDay);
                    double money = overDay > 0 ? overDay * 500 : 0; // if over day > 0 we multiply over day with 500 else return 0
                    blackListDto.setMoney(money);
                    return blackListDto;
                });
    }

}
