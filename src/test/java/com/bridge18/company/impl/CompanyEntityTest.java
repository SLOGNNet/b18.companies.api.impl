package com.bridge18.company.impl;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.bridge18.company.entities.CompanyType;
import com.bridge18.company.entities.ContactInfoType;
import com.bridge18.company.impl.entities.*;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.TreePVector;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompanyEntityTest {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("CompanyEntityTest");
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }


    @Test
    public void testBlockingBeforeCreateCompany() {
        PersistentEntityTestDriver<CompanyCommand, CompanyEvent, CompanyState> persistentEntityTestDriver =
                new PersistentEntityTestDriver(system, new CompanyEntity(), "test-company-1");

        UpdateCompany updateCompanyCmd = UpdateCompany.builder().name("John").build();
        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> updateOutcome = persistentEntityTestDriver.run
                (updateCompanyCmd);
        assertTrue(updateOutcome.issues().get(0) instanceof PersistentEntityTestDriver.UnhandledCommand);

        DeleteCompany deleteCompanyCmd = DeleteCompany.builder().build();
        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> deleteOutcome = persistentEntityTestDriver.run
                (deleteCompanyCmd);
        assertTrue(deleteOutcome.issues().get(0) instanceof PersistentEntityTestDriver.UnhandledCommand);

        GetCompanyInformation getCompanyCmdInf = GetCompanyInformation.builder().build();
        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> getOutcome =
                persistentEntityTestDriver.run(getCompanyCmdInf);
        assertTrue(getOutcome.issues().get(0) instanceof PersistentEntityTestDriver.UnhandledCommand);
    }

    @Test
    public void testBlockingAfterCompanyDeleted() {
        PersistentEntityTestDriver<CompanyCommand, CompanyEvent, CompanyState> persistentEntityTestDriver =
                new PersistentEntityTestDriver(system, new CompanyEntity(), "test-company-2");

        CreateCompany createDriverCmd = CreateCompany.builder().name("John").build();
        persistentEntityTestDriver.run(createDriverCmd);

        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> createOutcome = persistentEntityTestDriver.run
                (createDriverCmd);
        assertTrue(createOutcome.issues().get(0) instanceof PersistentEntityTestDriver.UnhandledCommand);

        //delete company
        DeleteCompany deleteCompany = DeleteCompany.builder().build();
        persistentEntityTestDriver.run(deleteCompany);

        GetCompanyInformation getCompanyInformation = GetCompanyInformation.builder().build();
        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> getOutcome = persistentEntityTestDriver.run
                (getCompanyInformation);
        assertTrue(getOutcome.issues().get(0) instanceof PersistentEntityTestDriver.UnhandledCommand);

        UpdateCompany updateCompany = UpdateCompany.builder().name("John").build();
        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> updateOutcome = persistentEntityTestDriver.run
                (updateCompany);
        assertTrue(updateOutcome.issues().get(0) instanceof PersistentEntityTestDriver.UnhandledCommand);

        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> createOutcome_2 = persistentEntityTestDriver.run
                (createDriverCmd);
        assertTrue(createOutcome_2.issues().get(0) instanceof PersistentEntityTestDriver.UnhandledCommand);
    }

    @Test
    public void test() {
        PersistentEntityTestDriver<CompanyCommand, CompanyEvent, CompanyState> persistentEntityTestDriver =
                new PersistentEntityTestDriver(system, new CompanyEntity(), "test-company-3");


        Address address = Address.builder()
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

        TreePVector<ContactInfo> contactInfos = TreePVector.from(
                Arrays.asList(
                        ContactInfo.builder().label("label-1").value("value-1").type(ContactInfoType.NONE).build(),
                        ContactInfo.builder().label("label-2").value("value-2").type(ContactInfoType.NONE).build(),
                        ContactInfo.builder().label("label-3").value("value-3").type(ContactInfoType.NONE).build()
                )
        );

        TreePVector<Location> locations = TreePVector.from(
                Arrays.asList(
                        Location.builder().name("Location1").address(address).contactInfo(contactInfos).build(),
                        Location.builder().name("Location2").address(address).contactInfo(contactInfos).build()
                )
        );

        TreePVector<Contact> contacts = TreePVector.from(
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

        //create command
        CreateCompany createCompanyCmd = CreateCompany
                .builder()
                .name("company")
                .mc("MC1")
                .taxId("1111")
                .companyType(CompanyType.CARRIER)
                .contacts(contacts)
                .locations(locations)
                .build();

        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> createOutcome = persistentEntityTestDriver.run
                (createCompanyCmd);

        assertEquals(1, createOutcome.events().size());

        CompanyState companyState = (CompanyState) createOutcome.getReplies().get(0);
        assertEquals("test-company-3", companyState.getId());
        assertEquals("company", companyState.getName());
        assertEquals(Optional.of("1111"), companyState.getTaxId());
        assertEquals(Optional.of("MC1"), companyState.getMc());
        assertEquals(Optional.of(CompanyType.CARRIER), companyState.getCompanyType());
        assertEquals(Optional.of(contacts), companyState.getContacts());
        assertEquals(Optional.of(locations), companyState.getLocations());

        //update command
        UpdateCompany updateCompanyCmd = UpdateCompany.builder()
                .name("company-2")
                .mc("MC-2")
                .taxId("2222")
                .companyType(CompanyType.CARRIER)
                .contacts(contacts)
                .locations(locations)
                .build();

        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> updateOutcome = persistentEntityTestDriver.run
                (updateCompanyCmd);

        assertEquals(1, updateOutcome.events().size());
        companyState = (CompanyState) updateOutcome.getReplies().get(0);
        assertEquals("test-company-3", companyState.getId());
        assertEquals("company-2", companyState.getName());
        assertEquals(Optional.of("2222"), companyState.getTaxId());
        assertEquals(Optional.of("MC-2"), companyState.getMc());
        assertEquals(Optional.of(CompanyType.CARRIER), companyState.getCompanyType());
        assertEquals(Optional.of(contacts), companyState.getContacts());
        assertEquals(Optional.of(locations), companyState.getLocations());


        //get Information command
        GetCompanyInformation getCompanyInformation = GetCompanyInformation.builder().build();
        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> getInfoCompany = persistentEntityTestDriver
                .run(getCompanyInformation );

        assertEquals(0, getInfoCompany.events().size());

        companyState = (CompanyState) getInfoCompany.getReplies().get(0);
        assertEquals("test-company-3", companyState.getId());
        assertEquals("company-2", companyState.getName());
        assertEquals(Optional.of("2222"), companyState.getTaxId());
        assertEquals(Optional.of("MC-2"), companyState.getMc());
        assertEquals(Optional.of(CompanyType.CARRIER), companyState.getCompanyType());
        assertEquals(Optional.of(contacts), companyState.getContacts());
        assertEquals(Optional.of(locations), companyState.getLocations());

        //delete company command
        DeleteCompany deleteCompanyCmd = DeleteCompany.builder().build();
        PersistentEntityTestDriver.Outcome<CompanyEvent, CompanyState> deleteCompanyOutcome = persistentEntityTestDriver
                .run(deleteCompanyCmd);
        assertEquals(1, deleteCompanyOutcome.events().size());
        assertEquals(CompanyDeleted.builder().id("test-company-3").build(), deleteCompanyOutcome.events().get(0));
    }


}
