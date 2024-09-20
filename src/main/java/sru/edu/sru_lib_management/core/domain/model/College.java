/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class College {
    private String collegeId;
    private String collegeName;

    public String getCollegeId() {
        return this.collegeId;
    }
    public String getCollegeName(){
        return this.collegeName;
    }
}
