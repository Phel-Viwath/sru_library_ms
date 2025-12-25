/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.mapper;

import sru.edu.sru_lib_management.core.domain.mapper.dto.StaffDto;
import sru.edu.sru_lib_management.core.domain.model.LibraryStaff;

public class StaffMapper {
    public static StaffDto toStaffDto(LibraryStaff libraryStaff){
        if (libraryStaff == null) return null;
        return new StaffDto(
                libraryStaff.getStaffId(),
                libraryStaff.getStaffName(),
                libraryStaff.getGender(),
                libraryStaff.getPosition(),
                libraryStaff.getDegreeLevel(),
                libraryStaff.getMajorId().split(", "),
                libraryStaff.getYear(),
                libraryStaff.getShiftWork(),
                libraryStaff.getIsActive()
        );
    }
}
