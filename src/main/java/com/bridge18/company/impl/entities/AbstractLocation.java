package com.bridge18.company.impl.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;
import org.pcollections.PVector;

import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractLocation extends Jsonable {
    @Value.Parameter
    Optional<String> getName();
    @Value.Parameter
    Optional<Address> getAddress();
    @Value.Parameter
    Optional<PVector<ContactInfo>> getContactInfo();
}
