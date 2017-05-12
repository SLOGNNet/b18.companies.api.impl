package com.bridge18.company.impl.services.objects;

import akka.Done;
import com.bridge18.company.entities.CompanyType;
import com.bridge18.company.impl.entities.*;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import org.pcollections.PVector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Singleton
public class CompanyServiceImpl implements CompanyService {

    private final PersistentEntityRegistry persistentEntityRegistry;

    @Inject
    public CompanyServiceImpl(PersistentEntityRegistry persistentEntityRegistry) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(CompanyEntity.class);
    }

    @Override
    public CompletionStage<CompanyState> createCompany(String name,
                                                       Optional<String> taxId,
                                                       Optional<String> mc,
                                                       Optional<CompanyType> companyType,
                                                       Optional<PVector<Contact>> contacts,
                                                       Optional<PVector<Location>> locations) {

        PersistentEntityRef<CompanyCommand> ref = persistentEntityRegistry
                .refFor(CompanyEntity.class, UUID.randomUUID().toString());

        CreateCompany createCompany = CreateCompany.builder()
                .name(name)
                .mc(mc)
                .taxId(taxId)
                .companyType(companyType)
                .contacts(contacts)
                .locations(locations)
                .build();

        return ref.ask(createCompany);
    }

    @Override
    public CompletionStage<CompanyState> updateCompany(String id, String name,
                                                       Optional<String> taxId,
                                                       Optional<String> mc,
                                                       Optional<CompanyType> companyType,
                                                       Optional<PVector<Contact>> contacts,
                                                       Optional<PVector<Location>> locations) {
        PersistentEntityRef<CompanyCommand> ref = persistentEntityRegistry.refFor(CompanyEntity.class, id);

        UpdateCompany updateCompany = UpdateCompany.builder()
                .name(name)
                .mc(mc)
                .taxId(taxId)
                .companyType(companyType)
                .contacts(contacts)
                .locations(locations)
                .build();

        return ref.ask(updateCompany);
    }

    @Override
    public CompletionStage<CompanyState> getCompany(String id) {
        PersistentEntityRef<CompanyCommand> ref = persistentEntityRegistry.refFor(CompanyEntity.class, id);
        GetCompanyInformation getCompanyInformation = GetCompanyInformation.builder().build();

        return ref.ask(getCompanyInformation);
    }

    @Override
    public CompletionStage<Done> deleteCompany(String id) {
        PersistentEntityRef<CompanyCommand> ref = persistentEntityRegistry.refFor(CompanyEntity.class, id);
        DeleteCompany deleteCompany = DeleteCompany.builder().build();

        return ref.ask(deleteCompany);
    }
}

