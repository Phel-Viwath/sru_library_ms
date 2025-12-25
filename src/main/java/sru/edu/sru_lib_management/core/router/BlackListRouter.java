package sru.edu.sru_lib_management.core.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import sru.edu.sru_lib_management.core.handler.BlacklistHandler;

@Configuration
public class BlackListRouter {

    @Bean
    public RouterFunction<ServerResponse> blackListRoute(BlacklistHandler blacklistHandler){
        return RouterFunctions.route()
                .nest(RequestPredicates.path("/api/v1/blacklist"), builder -> {
                    builder.GET("", blacklistHandler::allInBlackListDetail);
                    builder.POST("", blacklistHandler::addToBlackList);
                    builder.PUT("/{blacklistId}", blacklistHandler::updateBlackList);
                    builder.GET("/{blacklistId}", blacklistHandler::getById);
                    builder.DELETE("/{blacklistId}", blacklistHandler::delete);
                    builder.GET("/search", blacklistHandler::search);
                })
                .build();
    }

}
