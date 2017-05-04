package com.bridge18.company.impl.services.lagom;

import akka.Done;
import akka.NotUsed;
import com.bridge18.company.impl.entities.*;
import com.bridge18.company.impl.repository.CompanyMongoRepository;
import com.bridge18.company.impl.services.objects.CompanyService;
import com.bridge18.company.v1.api.LagomCompanyService;
import com.bridge18.company.v1.dto.company.*;
import com.bridge18.exception.LagomException;
import com.bridge18.v1.dto.PaginatedSequence;
import com.google.common.collect.Lists;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LagomCompanyServiceImpl implements LagomCompanyService {
    private static final int PAGE_SIZE = 20;

    private CompanyService companyService;
    private CompanyMongoRepository companyMongoRepository;

    @Inject
    public LagomCompanyServiceImpl(CompanyService companyService, CompanyMongoRepository companyMongoRepository) {
        this.companyService = companyService;
        this.companyMongoRepository = companyMongoRepository;
    }

    @Override
    public ServiceCall<CompanyDTO, CompanyDTO> createCompany() {
        return request -> {
            PVector<Contact> contacts = request.contacts != null ?
                    convertContactDTOListToContactPVector(request.contacts) : null;

            PVector<Location> locations = request.locations != null ?
                    convertLocationDTOListToLocationPVector(request.locations) : null;

            return companyService.createCompany(request.name,
                    Optional.ofNullable(Optional.ofNullable(request.taxId).orElseThrow(
                            () -> new LagomException("taxId is mandatory", 400, "taxid == null", "taxId"))),
                    Optional.ofNullable(Optional.ofNullable(request.mc).orElseThrow(
                            () -> new WebServiceException("Type field is mandatory"))),
                    Optional.ofNullable(request.companyType),
                    Optional.ofNullable(contacts),
                    Optional.ofNullable(locations))

                    .thenApply(this::convertCompanyStateToCompanyDTO);
        };
    }

    @Override
    public ServiceCall<NotUsed, PaginatedSequence<CompanyDTO>> getCompanies(Optional<Integer> pageNumber,
                                                                            Optional<Integer> pageSize) {
        return request ->
                companyMongoRepository.getCompanies(pageNumber.orElse(0), PAGE_SIZE)
                        .thenApply(e ->
                                new PaginatedSequence<>(
                                        TreePVector.from(e.getValues()
                                                .stream().map(this::convertCompanyStateToCompanyDTO)
                                                .collect(Collectors.toList())),
                                        e.getPageNumber(),
                                        e.getPageSize()));

    }

    @Override
    public ServiceCall<CompanyDTO, CompanyDTO> updateCompany(String id) {
        return request -> {
            PVector<Contact> contacts = request.contacts != null ?
                    convertContactDTOListToContactPVector(request.contacts) : null;

            PVector<Location> locations = request.locations != null ?
                    convertLocationDTOListToLocationPVector(request.locations) : null;

            return companyService.updateCompany(
                    id,
                    request.name,
                    Optional.ofNullable(request.taxId),
                    Optional.ofNullable(request.mc),
                    Optional.ofNullable(request.companyType),
                    Optional.ofNullable(contacts),
                    Optional.ofNullable(locations))

                    .thenApply(this::convertCompanyStateToCompanyDTO);
        };
    }

    @Override
    public ServiceCall<NotUsed, CompanyDTO> getCompany(String id) {
        return request ->
                companyService.getCompany(id)

                        .thenApply(this::convertCompanyStateToCompanyDTO);
    }

    @Override
    public ServiceCall<NotUsed, Done> deleteCompany(String id) {
        return request -> companyService.deleteCompany(id)
                .thenApply(companyState ->
                        Done.getInstance());
    }

    private CompanyDTO convertCompanyStateToCompanyDTO(CompanyState companyState) {
        List<ContactDTO> contactDTOS = companyState.getContacts().isPresent() ?
                Lists.transform(companyState.getContacts().get(), contact ->
                        new ContactDTO(contact.getId().orElse(null),
                                contact.getFirstName().orElse(null),
                                contact.getMiddleName().orElse(null),
                                contact.getLastName().orElse(null),
                                convertContactInfoPVectorToList(
                                        contact.getContactInfo().orElse(TreePVector.empty())
                                ),
                                contact.getPosition().orElse(null),
                                convertAddressToAddressDTO(contact.getAddress().orElse(Address.builder().build()))
                        )
                ) : null;

        List<LocationDTO> locationDTOS = companyState.getLocations().isPresent() ?
                Lists.transform(companyState.getLocations().get(), location ->
                        new LocationDTO(location.getName().orElse(null),
                                convertAddressToAddressDTO(location.getAddress().orElse(Address.builder().build())),
                                convertContactInfoPVectorToList(
                                        location.getContactInfo().orElse(TreePVector.empty())
                                )
                        )
                ) : null;

        CompanyDTO companyDTO = new CompanyDTO(companyState.getId(),
                companyState.getName(),
                companyState.getMc().orElse(null),
                companyState.getTaxId().orElse(null),
                companyState.getCompanyType().orElse(null),
                contactDTOS,
                locationDTOS
        );
        return companyDTO;
    }

    private PVector<Contact> convertContactDTOListToContactPVector(List<ContactDTO> contactDTOList) {
        contactDTOList = Optional.ofNullable(contactDTOList).orElse(new ArrayList<>());
        return TreePVector.from(
                Lists.transform(contactDTOList, contactDTO ->
                        Contact.builder()
                                .id(Optional.ofNullable(contactDTO.id))
                                .firstName(Optional.ofNullable(contactDTO.firstName))
                                .middleName(Optional.ofNullable(contactDTO.middleName))
                                .lastName(Optional.ofNullable(contactDTO.lastName))
                                .contactInfo(Optional.ofNullable(contactDTO.contactInfo != null ?
                                        convertContactInfoListToPVector(contactDTO.contactInfo) : null)
                                )
                                .position(Optional.ofNullable(contactDTO.position))
                                .address(Optional.ofNullable(contactDTO.address != null ?
                                        convertAddressDTOToAddress(contactDTO.address) : null)
                                )
                                .build()
                )
        );
    }

    public String name;
    public AddressDTO address;
    public List<ContactInfoDTO> contactInfo;

    private PVector<Location> convertLocationDTOListToLocationPVector(List<LocationDTO> locationDTOList) {
        locationDTOList = Optional.ofNullable(locationDTOList).orElse(new ArrayList<>());
        return TreePVector.from(
                Lists.transform(locationDTOList, locationDTO ->
                        Location.builder()
                                .name(Optional.ofNullable(locationDTO.name))
                                .address(Optional.ofNullable(locationDTO.address != null ?
                                        convertAddressDTOToAddress(locationDTO.address) : null)
                                )
                                .contactInfo(
                                        Optional.ofNullable(locationDTO.contactInfo != null ?
                                                convertContactInfoListToPVector(locationDTO.contactInfo) : null)
                                )
                                .build()
                )
        );
    }

    private List<ContactInfoDTO> convertContactInfoPVectorToList(PVector<ContactInfo> contactInfos) {
        contactInfos = Optional.ofNullable(contactInfos).orElse(TreePVector.empty());
        return Lists.transform(contactInfos, contactInfo ->
                new ContactInfoDTO(contactInfo.getLabel().orElse(null),
                        contactInfo.getValue().orElse(null),
                        contactInfo.getType().orElse(null)
                )
        );
    }

    private PVector<ContactInfo> convertContactInfoListToPVector(List<ContactInfoDTO> contactInfos) {
        contactInfos = Optional.ofNullable(contactInfos).orElse(new ArrayList<>());
        return TreePVector.from(
                Lists.transform(contactInfos, contactInfoDTO ->
                        ContactInfo.builder()
                                .label(Optional.ofNullable(contactInfoDTO.label))
                                .value(Optional.ofNullable(contactInfoDTO.value))
                                .type(Optional.ofNullable(contactInfoDTO.type))
                                .build()
                )
        );
    }

    private AddressDTO convertAddressToAddressDTO(Address address) {
        address = Optional.ofNullable(address).orElse(Address.builder().build());
        return new AddressDTO(address.getAddressId().orElse(null),
                address.getAddressName().orElse(null), address.getStreetAddress1().orElse(null),
                address.getStreetAddress2().orElse(null), address.getCity().orElse(null),
                address.getAddressPhone().orElse(null), address.getState().orElse(null),
                address.getZip().orElse(null), address.getAddressFax().orElse(null),
                address.getAddressPhoneExtension().orElse(null), address.getAddressFaxExtension().orElse(null),
                address.getAddressLatitude().orElse(null), address.getAddressLongitude().orElse(null)
        );
    }

    private Address convertAddressDTOToAddress(AddressDTO addressDTO) {
        addressDTO = Optional.ofNullable(addressDTO).orElse(new AddressDTO());
        return Address.builder()
                .addressId(Optional.ofNullable(addressDTO.id))
                .addressName(Optional.ofNullable(addressDTO.name))
                .streetAddress1(Optional.ofNullable(addressDTO.streetAddress1))
                .streetAddress2(Optional.ofNullable(addressDTO.streetAddress2))
                .city(Optional.ofNullable(addressDTO.city))
                .addressPhone(Optional.ofNullable(addressDTO.phone))
                .state(Optional.ofNullable(addressDTO.state))
                .zip(Optional.ofNullable(addressDTO.zip))
                .addressFax(Optional.ofNullable(addressDTO.fax))
                .addressPhoneExtension(Optional.ofNullable(addressDTO.phoneExtension))
                .addressFaxExtension(Optional.ofNullable(addressDTO.faxExtension))
                .addressLatitude(Optional.ofNullable(addressDTO.latitude))
                .addressLongitude(Optional.ofNullable(addressDTO.longitude))
                .build();
    }
}
