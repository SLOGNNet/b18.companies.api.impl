package com.bridge18.company.impl.services.objects;

import akka.Done;
import com.bridge18.company.entities.CompanyType;
import com.bridge18.company.impl.entities.CompanyState;
import com.bridge18.company.impl.entities.Contact;
import com.bridge18.company.impl.entities.Location;
import org.pcollections.PVector;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface CompanyService {
    CompletionStage<CompanyState> createCompany(String name, Optional<String> taxId, Optional<String> mc,
                                                Optional<CompanyType> companyType, Optional<PVector<Contact>> contacts,
                                                Optional<PVector<Location>> locations);

    CompletionStage<CompanyState> updateCompany(String id, String name, Optional<String> taxId, Optional<String> mc,
                                                Optional<CompanyType> companyType, Optional<PVector<Contact>> contacts,
                                                Optional<PVector<Location>> locations);

    CompletionStage<CompanyState> getCompany(String id);

    CompletionStage<Done> deleteCompany(String id);
}
