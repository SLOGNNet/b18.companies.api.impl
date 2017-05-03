package com.bridge18.company.impl.repository;

import akka.Done;
import com.bridge18.company.impl.entities.*;
import com.bridge18.readside.mongodb.readside.MongodbReadSide;
import com.bridge18.v1.dto.PaginatedSequence;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.UpdateOperations;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.bridge18.company.impl.core.CompletionStageUtils.doAll;

@Singleton
public class CompanyMongoRepository {

    private final Datastore datastore;

    @Inject
    public CompanyMongoRepository(ReadSide readSide, Datastore datastore) {
        readSide.register(CompanyEventProcessor.class);
        this.datastore = datastore;
    }

    public CompletionStage<PaginatedSequence<CompanyState>> getCompanies(int pageNumber, int pageSize) {

        List<CompanyState> companies = datastore.createQuery(CompanyState.class).asList(
                new FindOptions()
                        .skip(pageNumber > 0 ? (pageNumber - 1) * pageSize : 0)
                        .limit(pageSize)
        );

        return CompletableFuture.completedFuture(
                new PaginatedSequence<>(
                        TreePVector.from(companies),
                        pageNumber,
                        pageSize));
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
                    .setEventHandler(CompanyCreated.class,
                            this::insertCompany)
                    .setEventHandler(CompanyUpdated.class,
                            this::updateCompany)
                    .setEventHandler(CompanyDeleted.class,
                            (datastore, e) -> deleteCompany(datastore, e.getId()))
                    .build();
        }

        private CompletionStage<Done> globalPrepare(Datastore datastore) {
            return doAll(
                    CompletableFuture.runAsync(() ->
                            datastore.ensureIndexes(CompanyState.class))
            );
        }

        @Override
        public PSequence<AggregateEventTag<CompanyEvent>> aggregateTags() {
            return CompanyEvent.COMPANY_EVENT_SHARDS.allTags();
        }

        private CompletionStage<Void> insertCompany(Datastore datastore, CompanyCreated created) {
            return CompletableFuture.runAsync(() ->
                    datastore.save(
                            CompanyState.builder()
                                    .id(created.getId())
                                    .name(created.getName())
                                    .mc(created.getMc().orElse(null))
                                    .taxId(created.getTaxId().orElse(null))
                                    .companyType(created.getCompanyType().orElse(null))
                                    .contacts(created.getContacts().orElse(TreePVector.empty()))
                                    .locations(created.getLocations().orElse(TreePVector.empty()))
                                    .build()));
        }

        private CompletionStage<Void> updateCompany(Datastore datastore, CompanyUpdated companyUpdated) {
            return CompletableFuture.runAsync(() -> {
                UpdateOperations<CompanyState> updateCompanyState = setNotNullFieldsInUpdateOperations(datastore,
                        companyUpdated);

                datastore.update(
                        datastore.find(CompanyState.class).field("id").equal(companyUpdated.getId()),
                        updateCompanyState
                );
            });
        }

        private UpdateOperations<CompanyState> setNotNullFieldsInUpdateOperations(Datastore datastore, CompanyUpdated e) {
            UpdateOperations updateOperations = datastore.createUpdateOperations(CompanyState.class);

            if (!e.getName().isEmpty()) updateOperations.set("name", e.getName());
            if (e.getMc().isPresent()) updateOperations.set("mc", e.getMc());
            if (e.getTaxId().isPresent()) updateOperations.set("taxId", e.getTaxId());
            if (e.getCompanyType().isPresent()) updateOperations.set("companyType", e.getCompanyType());
            if (e.getContacts().isPresent()) updateOperations.set("contacts", e.getContacts());
            if (e.getLocations().isPresent()) updateOperations.set("locations", e.getLocations());

            return updateOperations;
        }

        private CompletionStage<Void> deleteCompany(Datastore datastore, String id) {
            return CompletableFuture.runAsync(() ->
                    datastore.delete(datastore.find(CompanyState.class).field("id").equal(id))
            );
        }
    }
}