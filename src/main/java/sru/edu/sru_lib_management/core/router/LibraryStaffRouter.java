package sru.edu.sru_lib_management.core.router;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import sru.edu.sru_lib_management.core.handler.LibraryStaffHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
@RequiredArgsConstructor
public class LibraryStaffRouter {

    private final LibraryStaffHandler libraryStaffHandler;

    @Bean
    public RouterFunction<ServerResponse> staffRoute(){
        return RouterFunctions.route()
                .path("/api/v1/library-staff", b1 -> b1
                        .nest(accept(MediaType.APPLICATION_JSON), b2 -> b2
                            .GET("", libraryStaffHandler::getAllStaff)
                            .POST("", libraryStaffHandler::addNewStaff)
                            .PUT("", libraryStaffHandler::updateStaff)
                            .GET("/{id}", libraryStaffHandler::getByStaffById)
                            .DELETE("/{id}", libraryStaffHandler::deleteStaff)
                        )
                ).build();
    }
}
