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
import sru.edu.sru_lib_management.core.domain.model.Major;
import sru.edu.sru_lib_management.core.domain.repository.MajorRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MajorRepositoryImp implements MajorRepository {

    private final DatabaseClient client;
    private static final String SAVE_QUERY = """
        INSERT INTO majors(major_id, major_name, college_id)
        VALUES (:majorId, :majorName, :collegeId);
    """;
    private static final String UPDATE_QUERY = """
        UPDATE majors set major_id = :majorId, major_name = :majorName, college_id = :collegeId WHERE major_id = :id;
     """;
    private static final String FIND_ALL = "SELECT * FROM majors;";
    private static final String FIND_BY_ID = "SELECT * FROM majors WHERE major_id = :id;";
    private static final String DELETE_QUERY = "DELETE FROM majors WHERE major_id = :id;";



    @Override
    public Mono<Major> save(Major entity) {
        return client.sql(SAVE_QUERY)
                .bindValues(majorParamMap(entity))
                .then()
                .thenReturn(entity);
    }

    @Override
    public Mono<Major> update(Major entity, String id) {
        return client.sql(UPDATE_QUERY)
                .bindValues(majorParamMapUpdate(entity, id))
                .then()
                .thenReturn(entity);
    }


    @Override
    public Mono<Major> findById(String id) {
        return client.sql(FIND_BY_ID)
                .bind("id", id)
                .map(((row, rowMetadata) -> mapToMajor(row)))
                .one();
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return client.sql(DELETE_QUERY)
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .map(result -> result > 0);
    }

    @Override
    public Flux<Major> findAll() {
        return client.sql(FIND_ALL)
                .map(((row, rowMetadata) -> mapToMajor(row)))
                .all();
    }


    private Map<String, Object> majorParamMap(Major major){
        return Map.ofEntries(
                Map.entry("majorId", major.getMajorId()),
                Map.entry("majorName", major.getMajorName()),
                Map.entry("collegeId", major.getCollegeId())
        );
    }

    private Map<String, Object> majorParamMapUpdate(Major major, String id){
        return Map.ofEntries(
                Map.entry("majorId", major.getMajorId()),
                Map.entry("majorName", major.getMajorName()),
                Map.entry("collegeId", major.getCollegeId()),
                Map.entry("id", id)
        );
    }

    private Major mapToMajor(Row row){
        return new Major(
            row.get("major_id", String.class),
            row.get("major_name", String.class),
            row.get("college_id", String.class)
        );
    }

}
