/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.data.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.BlackList;
import sru.edu.sru_lib_management.core.domain.model.BlackListDto;
import sru.edu.sru_lib_management.core.domain.repository.BlackListRepository;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BlackListRepositoryImp implements BlackListRepository {

    private final DatabaseClient databaseClient;

    private static final String SAVE_QR = "Insert into blacklist(student_id, book_id) VALUES (:studentId, :bookId);";
    private static final String UPDATE_QR = "UPDATE blacklist set student_id = :studentId, book_id = :bookId WHERE blacklist_id = :blacklistId;";
    private static final String DELETE_QR = "DELETE from blacklist WHERE blacklist_id = :id;";
    private static final String FIND_ALL_QR = "SELECT * FROM blacklist;";
    private static final String FIND_BY_ID_QR = "SELECT * FROM blacklist WHERE blacklist_id = :blacklistId;";

    @Override
    public Mono<BlackList> save(BlackList data) {
        return databaseClient.sql(SAVE_QR)
                .bind("studentId", data.getStudentId())
                .bind("bookId", data.getBookId())
                .fetch().rowsUpdated()
                .thenReturn(data);
    }

    @Override
    public Mono<BlackList> update(BlackList data, Integer id) {
        return databaseClient.sql(UPDATE_QR)
                .bind("blacklistId", data.getBlacklistId())
                .bind("studentId", data.getStudentId())
                .bind("bookId", data.getBookId())
                .fetch()
                .rowsUpdated()
                .thenReturn(data);
    }

    @Override
    public Mono<Boolean> delete(Integer id) {
        return databaseClient.sql(DELETE_QR)
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .map(rows -> rows > 0);
    }

    @Override
    public Flux<BlackList> findAll() {
        return databaseClient.sql(FIND_ALL_QR)
                .map(((row, rowMetadata) -> new BlackList(
                        row.get("blacklist_id", Integer.class),
                        row.get("student_id", Long.class),
                        row.get("book_id", String.class)))
                ).all();
    }

    @Override
    public Mono<BlackList> findById(Integer id) {
        return databaseClient
                .sql(FIND_BY_ID_QR)
                .bind("blacklistId", id)
                .map(((row, rowMetadata) -> new BlackList(
                        row.get("blacklist_id", Integer.class),
                        row.get("student_id", Long.class),
                        row.get("book_id", String.class)))
                ).one();
    }

    @Override
    public Mono<List<BlackList>> findByStudentIdAndBookId(Long studentId, String bookId) {
        return databaseClient
                .sql("Select * From blacklist Where student_id = :studentId And book_id = :bookId")
                .bind("studentId", studentId)
                .bind("bookId", bookId)
                .map(((row, rowMetadata) -> new BlackList(
                        row.get("blacklist_id", Integer.class),
                        row.get("student_id", Long.class),
                        row.get("book_id", String.class)))
                ).all().collectList();
    }

    @Override
    public Flux<BlackListDto> getAllBlackListDetail() {
        return databaseClient.
                sql(QR_Detail)
                .map((row, rowMetadata) ->
                        new BlackListDto(
                               row.get("book_id", String.class),
                               row.get("book_title", String.class),
                               row.get("student_id", Long.class),
                               row.get("student_name", String.class),
                               row.get("give_back_date", LocalDate.class),
                               null
                        )
                ).all();
    }

    private static final String QR_Detail= """
            SELECT
                b.book_id,
                b.book_title,
                s.student_id,
                s.student_name,
                bb.give_back_date
            FROM
                blacklist bl
                    JOIN
                borrow_books bb ON bl.student_id = bb.student_id AND bl.book_id = bb.book_id
                    JOIN
                books b ON bb.book_id = b.book_id
                    JOIN
                students s ON bl.student_id = s.student_id
            WHERE
                bb.is_bring_back = 0;
            
            """;


}
