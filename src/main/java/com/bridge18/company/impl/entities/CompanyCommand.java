package com.bridge18.company.impl.entities;


import akka.Done;
import com.bridge18.company.entities.CompanyType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;
import org.pcollections.PVector;

import java.util.Optional;

public interface CompanyCommand extends Jsonable {

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractCreateCompany extends CompanyCommand, CompressedJsonable, PersistentEntity.ReplyType<CompanyState> {
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

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractUpdateCompany extends CompanyCommand, CompressedJsonable, PersistentEntity.ReplyType<CompanyState> {
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

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractGetCompanyInformation extends CompanyCommand, CompressedJsonable, PersistentEntity
            .ReplyType<CompanyState> {
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractDeleteCompany extends CompanyCommand, CompressedJsonable, PersistentEntity
            .ReplyType<Done> {
    }

}
