package com.github.gugumian.indigo.web.handler;

import com.github.gugumian.indigo.web.Constant;
import com.github.gugumian.indigo.web.service.IndigoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class IndigoHandler {
    @Autowired
    IndigoService service;

    public Mono<ServerResponse> getVersion(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body(BodyInserters.fromPublisher(service.getVersion(), String.class));
    }
    public Mono<ServerResponse> render(ServerRequest request) {
        String type = request.queryParam("type").orElse("");
        if (type.isEmpty()) {
            return ServerResponse.badRequest().build();
        }
        return request.bodyToMono(String.class)
                .flatMap(identifier -> ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body(BodyInserters.fromDataBuffers(service.renderer(identifier, type))));
    }
    public Mono<ServerResponse> convert(ServerRequest request) {
        String source = request.queryParam("source").orElse("");
        String target = request.queryParam("target").orElse("");
        if (source.isEmpty() || target.isEmpty()) {
            return ServerResponse.badRequest().build();
        }
        if (Constant.REACTION.equals(source) && Constant.FORMULA.equals(target)) {
            return request.bodyToMono(String.class)
                    .flatMap(identifier -> service.reactionDetail(identifier))
                    .flatMap(list -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(list)));
        }
        return request.bodyToMono(String.class)
                .flatMap(identifier -> service.convert(identifier, source, target))
                .flatMap(i -> ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body(BodyInserters.fromValue(i)));
    }
}
