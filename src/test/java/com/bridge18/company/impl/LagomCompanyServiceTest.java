package com.bridge18.company.impl;

import akka.actor.ActorSystem;
import com.bridge18.company.entities.CompanyType;
import com.bridge18.company.entities.ContactInfoType;
import com.bridge18.company.v1.api.LagomCompanyService;
import com.bridge18.company.v1.dto.company.*;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LagomCompanyServiceTest {
    static ActorSystem system;

    private final static ServiceTest.Setup setup = defaultSetup().withCassandra(true)
            .configureBuilder(b ->
                    b.configure("cassandra-query-journal.eventual-consistency-delay", "0")
            );

    private static ServiceTest.TestServer testServer;
    private static LagomCompanyService testService;

    @BeforeClass
    public static void beforeAll() {
        system = ActorSystem.create("LagomCompanyServiceTest");

        testServer = ServiceTest.startServer(setup);
        testService = testServer.client(LagomCompanyService.class);
    }

    @Test
    public void test() throws InterruptedException, ExecutionException, TimeoutException {

        AddressDTO addressDTO = new AddressDTO(
                1, "addressName-1", "streetAddress1-1", "streetAddress2-1", "city-1",
                "addressPhone-1", "state-1", "zip-1", "addressFax-1", "addressPhoneExtension-1",
                "addressFaxExtension-1", 1.0, 1.0
        );

        List<ContactInfoDTO> contactInfos = Arrays.asList(
                new ContactInfoDTO("label-1", "value-1", ContactInfoType.NONE),
                new ContactInfoDTO("label-2", "value-2", ContactInfoType.NONE)
        );

        List<LocationDTO> locationDTOs = Arrays.asList(
                new LocationDTO("NY-1", addressDTO, contactInfos),
                new LocationDTO("NY-2", addressDTO, contactInfos)
        );

        List<ContactDTO> contactDTOs = Arrays.asList(
                new ContactDTO("1", "John-1", "John-1", "John-1", contactInfos, "1", addressDTO),
                new ContactDTO("2", "John-2", "John-2", "John-2", contactInfos, "2", addressDTO)
        );

        CompanyDTO getCompanyInfo = new CompanyDTO(null, "John-1", "mc-1", "1111", CompanyType.CARRIER, contactDTOs,
                locationDTOs);
        CompanyDTO createCompanyDTO = testService.createCompany().invoke(getCompanyInfo).toCompletableFuture().get(10,
                SECONDS);

        assertNotNull(createCompanyDTO.id);
        assertEquals("John-1", createCompanyDTO.name);
        assertEquals("mc-1", createCompanyDTO.mc);
        assertEquals("1111", createCompanyDTO.taxId);
        assertEquals(CompanyType.CARRIER, createCompanyDTO.companyType);
        assertEquals(contactDTOs, createCompanyDTO.contacts);
        assertEquals(locationDTOs, createCompanyDTO.locations);

        getCompanyInfo = new CompanyDTO(null, "John-2", "mc-2", "2222", CompanyType.CARRIER, contactDTOs,
                locationDTOs);
        CompanyDTO updateCompanyDTO = testService.updateCompany(createCompanyDTO.id).invoke(getCompanyInfo)
                .toCompletableFuture().get(10, SECONDS);

        assertNotNull(updateCompanyDTO.id);
        assertEquals("John-2", updateCompanyDTO.name);
        assertEquals("mc-2", updateCompanyDTO.mc);
        assertEquals("2222", updateCompanyDTO.taxId);
        assertEquals(CompanyType.CARRIER, updateCompanyDTO.companyType);
        assertEquals(contactDTOs, updateCompanyDTO.contacts);
        assertEquals(locationDTOs, updateCompanyDTO.locations);


        getCompanyInfo = testService.getCompany(createCompanyDTO.id).invoke().toCompletableFuture().get(10, SECONDS);
        assertNotNull(getCompanyInfo.id);
        assertEquals("John-2", getCompanyInfo.name);
        assertEquals("mc-2", getCompanyInfo.mc);
        assertEquals("2222", getCompanyInfo.taxId);
        assertEquals(CompanyType.CARRIER, getCompanyInfo.companyType);
        assertEquals(contactDTOs, getCompanyInfo.contacts);
        assertEquals(locationDTOs, getCompanyInfo.locations);

        testService.deleteCompany(getCompanyInfo.id).invoke().toCompletableFuture().get(10, SECONDS);


    }
}
