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
import sru.edu.sru_lib_management.core.domain.model.LibraryStaff;
import sru.edu.sru_lib_management.core.domain.repository.LibraryStaffRepository;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LibraryStaffRepositoryImp implements LibraryStaffRepository {

    private final DatabaseClient client;

    @Override
    public Mono<LibraryStaff> save(LibraryStaff entity) {
        DatabaseClient.GenericExecuteSpec statement = client.sql(SAVE_STAFF)
                .filter(s -> s.returnGeneratedValues("staff_id"));
        statement = bindParam(statement, paramStaffSave(entity));
        return statement.fetch().first()
                .map(result -> {
                    entity.setStaffId((Long) result.get("staff_id"));
                    return entity;
                });
    }

    @Override
    public Mono<LibraryStaff> update(LibraryStaff entity, Long id) {
        DatabaseClient.GenericExecuteSpec statement = client.sql(UPDATE_STAFF);
        statement = bindParam(statement, paramStaffUpdate(entity));
        return statement.then().thenReturn(entity);
    }

    @Override
    public Mono<LibraryStaff> findById(Long id) {
        return client.sql(GET_STAFF)
                .bind("staffId", id)
                .map((row, rowMetadata) -> mapToStaff(row))
                .one();

    }

    @Override
    public Mono<Boolean> delete(Long id) {
        return client.sql(DELETE_STAFF)
                .bind("staffId", id)
                .fetch()
                .rowsUpdated()
                .map(rowUpdate -> rowUpdate > 0);
    }

    @Override
    public Flux<LibraryStaff> findAll() {
        return client.sql(GET_STAFFS)
                .map((row, rowMetadata) -> mapToStaff(row))
                .all();
    }


    private DatabaseClient.GenericExecuteSpec bindParam(DatabaseClient.GenericExecuteSpec statement, Map<String, Object> param){
        for (Map.Entry<String, Object> entry: param.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value != null){
                statement = statement.bind(key, value);
            }else {
                Class<?> valueType = getValueKey(key);
                statement = statement.bindNull(key, valueType);
            }
        }
        return statement;
    }

    private Class<?> getValueKey(String key){
        return switch (key){
            //case "staff_name", "gender", "position", "degree_level_id", "shift_work" -> String.class;
            case "staff_id" -> Long.class;
            case "year" -> Integer.class;
            case "is_active" -> Boolean.class;
            default -> String.class;
        };
    }

    private Map<String, Object> paramStaffUpdate(LibraryStaff libraryStaff){
        Map<String, Object> param = new HashMap<>();
        param.put("staffId", libraryStaff.getStaffId());
        param.put("staffName", libraryStaff.getStaffName());
        param.put("gender", libraryStaff.getGender());
        param.put("position", libraryStaff.getPosition());
        param.put("degreeLevel", libraryStaff.getDegreeLevel());
        param.put("year", libraryStaff.getYear());
        param.put("shiftWork", libraryStaff.getShiftWork());
        param.put("majorId", libraryStaff.getMajorId());
        param.put("isActive", libraryStaff.getIsActive());
        return param;
    }

    private Map<String, Object> paramStaffSave(LibraryStaff libraryStaff){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("staffName", libraryStaff.getStaffName());
        paramMap.put("gender", libraryStaff.getGender());
        paramMap.put("position", libraryStaff.getPosition());
        paramMap.put("degreeLevel", libraryStaff.getDegreeLevel());
        paramMap.put("year", libraryStaff.getYear());
        paramMap.put("shiftWork", libraryStaff.getShiftWork());
        paramMap.put("majorId", libraryStaff.getMajorId());
        paramMap.put("isActive", libraryStaff.getIsActive());
        return paramMap;
    }

    private LibraryStaff mapToStaff(Row row){
        return new LibraryStaff(
                row.get("staff_id", Long.class),
                row.get("staff_name", String.class),
                row.get("gender", String.class),
                row.get("position", String.class),
                row.get("degree_level_id", String.class),
                row.get("major_id", String.class),
                row.get("year", Integer.class),
                row.get("shift_work", String.class),
                row.get("is_active", Boolean.class)
        );
    }

    private static final String SAVE_STAFF  = """
        INSERT into library_staff(staff_name, gender, position, degree_level_id, major_id, year, shift_work, is_active)
        VALUES (:staffName, :gender, :position, :degreeLevel, :majorId, :year, :shiftWork, :isActive);
    """;
    private static final String UPDATE_STAFF = """
        UPDATE library_staff SET staff_name = :staffName, gender = :gender, position = :position
                   , degree_level_id = :degreeLevel, major_id = :majorId, year = :year
                   , shift_work = :shiftWork, is_active = :isActive
        WHERE staff_id = :staffId;
    """;
    private static final String GET_STAFF = "SELECT * FROM library_staff WHERE staff_id = :staffId;";
    private static final String DELETE_STAFF = "DELETE FROM library_staff WHERE staff_id = :staffId;";

    private static final String GET_STAFFS = """
        SELECT
        s.staff_id as staff_id,
        s.staff_name as staff_name,
        s.gender as gender,
        s.position as position,
        s.degree_level_id as degree_level_id,
        GROUP_CONCAT(dl.major_name SEPARATOR ', ') AS major_id,
        s.year as year,
        s.shift_work as shift_work,
        s.is_active as is_active
        FROM
            library_staff s JOIN
            staff_major sd ON s.staff_id = sd.staff_id JOIN
            majors dl ON sd.major_id = dl.major_id
        GROUP BY
            s.staff_id, s.staff_name, s.gender, s.position;
    """;



}
