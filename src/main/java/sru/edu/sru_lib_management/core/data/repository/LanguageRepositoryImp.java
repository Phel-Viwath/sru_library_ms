/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.data.repository;

import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sru.edu.sru_lib_management.core.domain.model.Language;
import sru.edu.sru_lib_management.core.domain.repository.LanguageRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LanguageRepositoryImp implements LanguageRepository {

    private final DatabaseClient client;

    private static final String SAVE_QR = "INSERT INTO language(language_id, language_name) VALUES (:languageId, :languageName)";
    private static final String UPDATE_QR = "UPDATE language set language_id = :languageId, language_name = :languageName WHERE language_id = :id";

    @Override
    public Mono<Language> save(Language entity) {
        return client.sql(SAVE_QR)
                .bindValues(paramMap(entity))
                .then()
                .thenReturn(entity);
    }

    @Override
    public Mono<Language> update(Language entity, String id) {
        return client.sql(UPDATE_QR)
                .bind("id", id)
                .bind("languageName", entity.getLanguageName())
                .bind("languageId", entity.getLanguageId())
                .then()
                .thenReturn(entity);
    }

    @Override
    public Mono<Language> findById(String id) {
        return client.sql("SELECT * FROM language WHERE language_id = :languageId")
                .bind("languageId", id)
                .map((row, rowMetadata) -> mapToLanguage(row))
                .one();
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return client.sql("DELETE FROM language WHERE language_id = :id")
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .map(result -> result > 0);
    }

    @Override
    public Flux<Language> findAll() {
        return client.sql("SELECT * FROM language")
                .map((row, rowMetadata) -> mapToLanguage(row))
                .all();
    }


    private Map<String, Object> paramMap(Language language){
        return Map.ofEntries(
                Map.entry("languageId", language.getLanguageId()),
                Map.entry("languageName", language.getLanguageName())
        );
    }

    private Language mapToLanguage(Row row){
        return new Language(
                row.get("language_id", String.class),
                row.get("language_name", String.class)
        );
    }
}
