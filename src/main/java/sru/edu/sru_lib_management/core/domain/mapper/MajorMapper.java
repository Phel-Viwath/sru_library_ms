/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.mapper;

import sru.edu.sru_lib_management.core.domain.mapper.dto.MajorDto;
import sru.edu.sru_lib_management.core.domain.model.Major;

public class MajorMapper {

    public static MajorDto toMajorDto(Major major){
        if (major == null) return null;
        return new MajorDto(
                major.getMajorId(),
                major.getMajorName()
        );
    }
}
