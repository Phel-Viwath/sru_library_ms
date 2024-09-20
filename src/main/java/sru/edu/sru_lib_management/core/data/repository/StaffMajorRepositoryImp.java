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
import sru.edu.sru_lib_management.core.domain.model.StaffMajor;
import sru.edu.sru_lib_management.core.domain.repository.StaffMajorRepository;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StaffMajorRepositoryImp implements StaffMajorRepository {

    private final DatabaseClient client;

    private static final String SAVE_QR = """
        INSERT INTO staff_major(staff_id, major_id) VALUES(:staffId, :majorId) ;
    """;

    @Override
    public Mono<StaffMajor> save(StaffMajor entity) {
        return client.sql(SAVE_QR)
                .bindValues(mapStaffMajor(entity))
                .then()
                .thenReturn(entity);
    }

    @Override
    public Mono<StaffMajor> update(StaffMajor entity, Long id) {
        return null;
    }

    @Override
    public Mono<StaffMajor> findById(Long id) {
        return null;
    }

    @Override
    public Mono<Boolean> delete(Long id) {
        return client.sql("DELETE FROM staff_major WHERE staff_id = :id")
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .map(result -> result > 0);
    }

    @Override
    public Flux<StaffMajor> findAll() {
        return null;
    }

    private Map<String, Object> mapStaffMajor(StaffMajor staffMajor){
        return Map.ofEntries(
                Map.entry("staffId", staffMajor.getStaffId()),
                Map.entry("majorId", staffMajor.getMajorId())
        );
    }

    private StaffMajor mapToStaffMajor(Row row){
        return new  StaffMajor(
                row.get("staff_id", Long.class),
                row.get("major_id", String.class)
        );
    }

    @Override
    public Flux<StaffMajor> findStaffMajorByStaffId(Long id) {
        return client.sql("SELECT * FROM staff_major WHERE staff_id = :id")
                .bind("id", id)
                .map((row, rowMetadata) -> mapToStaffMajor(row))
                .all();
    }

    @Override
    public Mono<Void> deleteAllByStaffIdAndMajorIds(Long staffId, List<String> majorIds) {
        String deleteQuery = """
            DELETE FROM staff_major 
            WHERE staff_id = :staffId 
            AND major_id NOT IN (:majorIds)
        """;

        return client.sql(deleteQuery)
                .bind("staffId", staffId)
                .bind("majorIds", majorIds)
                .fetch()
                .rowsUpdated()
                .then();
    }
}