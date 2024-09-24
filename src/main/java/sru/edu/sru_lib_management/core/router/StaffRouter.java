package sru.edu.sru_lib_management.core.router;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import sru.edu.sru_lib_management.core.handler.StaffHandler;

@Configuration
@RequiredArgsConstructor
public class StaffRouter {

    private final StaffHandler staffHandler;

    @Bean
    public RouterFunction<ServerResponse> staffRoute(){
        return RouterFunctions.route()
                .nest(RequestPredicates.path("/api/v1/staff"), builder -> {
                    builder.GET("", staffHandler::getAllStaff);
                    builder.POST("", staffHandler::addNewStaff);
                    builder.POST("", staffHandler::updateStaff);
                    builder.GET("/{id}", staffHandler::getByStaffById);
                    builder.DELETE("/{id}", staffHandler::deleteStaff);
                }).build();
    }
}
