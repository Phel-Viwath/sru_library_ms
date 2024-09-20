/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service;

import org.springframework.stereotype.Service;
import sru.edu.sru_lib_management.core.domain.model.Major;
import sru.edu.sru_lib_management.core.domain.service.crud.ICrudService;

@Service
public interface MajorService extends ICrudService<Major, String> {
}
