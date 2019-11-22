package com.github.gugumian.indigo.web.config;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndigoConfig {
    @Bean
    Indigo indigo() {
        return new Indigo();
    }

    @Bean
    IndigoInchi indigoInchi(Indigo indigo) {
        return new IndigoInchi(indigo);
    }

    @Bean
    IndigoRenderer indigoRenderer(Indigo indigo) {
        return new IndigoRenderer(indigo);
    }
}
