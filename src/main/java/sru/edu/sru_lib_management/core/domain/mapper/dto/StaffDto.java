/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.mapper.dto;

import lombok.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StaffDto {
    private Long staffId;
    private String staffName;
    private String gender;
    private String position;
    private String degreeLevel;
    private String[] majorId;
    private Integer year;
    private String shiftWork;
    private Boolean isActive;

    public boolean _getIsActive(){
        return isActive;
    }
}
