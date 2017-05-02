package com.bridge18.company.impl.entities;

import com.bridge18.company.entities.CompanyType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;
import org.pcollections.PVector;

import java.util.Optional;

public interface CompanyEvent extends Jsonable, AggregateEvent<CompanyEvent> {

    int NUM_SHARDS = 4;
    AggregateEventShards<CompanyEvent> COMPANY_EVENT_SHARDS =
            AggregateEventTag.sharded(CompanyEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventTagger<CompanyEvent> aggregateTag() {
        return COMPANY_EVENT_SHARDS;
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractCompanyCreated extends CompanyEvent {
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

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractCompanyUpdated extends CompanyEvent {
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

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractCompanyDeleted extends CompanyEvent {
        @Value.Parameter
        String getId();
    }
}
