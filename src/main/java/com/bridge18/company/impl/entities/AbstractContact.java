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
public interface AbstractContact extends Jsonable {
    @Value.Parameter
    Optional<String> getId();
    @Value.Parameter
    Optional<String> getFirstName();
    @Value.Parameter
    Optional<String> getMiddleName();
    @Value.Parameter
    Optional<String> getLastName();
    @Value.Parameter
    Optional<PVector<ContactInfo>> getContactInfo();
    @Value.Parameter
    Optional<String> getPosition();
    @Value.Parameter
    Optional<Address> getAddress();
}
