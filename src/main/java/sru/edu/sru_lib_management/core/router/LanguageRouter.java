package sru.edu.sru_lib_management.core.router;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import sru.edu.sru_lib_management.core.handler.LanguageHandler;

@Configuration
@RequiredArgsConstructor
public class LanguageRouter {
    private final LanguageHandler languageHandler;

    @Bean
    public RouterFunction<ServerResponse> languageRoute(){
        return RouterFunctions.route()
                .nest(RequestPredicates.path("/api/v1/language"), builder -> {
                    builder.GET("", languageHandler::allLanguage);
                    builder.POST("", languageHandler::addLanguage);
                    builder.GET("/{id}", languageHandler::findLanguageById);
                    builder.PUT("/{id}", languageHandler::updateLanguage);
                    builder.DELETE("/{id}", languageHandler::deleteCollege);
                })
                .build();
    }

}
