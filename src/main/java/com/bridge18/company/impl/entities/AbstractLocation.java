package com.bridge18.company.impl.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;
import org.pcollections.PVector;

import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractLocation {
    @Value.Parameter
    Optional<String> getName();
    @Value.Parameter
    Optional<Address> getAddress();
    @Value.Parameter
    Optional<PVector<ContactInfo>> getContactInfo();
}
