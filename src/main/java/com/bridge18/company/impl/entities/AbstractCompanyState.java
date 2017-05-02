package com.bridge18.company.impl.entities;

import com.bridge18.company.entities.CompanyType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;
import org.pcollections.PVector;

import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractCompanyState {
    @Value.Parameter
    String getId();

    @Value.Parameter
    String getName();
    @Value.Parameter
    Optional<String> getMc();
    @Value.Parameter
    Optional<String> getTaxId();
    @Value.Parameter
    Optional<CompanyType> getCompanyType();
    @Value.Parameter
    Optional<PVector<Contact>> getContacts();
    @Value.Parameter
    Optional<PVector<Location>> getLocations();
}
