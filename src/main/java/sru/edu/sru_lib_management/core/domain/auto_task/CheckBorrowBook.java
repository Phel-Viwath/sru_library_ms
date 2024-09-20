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
import sru.edu.sru_lib_management.core.domain.model.BorrowBook;
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
        Flux<BorrowBook> bookFlux = ExtensionKt.asFlux(borrowService.getBorrows());
        bookFlux.filter(borrowBook ->
                        !borrowBook.isBringBack() && borrowBook.getBorrowDate().isBefore(LocalDate.now().minusDays(14))
                )
                .flatMap(borrowBook ->
                        blacklistService
                                .exist(borrowBook.getStudentId(), borrowBook.getBookId())
                                .flatMap(exist -> {
                                    if (exist){
                                        return Mono.just(borrowBook);
                                    }else {
                                        return blacklistService.save(new BlackList(null, borrowBook.getStudentId(), borrowBook.getBookId()))
                                                .thenReturn(borrowBook);
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
