package com.bridge18.company.impl.entities;

import com.bridge18.company.entities.ContactInfoType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractContactInfo extends Jsonable {
    @Value.Parameter
    Optional<String> getLabel();
    @Value.Parameter
    Optional<String> getValue();
    @Value.Parameter
    Optional<ContactInfoType> getType();
}
