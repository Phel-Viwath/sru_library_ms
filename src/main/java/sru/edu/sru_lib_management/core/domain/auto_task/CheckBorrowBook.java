/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.auto_task;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.BlackList;
import sru.edu.sru_lib_management.core.domain.model.Borrow;
import sru.edu.sru_lib_management.core.domain.service.BlacklistService;
import sru.edu.sru_lib_management.core.domain.service.BorrowService;
import sru.edu.sru_lib_management.core.utils.ExtensionKt;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CheckBorrowBook {

    private final BlacklistService blacklistService;
    private final BorrowService borrowService;

    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Phnom_Penh")
    public void addToBlackList(){
        System.out.println("Check borrow!");
        Flux<Borrow> bookFlux = ExtensionKt.asFlux(borrowService.getBorrows());
        bookFlux.filter(borrow ->
                        !borrow.isBringBack() && borrow.getBorrowDate().isBefore(LocalDate.now().minusDays(14))
                )
                .flatMap(borrow ->
                        blacklistService
                                .exist(borrow.getStudentId(), borrow.getBookId())
                                .flatMap(exist -> {
                                    if (exist){
                                        return Mono.just(borrow);
                                    }else {
                                        return blacklistService.save(new BlackList(null, borrow.getStudentId(), borrow.getBookId()))
                                                .thenReturn(borrow);
                                    }
                                })
                )
                .doOnError(error -> {
                    System.err.println("Error processing blacklist: " + error.getMessage());
                })
                .doOnComplete(() -> {
                    System.out.println("Blacklist processing completed.");
                })
                .subscribe();
    }

}
