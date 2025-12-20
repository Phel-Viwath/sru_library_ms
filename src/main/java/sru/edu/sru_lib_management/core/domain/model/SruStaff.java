/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.model;

import lombok.*;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SruStaff {
    private String sruStaffId;
    private String sruStaffName;
    private String gender;
    private String position;

    public String _getSruStaffId() {
        return sruStaffId;
    }
}
