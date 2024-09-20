/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.mapper;

import sru.edu.sru_lib_management.core.domain.mapper.dto.StaffDto;
import sru.edu.sru_lib_management.core.domain.model.Staff;

public class StaffMapper {
    public static StaffDto toStaffDto(Staff staff){
        if (staff == null) return null;
        return new StaffDto(
                staff.getStaffId(),
                staff.getStaffName(),
                staff.getGender(),
                staff.getPosition(),
                staff.getDegreeLevel(),
                staff.getMajorId().split(", "),
                staff.getYear(),
                staff.getShiftWork(),
                staff.getIsActive()
        );
    }
}
