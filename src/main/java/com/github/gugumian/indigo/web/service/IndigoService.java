package com.github.gugumian.indigo.web.service;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.github.gugumian.indigo.web.Constant.*;

@Service
public class IndigoService {
    @Autowired
    Indigo indigo;
    @Autowired
    IndigoInchi indigoInchi;
    @Autowired
    IndigoRenderer indigoRenderer;

    public Mono<String> getVersion() {
        return Mono.just(indigo.version());
    }

    public Mono<IndigoObject> loadIdentifier(String identifier, String type) {
        return Mono.just(type)
                .flatMap(t -> {
                    switch (t.toLowerCase()) {
                        case INCHI:
                            return Mono.just(indigoInchi.loadMolecule(identifier));
                        case SMILES:
                            return Mono.just(indigo.loadMolecule(identifier));
                        case SMARTS:
                            return Mono.just(indigo.loadSmarts(identifier));
                        case REACTION:
                            return Mono.just(indigo.loadReaction(identifier));
                        default:
                            return Mono.empty();
                    }
                });
    }

    public Mono<String> convert(String identifier, String source, String target) {
        return loadIdentifier(identifier, source)
                .flatMap(i -> {
                    switch (target.toLowerCase()){
                        case INCHI:
                            return Mono.just(indigoInchi.getInchi(i));
                        case INCHI_KEY:
                            return  Mono.just(indigoInchi.getInchiKey(indigoInchi.getInchi(i)));
                        case SMILES:
                            return Mono.just(i.smiles());
                        case FORMULA:
                            return  REACTION.equals(source) ? Flux.fromIterable(i.iterateReactants())
                                    .map(IndigoObject::grossFormula)
                                    .map(s -> s.replaceAll("\\s", ""))
                                    .reduce((s1, s2) -> s1 + "+" + s2)
                                    .concatWith(Flux.fromIterable(i.iterateCatalysts())
                                            .map(IndigoObject::grossFormula)
                                            .map(s -> s.replaceAll("\\s", ""))
                                            .reduce((s1, s2) -> s1 + "+" + s2)
                                    )
                                    .concatWith(Flux.fromIterable(i.iterateProducts())
                                            .map(IndigoObject::grossFormula)
                                            .map(s -> s.replaceAll("\\s", ""))
                                            .reduce((s1, s2) -> s1 + "+" + s2)
                                    )
                                    .reduce((s1, s2) -> s1 + ">" + s2)
                                    : Mono.just(i.grossFormula());
                        default:
                            return  Mono.empty();
                    }
                });
    }

    public Mono<DataBuffer> renderer(String identifier, String type) {
        indigo.setOption("render-output-format", "svg");
        return loadIdentifier(identifier, type)
                .map(indigoObject -> {
                    indigoObject.layout();
                    return indigoRenderer.renderToBuffer(indigoObject);
                })
                .map(bytes -> new DefaultDataBufferFactory().wrap(bytes));
    }

    public Mono<List<List<String>>> reactionDetail(String identifier) {
        return loadIdentifier(identifier, REACTION)
                .flatMap(indigoObject -> {
                    Mono<List<String>> reactants = Flux.fromIterable(indigoObject.iterateReactants())
                            .map(IndigoObject::grossFormula).map(s -> s.replaceAll("\\s", "")).collectList();
                    Mono<List<String>> catalysts = Flux.fromIterable(indigoObject.iterateCatalysts())
                            .map(IndigoObject::grossFormula).map(s -> s.replaceAll("\\s", "")).collectList();
                    Mono<List<String>> products = Flux.fromIterable(indigoObject.iterateProducts())
                            .map(IndigoObject::grossFormula).map(s -> s.replaceAll("\\s", "")).collectList();
                    return reactants.concatWith(catalysts).concatWith(products).collectList();
                });
    }

}
