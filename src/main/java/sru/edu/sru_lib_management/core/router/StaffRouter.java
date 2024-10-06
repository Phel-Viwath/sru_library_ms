package sru.edu.sru_lib_management.core.router;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import sru.edu.sru_lib_management.core.handler.StaffHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
@RequiredArgsConstructor
public class StaffRouter {

    private final StaffHandler staffHandler;

    @Bean
    public RouterFunction<ServerResponse> staffRoute(){
        return RouterFunctions.route()
                .path("/api/v1/staff", b1 -> b1
                        .nest(accept(MediaType.APPLICATION_JSON), b2 -> b2
                            .GET(staffHandler::getAllStaff)
                            .POST(staffHandler::addNewStaff)
                            .PUT(staffHandler::updateStaff)
                            .GET("/{id}", staffHandler::getByStaffById)
                            .DELETE("/{id}", staffHandler::deleteStaff)
                        )
                ).build();
    }
}
