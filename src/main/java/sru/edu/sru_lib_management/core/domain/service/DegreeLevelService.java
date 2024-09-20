/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service;

import org.springframework.stereotype.Service;
import sru.edu.sru_lib_management.core.domain.model.DegreeLevel;
import sru.edu.sru_lib_management.core.domain.service.crud.ICrudService;

@Service
public interface DegreeLevelService extends ICrudService<DegreeLevel, String> {
}
