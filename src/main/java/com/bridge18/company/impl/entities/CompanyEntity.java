package com.bridge18.company.impl.entities;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

public class CompanyEntity extends PersistentEntity<CompanyCommand, CompanyEvent, CompanyState> {


    @Override
    public Behavior initialBehavior(Optional<CompanyState> snapshotState) {

        BehaviorBuilder b = newBehaviorBuilder(
                snapshotState.orElse(CompanyState.builder().id(entityId()).name(entityTypeName()).build()));

        b.setCommandHandler(CreateCompany.class, (cmd, ctx) ->
                ctx.thenPersistAll(
                        () -> ctx.reply(state()),

                        CompanyCreated.builder()
                                .id(entityId())
                                .name(cmd.getName())
                                .mc(cmd.getMc())
                                .taxId(cmd.getTaxId())
                                .companyType(cmd.getCompanyType())
                                .contacts(cmd.getContacts())
                                .locations(cmd.getLocations())
                                .build()
                )
        );

        b.setEventHandlerChangingBehavior(
                CompanyCreated.class,
                evt -> created(
                        CompanyState
                                .builder()
                                .from(state())
                                .id(evt.getId())
                                .name(evt.getName())
                                .mc(evt.getMc())
                                .taxId(evt.getTaxId())
                                .companyType(evt.getCompanyType())
                                .contacts(evt.getContacts())
                                .locations(evt.getLocations())
                                .build())
        );
        return b.build();
    }

    private Behavior created(CompanyState state) {
        BehaviorBuilder b = newBehaviorBuilder(state);

        b.setCommandHandler(
                UpdateCompany.class,
                (cmd, ctx) ->
                        ctx.thenPersist(
                                CompanyUpdated.builder()
                                        .id(entityId())
                                        .name(cmd.getName())
                                        .mc(cmd.getMc())
                                        .taxId(cmd.getTaxId())
                                        .companyType(cmd.getCompanyType())
                                        .contacts(cmd.getContacts())
                                        .locations(cmd.getLocations())
                                        .build(),
                                evt -> {
                                    ctx.reply(state());
                                })
        );

        b.setEventHandler(
                CompanyUpdated.class,
                evt ->
                        CompanyState
                                .builder()
                                .from(state())
                                .id(evt.getId())
                                .name(evt.getName())
                                .mc(evt.getMc())
                                .taxId(evt.getTaxId())
                                .companyType(evt.getCompanyType())
                                .contacts(evt.getContacts())
                                .locations(evt.getLocations())
                                .build()
        );


        b.setCommandHandler(
                DeleteCompany.class,
                (cmd, ctx) ->
                        ctx.thenPersist(
                                CompanyDeleted.builder().id(entityId()).build(),
                                evt -> {
                                    ctx.reply(Done.getInstance());
                                })
        );

        b.setReadOnlyCommandHandler(GetCompanyInformation.class, (cmd, ctx) ->
                ctx.reply(state())
        );

        b.setEventHandlerChangingBehavior(
                CompanyDeleted.class,
                companyDeleted -> deleted(state())
        );

        return b.build();
    }

    private Behavior deleted(CompanyState state) {
        BehaviorBuilder b = newBehaviorBuilder(state);

        b.setReadOnlyCommandHandler(DeleteCompany.class, (cmd, ctx) -> ctx.reply(Done.getInstance()));

        return b.build();
    }
}
