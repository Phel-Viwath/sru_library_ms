/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository;

import org.springframework.stereotype.Repository;
import sru.edu.sru_lib_management.core.domain.model.DegreeLevel;
import sru.edu.sru_lib_management.core.domain.repository.crud.RCrudRepository;

@Repository
public interface DegreeLevelRepository extends RCrudRepository<DegreeLevel, String> {
}
