package com.github.gugumian.indigo.web.config;

import com.github.gugumian.indigo.web.handler.IndigoHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class WebRounter {
    @Autowired
    IndigoHandler indigoHandler;

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return nest(
                path("/v1/indigo"),
                route(
                        GET("/info"),
                        indigoHandler::getVersion
                ).andRoute(
                        POST("/render"),
                        indigoHandler::render
                ).andNest(
                        path("/convert"),
                        route(
                                method(HttpMethod.GET),
                                indigoHandler::convert
                        ).andRoute(
                                method(HttpMethod.POST),
                                indigoHandler::convert
                        )
                )
        );
    }
}
