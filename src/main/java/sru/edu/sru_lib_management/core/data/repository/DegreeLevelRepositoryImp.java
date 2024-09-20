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
import sru.edu.sru_lib_management.core.domain.model.DegreeLevel;
import sru.edu.sru_lib_management.core.domain.repository.DegreeLevelRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DegreeLevelRepositoryImp implements DegreeLevelRepository {

    private final DatabaseClient client;

    @Override
    public Mono<DegreeLevel> save(DegreeLevel entity) {
        return client.sql("INSERT INTO degree_level(degree_level_id, degree_level) VALUES (:degreeLevelId, :degreeLevel);")
                .bindValues(paramMap(entity))
                .then().thenReturn(entity);
    }

    @Override
    public Mono<DegreeLevel> update(DegreeLevel entity, String id) {
        return client.sql("UPDATE degree_level set degree_level_id = :degreeLevelId, degree_level = :degreeLevel WHERE degree_level_id = :id;")
                .bind("degreeLevelId", entity.getDegreeLevel())
                .bind("degreeLevel", entity.getDegreeLevel())
                .bind("id", id)
                .then()
                .thenReturn(entity);
    }

    @Override
    public Mono<DegreeLevel> findById(String id) {
        return client.sql("SELECT * FROM degree_level WHERE degree_level_id = :id;")
                .bind("id", id)
                .map((row, rowMetadata) -> mapToDegreeLevel(row))
                .one();
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return client.sql("DELETE FROM degree_level WHERE degree_level_id = :id;")
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .map(result -> result >0);
    }

    @Override
    public Flux<DegreeLevel> findAll() {
        return client.sql("SELECT * FROM degree_level;")
                .map((row, rowMetadata) -> mapToDegreeLevel(row))
                .all();
    }

    private DegreeLevel mapToDegreeLevel(Row row){
        return new DegreeLevel(
                row.get("degree_level_id", String.class),
                row.get("degree_level", String.class)
        );
    }
    private Map<String, Object> paramMap(DegreeLevel degreeLevel){
        return Map.ofEntries(
                Map.entry("degreeLevelId", degreeLevel.getDegreeLevelId()),
                Map.entry("degreeLevel", degreeLevel.getDegreeLevel())
        );
    }
}
