package com.bridge18.company.impl.repository;

import akka.Done;
import com.bridge18.company.impl.entities.*;
import com.bridge18.company.v1.dto.company.*;
import com.bridge18.readside.mongodb.readside.MongodbReadSide;
import com.bridge18.v1.dto.PaginatedSequence;
import com.google.common.collect.Lists;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.FindOptions;
import org.pcollections.PSequence;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.bridge18.company.impl.core.CompletionStageUtils.doAll;

@Singleton
public class CompanyMongoRepository {

    private final Datastore datastore;

    @Inject
    public CompanyMongoRepository(ReadSide readSide, Datastore datastore) {
        readSide.register(CompanyEventProcessor.class);
        this.datastore = datastore;
    }

    public CompletionStage<PaginatedSequence<CompanyDTO>> getCompanies(int pageNumber, int pageSize) {

        List<CompanyDTO> companies = datastore.createQuery(CompanyDTO.class).asList(
                new FindOptions()
                        .skip(pageNumber > 0 ? (pageNumber - 1) * pageSize : 0)
                        .limit(pageSize)
        );
        return CompletableFuture.completedFuture(
                new PaginatedSequence<>(
                        TreePVector.from(companies),
                        null,
                        pageSize,
                        companies.size()));
    }

    private static CompanyDTO convertCompanyToCompanyDTO(CompanyEvent company) {
        if (company instanceof CompanyCreated) {
            CompanyCreated created = (CompanyCreated) company;
            return new CompanyDTO(
                    created.getId(),
                    created.getName(),
                    created.getMc().orElse(null),
                    created.getTaxId().orElse(null),
                    created.getCompanyType().orElse(null),
                    created
                            .getContacts().orElse(TreePVector.empty()).stream()
                            .map(CompanyMongoRepository::convertContactsToContactDTOs)
                            .collect(Collectors.toList()),
                    created
                            .getLocations().orElse(TreePVector.empty()).stream()
                            .map(CompanyMongoRepository::convertLocationsToLocationDTOs)
                            .collect(Collectors.toList()));

        } else {
            CompanyUpdated updated = (CompanyUpdated) company;
            return new CompanyDTO(
                    updated.getId(),
                    updated.getName(),
                    updated.getMc().orElse(null),
                    updated.getTaxId().orElse(null),
                    updated.getCompanyType().orElse(null),
                    updated
                            .getContacts().orElse(TreePVector.empty()).stream()
                            .map(CompanyMongoRepository::convertContactsToContactDTOs)
                            .collect(Collectors.toList()),
                    updated
                            .getLocations().orElse(TreePVector.empty()).stream()
                            .map(CompanyMongoRepository::convertLocationsToLocationDTOs)
                            .collect(Collectors.toList()));
        }

    }

    private static ContactDTO convertContactsToContactDTOs(Contact contact) {
        return new ContactDTO(
                contact.getId().orElse(null),
                contact.getFirstName().orElse(null),
                contact.getMiddleName().orElse(null),
                contact.getLastName().orElse(null),
                convertContactInfoPVectorToList(contact.getContactInfo().orElse(TreePVector.empty())),
                contact.getPosition().orElse(null),
                convertAddressToAddressDTO(contact.getAddress().orElse(null)));
    }

    private static LocationDTO convertLocationsToLocationDTOs(Location location) {

        return new LocationDTO(
                location.getName().orElse(null),
                convertAddressToAddressDTO(location.getAddress().orElse(Address.builder().build())),
                convertContactInfoPVectorToList(location.getContactInfo().orElse(TreePVector.empty())));
    }

    private static List<ContactInfoDTO> convertContactInfoPVectorToList(PVector<ContactInfo> contactInfos) {
        contactInfos = Optional.ofNullable(contactInfos).orElse(TreePVector.empty());
        return Lists.transform(contactInfos, contactInfo ->
                new ContactInfoDTO(contactInfo.getLabel().orElse(null),
                        contactInfo.getValue().orElse(null),
                        contactInfo.getType().orElse(null)
                )
        );
    }

    private static AddressDTO convertAddressToAddressDTO(Address address) {
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


    private static class CompanyEventProcessor extends ReadSideProcessor<CompanyEvent> {

        private final MongodbReadSide mongodbReadSide;

        @Inject
        public CompanyEventProcessor(MongodbReadSide mongodbReadSide) {
            this.mongodbReadSide = mongodbReadSide;
        }

        @Override
        public ReadSideHandler<CompanyEvent> buildHandler() {
            return mongodbReadSide.<CompanyEvent>builder("mongodbCompanyOffset")
                    .setGlobalPrepare(this::globalPrepare)
                    .setPrepare(this::prepareStatements)
                    .setEventHandler(CompanyCreated.class,
                            (datastore, e) ->
                                    insertCompany(datastore,
                                            convertCompanyToCompanyDTO(e)))
                    .setEventHandler(CompanyUpdated.class,
                            (datastore, e) ->
                                    updateCompany(datastore,
                                            convertCompanyToCompanyDTO(e)))
                    .setEventHandler(CompanyDeleted.class,
                            (datastore, e) -> deleteCompany(datastore, e.getId()))
                    .build();
        }

        private CompletionStage<Done> globalPrepare(Datastore datastore) {
            return doAll(
                    CompletableFuture.runAsync(() ->
                            datastore.ensureIndexes(CompanyDTO.class))
            );
        }

        private CompletionStage<Done> prepareStatements(Datastore datastore, AggregateEventTag<CompanyEvent>
                eventAggregateEventTag) {
            return doAll(

            );
        }

        @Override
        public PSequence<AggregateEventTag<CompanyEvent>> aggregateTags() {
            return CompanyEvent.COMPANY_EVENT_SHARDS.allTags();
        }

        private CompletionStage<Void> insertCompany(Datastore datastore, CompanyDTO company) {
            return CompletableFuture.runAsync(() ->
                    datastore.save(company)
            );
        }

        private CompletionStage<Void> updateCompany(Datastore datastore, CompanyDTO company) {

            return CompletableFuture.runAsync(() ->
                    datastore.update(
                            datastore.find(CompanyDTO.class).field("id").equal(company.id),
                            datastore.createUpdateOperations(CompanyDTO.class)
                                    .set("name", company.name)
                                    .set("mc", company.mc)
                                    .set("taxId", company.taxId)
                                    .set("companyType", company.companyType)
                                    .set("contacts", Optional.ofNullable(company.contacts).isPresent() ? company
                                            .contacts : Collections.EMPTY_LIST)
                                    .set("locations", Optional.ofNullable(company.locations).isPresent() ? company
                                            .locations : Collections.EMPTY_LIST)
                    )
            );
        }

        private CompletionStage<Void> deleteCompany(Datastore datastore, String id){

            return CompletableFuture.runAsync(() ->
                    datastore.delete(datastore.find(CompanyDTO.class).field("id").equal(id))
            );
        }


    }

}

