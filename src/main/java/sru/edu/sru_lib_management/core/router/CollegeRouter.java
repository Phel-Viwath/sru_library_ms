package sru.edu.sru_lib_management.core.router;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import sru.edu.sru_lib_management.core.handler.CollegeHandler;

@Configuration
@RequiredArgsConstructor
public class CollegeRouter{

    private final CollegeHandler collegeHandler;

    @Bean
    public RouterFunction<ServerResponse> collegeRoute(){
        return RouterFunctions.route()
                .nest(RequestPredicates.path("/api/v1/college"), build -> {
                    build.POST("", collegeHandler::addNewCollege);
                    build.GET("", collegeHandler::getAllCollege);
                    build.GET("/{id}", collegeHandler::getById);
                    build.PUT("/{id}", collegeHandler::updateCollege);
                    build.PUT("/{id}", collegeHandler::deleteCollege);
                })
                .build();
    }


}
