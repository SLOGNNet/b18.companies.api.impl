package com.bridge18.company.impl;

import com.bridge18.company.entities.CompanyType;
import com.bridge18.company.entities.ContactInfoType;
import com.bridge18.company.impl.entities.*;
import com.bridge18.company.impl.services.objects.CompanyService;
import com.bridge18.company.impl.services.objects.CompanyServiceImpl;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CompanyServiceTest {


    private PersistentEntityRegistry persistentEntityRegistry;
    private CompanyService companyService;
    private Address address;
    private PVector<ContactInfo> contactInfos;
    private PVector<Location> locations;
    private PVector<Contact> contacts;

    @Before
    public void before() {
        persistentEntityRegistry = Mockito.mock(PersistentEntityRegistry.class);
        Mockito.doNothing().doThrow(Throwable.class).when(persistentEntityRegistry).register(CompanyEntity.class);
        companyService = new CompanyServiceImpl(persistentEntityRegistry);

        address = Address.builder()
                .addressName("AddressName")
                .streetAddress1("address1")
                .streetAddress2("address2")
                .addressFax("111111")
                .addressFaxExtension("122")
                .addressLatitude(48.2180675)
                .addressLongitude(48.2180675)
                .addressPhone("930123456")
                .addressPhoneExtension("+380")
                .state("NY")
                .zip("78000")
                .city("NY")
                .build();

        contactInfos = TreePVector.from(
                Arrays.asList(
                        ContactInfo.builder().label("label-1").value("value-1").type(ContactInfoType.NONE).build(),
                        ContactInfo.builder().label("label-2").value("value-2").type(ContactInfoType.NONE).build(),
                        ContactInfo.builder().label("label-3").value("value-3").type(ContactInfoType.NONE).build()
                )
        );

        locations = TreePVector.from(
                Arrays.asList(
                        Location.builder().name("Location1").address(address).contactInfo(contactInfos).build(),
                        Location.builder().name("Location2").address(address).contactInfo(contactInfos).build()
                )
        );

        contacts = TreePVector.from(
                Arrays.asList(
                        Contact.builder()
                                .id("1")
                                .position("position-1")
                                .firstName("firstName-1")
                                .middleName("middleName-1")
                                .lastName("lastName-1")
                                .contactInfo(contactInfos)
                                .address(address)
                                .build(),
                        Contact.builder()
                                .id("2")
                                .position("position-2")
                                .firstName("firstName-2")
                                .middleName("middleName-2")
                                .lastName("lastName-2")
                                .contactInfo(contactInfos)
                                .address(address)
                                .build()
                )
        );
    }

    @Test
    public void testCompany() throws InterruptedException, ExecutionException, TimeoutException {
        PersistentEntityRef ref = Mockito.mock(PersistentEntityRef.class);
        Mockito.when(persistentEntityRegistry.refFor(Mockito.any(), Mockito.any())).thenReturn(ref);

        Mockito.when(ref.ask(Mockito.any(CreateCompany.class))).thenReturn(CompletableFuture.completedFuture(
                CompanyState.builder()
                        .id("1")
                        .name("company")
                        .mc("MC1")
                        .taxId("1111")
                        .companyType(CompanyType.BROKER)
                        .contacts(contacts)
                        .locations(locations)
                        .build()
        ));

        CompanyState companyState = companyService.createCompany(
                "company",
                Optional.of("1111"),
                Optional.of("MC1"),
                Optional.of(CompanyType.BROKER),
                Optional.of(contacts),
                Optional.of(locations)
        ).toCompletableFuture().get(5, SECONDS);

        assertNotNull(companyState.getId());
        assertEquals("company", companyState.getName());
        assertEquals(Optional.of("1111"), companyState.getTaxId());
        assertEquals(Optional.of("MC1"), companyState.getMc());
        assertEquals(Optional.of(CompanyType.BROKER), companyState.getCompanyType());
        assertEquals(Optional.of(contacts), companyState.getContacts());
        assertEquals(Optional.of(locations), companyState.getLocations());



        Mockito.when(ref.ask(Mockito.any(UpdateCompany.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        CompanyState.builder()
                                .id("1")
                                .name("company-2")
                                .mc("MC-2")
                                .taxId("2222")
                                .companyType(CompanyType.CARRIER)
                                .contacts(contacts)
                                .locations(locations)
                                .build()
                ));

        CompanyState companyUpdateState = companyService.updateCompany(
                companyState.getId(),
                "company-2",
                Optional.of("2222"),
                Optional.of("MC-2"),
                Optional.of(CompanyType.CARRIER),
                Optional.of(contacts),
                Optional.of(locations)
        ).toCompletableFuture().get(5, SECONDS);

        assertNotNull(companyState.getId());
        assertEquals("company-2", companyUpdateState.getName());
        assertEquals(Optional.of("2222"), companyUpdateState.getTaxId());
        assertEquals(Optional.of("MC-2"), companyUpdateState.getMc());
        assertEquals(Optional.of(CompanyType.CARRIER), companyUpdateState.getCompanyType());
        assertEquals(Optional.of(contacts), companyUpdateState.getContacts());
        assertEquals(Optional.of(locations), companyUpdateState.getLocations());
    }

    @Test
    public void testWithNull() throws InterruptedException, ExecutionException, TimeoutException {
        PersistentEntityRef ref = Mockito.mock(PersistentEntityRef.class);
        Mockito.when(persistentEntityRegistry.refFor(Mockito.any(), Mockito.any())).thenReturn(ref);

        Mockito.when(ref.ask(Mockito.any(UpdateCompany.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        CompanyState.builder()
                                .id("1")
                                .name("company-2")
                                .mc(Optional.empty())
                                .taxId(Optional.empty())
                                .companyType(Optional.empty())
                                .contacts(contacts)
                                .locations(locations)
                                .build()
                ));

        CompanyState companyUpdateState = companyService.updateCompany(
                "1",
                "company-2",
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(contacts),
                Optional.of(locations)
        ).toCompletableFuture().get(5, SECONDS);

        assertEquals("company-2", companyUpdateState.getName());
        assertEquals(Optional.empty(), companyUpdateState.getTaxId());
        assertEquals(Optional.empty(), companyUpdateState.getMc());
        assertEquals(Optional.empty(), companyUpdateState.getCompanyType());
        assertEquals(Optional.of(contacts), companyUpdateState.getContacts());
        assertEquals(Optional.of(locations), companyUpdateState.getLocations());
    }
}
