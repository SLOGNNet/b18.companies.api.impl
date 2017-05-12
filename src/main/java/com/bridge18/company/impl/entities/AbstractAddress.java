package com.bridge18.company.impl.entities;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractAddress extends Jsonable {
    @Value.Parameter
    Optional<Integer> getAddressId();
    @Value.Parameter
    Optional<String> getAddressName();
    @Value.Parameter
    Optional<String> getStreetAddress1();
    @Value.Parameter
    Optional<String> getStreetAddress2();
    @Value.Parameter
    Optional<String> getCity();
    @Value.Parameter
    Optional<String> getAddressPhone();
    @Value.Parameter
    Optional<String> getState();
    @Value.Parameter
    Optional<String> getZip();
    @Value.Parameter
    Optional<String> getAddressFax();
    @Value.Parameter
    Optional<String> getAddressPhoneExtension();
    @Value.Parameter
    Optional<String> getAddressFaxExtension();
    @Value.Parameter
    Optional<Double> getAddressLatitude();
    @Value.Parameter
    Optional<Double> getAddressLongitude();
}
