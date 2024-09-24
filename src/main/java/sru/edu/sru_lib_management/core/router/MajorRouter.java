package sru.edu.sru_lib_management.core.router;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import sru.edu.sru_lib_management.core.handler.MajorHandler;

@Configuration
@RequiredArgsConstructor
public class MajorRouter {

    private final MajorHandler majorHandler;

    @Bean
    public RouterFunction<ServerResponse> majorRoute(){
        return RouterFunctions.route()
                .nest(RequestPredicates.path("/api/v1/major"), builder -> {
                    builder.GET("", majorHandler::getAllMajor);
                    builder.POST("", majorHandler::addNewMajor);
                    builder.GET("/{id}", majorHandler::getMajorById);
                    builder.PUT("/{id}", majorHandler::updateMajor);
                    builder.DELETE("/{id}", majorHandler::deleteMajor);
                })
                .build();
    }

}
