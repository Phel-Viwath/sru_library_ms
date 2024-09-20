/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Staff {
    private Long staffId;
    private String staffName;
    private String gender;
    private String position;
    private String degreeLevel;
    private String majorId;
    private Integer year;
    private String shiftWork;
    private Boolean isActive;
}
