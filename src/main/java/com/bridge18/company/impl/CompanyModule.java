package com.bridge18.company.impl;

import com.bridge18.company.impl.services.lagom.LagomCompanyServiceImpl;
import com.bridge18.company.impl.services.objects.CompanyService;
import com.bridge18.company.impl.services.objects.CompanyServiceImpl;
import com.bridge18.company.v1.api.LagomCompanyService;
import com.bridge18.serializer.LagomJacksonExceptionSerializer;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.jackson.JacksonExceptionSerializer;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import play.Configuration;
import play.Environment;

public class CompanyModule extends AbstractModule implements ServiceGuiceSupport {
    private final Environment environment;
    private final Configuration configuration;

    public CompanyModule(Environment environment, Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {

        bind(CompanyService.class).to(CompanyServiceImpl.class);

        bindServices(
                serviceBinding(LagomCompanyService.class, LagomCompanyServiceImpl.class));

        bind(JacksonExceptionSerializer.class).to(LagomJacksonExceptionSerializer.class);
    }
}
