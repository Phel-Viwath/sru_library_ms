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
import sru.edu.sru_lib_management.core.domain.model.SruStaff;
import sru.edu.sru_lib_management.core.domain.repository.SruStaffRepository;

import java.util.Map;


@Component
@RequiredArgsConstructor
public class SruStaffRepositoryImp implements SruStaffRepository {

    private final DatabaseClient client;

    private static final String SAVE_QR = """
        INSERT INTO sru_staff(sru_staff_id, sru_staff_name, gender, position)
        VALUES (:sruStaffid, :sruStaffName, :gender, :position);
    """;

    private static final String UPDATE_QR = """
        update sru_staff set sru_staff_name = :sruStaffname,
        gender = :gender, position = :position WHERE sru_staff_id = :id;
    """;

    private static final String DELETE_QR = "delete from sru_staff where sru_staff_id = :id;";
    private static final String GET_BY_ID = "select * from sru_staff where sru_staff_id =:id;";
    private static final String GET_ALL = "select * from sru_staff;";


    @Override
    public Mono<SruStaff> save(SruStaff sruStaff) {
        return client.sql(SAVE_QR)
                .bindValues(paramMap(sruStaff))
                .then()
                .thenReturn(sruStaff);
    }

    @Override
    public Mono<SruStaff> update(SruStaff entity, String id) {
        return  client.sql(UPDATE_QR)
                .bindValues(paramMapUpdate(entity, id))
                .then()
                .thenReturn(entity);
    }

    @Override
    public Mono<SruStaff> findById(String id) {
        return client.sql(GET_BY_ID)
                .bind("id", id)
                .map((row, rowMetadata) -> mapToSruStaff(row))
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
    public Flux<SruStaff> findAll() {
        return client.sql(GET_ALL)
                .map((row, rowMetadata) -> mapToSruStaff(row))
                .all();
    }

    private Map<String, Object> paramMap(SruStaff sruStaff){
        return Map.ofEntries(
                Map.entry("sruStaffId", sruStaff.getSruStaffId()),
                Map.entry("sruStaffName", sruStaff.getSruStaffId()),
                Map.entry("gender", sruStaff.getSruStaffId()),
                Map.entry("position", sruStaff.getSruStaffId())
        );
    }
    private Map<String, Object> paramMapUpdate(SruStaff sruStaff, String id){
        return Map.ofEntries(
                Map.entry("sruStaffId", sruStaff.getSruStaffId()),
                Map.entry("sruStaffName", sruStaff.getSruStaffId()),
                Map.entry("gender", sruStaff.getSruStaffId()),
                Map.entry("position", sruStaff.getSruStaffId()),
                Map.entry("id", id)
        );
    }


    private SruStaff mapToSruStaff(Row row){
        return new SruStaff(
                row.get("sru_staff_id", String.class),
                row.get("sru_staff_name", String.class),
                row.get("gender", String.class),
                row.get("position", String.class)
        );
    }


}
