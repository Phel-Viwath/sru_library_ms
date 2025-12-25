package sru.edu.sru_lib_management.core.router;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import sru.edu.sru_lib_management.core.handler.DegreeLevelHandler;

@Configuration
@RequiredArgsConstructor
public class DegreeRouter {

    private final DegreeLevelHandler degreeLevelHandler;

    @Bean
    public RouterFunction<ServerResponse> degreeRoute(){
        return RouterFunctions
            .route()
            .nest(RequestPredicates.path("/api/v1/degree-level"),builder -> {
                builder.GET("", degreeLevelHandler::getAllDegreeLevel);
                builder.POST("", degreeLevelHandler::addDegreeLevel);
                builder.GET("/{id}", degreeLevelHandler::getById);
                builder.PUT("/{id}", degreeLevelHandler::updateDegreeLevel);
                builder.DELETE("/{id}", degreeLevelHandler::deleteDegreeLevel);
            })
            .build();
    }

}
