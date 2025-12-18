/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class BlackListDto {
    private String bookId;
    private String bookTitle;
    private Long studentId;
    private String studentName;
    @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate giveBackDate;
    private Double money;
}
