/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.model;

import lombok.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Language {
    private String languageId;
    private String languageName;

    public String _getLanguageId() {
        return languageId;
    }

    public String _getLanguageName() {
        return languageName;
    }
}