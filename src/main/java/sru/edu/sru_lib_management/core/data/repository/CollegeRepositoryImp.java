/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.data.repository;

import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.College;
import sru.edu.sru_lib_management.core.domain.repository.CollegeRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CollegeRepositoryImp implements CollegeRepository {

    private final DatabaseClient client;

    private static final String SAVE_QR = "INSERT INTO colleges(college_id, college_name) VALUES (:collegeId, :collegeName)";
    private static final String UPDATE_QR = "Update colleges set college_id = :collegeId ,college_name = :collegeName WHERE college_id =:id;";
    private static final String DELETE_QR = "DELETE FROM colleges WHERE college_id = :id";
    private static final String FIND_ALL_QR = "SELECT * FROM colleges";
    private static final String FIND_BY_ID_QR = "SELECT * FROM colleges WHERE college_id = :id";


    @Override
    public Mono<College> save(College entity) {
        return client.sql(SAVE_QR)
                .bindValues(paramMap(entity))
                .then().thenReturn(entity);
    }

    @Override
    public Mono<College> update(College entity, String id) {
        return client.sql(UPDATE_QR)
                .bindValues(paramMapUpdate(entity, id))
                .then()
                .thenReturn(entity);
    }

    @Override
    public Mono<College> findById(String id) {
        return client.sql(FIND_BY_ID_QR)
                .bind("id", id)
                .map((row, rowMetadata) -> mapToCollege(row))
                .one();
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return client.sql(DELETE_QR)
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .map(result -> result > 0);
    }

    @Override
    public Flux<College> findAll() {
        return client.sql(FIND_ALL_QR)
                .map((row, rowMetadata) -> mapToCollege(row))
                .all();
    }

    private Map<String, Object> paramMap(College college){
        return Map.ofEntries(
                Map.entry("collegeId", college.getCollegeId()),
                Map.entry("collegeName", college.getCollegeName())
        );
    }

    private Map<String, Object> paramMapUpdate(College college, String id){
        return Map.ofEntries(
                Map.entry("collegeId", college.getCollegeId()),
                Map.entry("collegeName", college.getCollegeName()),
                Map.entry("id", id)
        );
    }


    private College mapToCollege(Row row){
        return new College(
                row.get("college_id", String.class),
                row.get("college_name", String.class)
        );
    }

}
