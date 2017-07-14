package org.grails.gorm.graphql

import groovy.transform.CompileStatic
import org.grails.gorm.graphql.binding.manager.GraphQLDataBinderManager
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.impl.EntityDataFetcher
import org.grails.gorm.graphql.fetcher.manager.DefaultGraphQLDataFetcherManager
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager
import org.grails.gorm.graphql.fetcher.manager.runtime.BindingRuntimeDataFetcher
import org.grails.gorm.graphql.fetcher.manager.runtime.DeletingRuntimeDataFetcher
import org.grails.gorm.graphql.fetcher.manager.runtime.ReadingRuntimeDataFetcher
import org.grails.gorm.graphql.types.scalars.GraphQLDate
import org.grails.gorm.graphql.types.scalars.coercing.DateCoercion

import javax.annotation.PostConstruct

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLList.list
import static graphql.schema.GraphQLObjectType.newObject

import graphql.schema.*
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.response.delete.DefaultGraphQLDeleteResponseHandler
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.types.DefaultGraphQLTypeManager
import org.grails.gorm.graphql.types.GraphQLTypeManager

/**
 * Created by jameskleeh on 5/19/17.
 */
@CompileStatic
class Schema {

    protected MappingContext mappingContext

    GraphQLTypeManager typeManager
    GraphQLDeleteResponseHandler deleteResponseHandler
    GraphQLEntityNamingConvention namingConvention
    GraphQLDataBinderManager dataBinderManager
    GraphQLDataFetcherManager dataFetcherManager

    List<String> dateFormats
    boolean dateFormatLenient

    protected Map<PersistentEntity, GraphQLMapping> mappedEntities = [:]

    private boolean initialized = false

    Schema(MappingContext mappingContext, GraphQLTypeManager typeManager) {
        this(mappingContext)
        this.typeManager = typeManager
    }

    Schema(MappingContext mappingContext) {
        this.mappingContext = mappingContext
    }

    @PostConstruct
    void initialize() {
        if (typeManager == null) {
            if (namingConvention == null) {
                namingConvention = new GraphQLEntityNamingConvention()
            }
            new DefaultGraphQLTypeManager(namingConvention)
        } else {
            if (namingConvention == null) {
                namingConvention = typeManager.namingConvention
            }
        }
        if (typeManager.getType(Date) == null) {
            typeManager.registerType(Date, new GraphQLDate(new DateCoercion(dateFormats, dateFormatLenient)))
        }
        if (deleteResponseHandler == null) {
            deleteResponseHandler = new DefaultGraphQLDeleteResponseHandler()
        }
        if (dataBinderManager == null) {
            dataBinderManager = new GraphQLDataBinderManager()
        }
        if (dataFetcherManager == null) {
            dataFetcherManager = new DefaultGraphQLDataFetcherManager()
        }
        initialized = true
    }

    protected void populateIdentityArguments(PersistentEntity entity, GraphQLFieldDefinition.Builder... builders) {
        List<PersistentProperty> identities = []
        if (entity.identity != null) {
            identities.add(entity.identity)
        }
        else if (entity.compositeIdentity != null) {
            identities.addAll(entity.compositeIdentity)
        }
        identities

        identities.each { PersistentProperty identity ->
            builders.each { GraphQLFieldDefinition.Builder builder ->
                builder.argument(newArgument()
                        .name(identity.name)
                        .type((GraphQLInputType)typeManager.getType(identity.type, false)))
            }
        }
    }

    GraphQLSchema generate() {

        if (!initialized) {
            initialize()
        }

        mappingContext.persistentEntities.each { PersistentEntity entity ->
            GraphQLMapping mapping = GraphQLEntityHelper.getMapping(entity)
            if (mapping != null) {
                mappedEntities.put(entity, mapping)
            }
        }

        GraphQLObjectType.Builder queryType = newObject().name("Query")
        GraphQLObjectType.Builder mutationType = newObject().name("Mutation")

        mappedEntities.each { PersistentEntity entity, GraphQLMapping mapping ->

            GraphQLDataBinder dataBinder = dataBinderManager.getDataBinder(entity.javaClass)

            GraphQLObjectType objectType = typeManager.getObjectType(entity)

            def queryOne = newFieldDefinition()
                    .name(namingConvention.getReadSingle(entity))
                    .type(objectType)
                    .dataFetcher(new ReadingRuntimeDataFetcher(entity, dataFetcherManager, GraphQLDataFetcherType.GET))

            def queryAll = newFieldDefinition()
                    .name(namingConvention.getReadMany(entity))
                    .type(list(objectType))
                    .dataFetcher(new ReadingRuntimeDataFetcher(entity, dataFetcherManager, GraphQLDataFetcherType.LIST))

            EntityDataFetcher.ARGUMENTS.each { String name, GraphQLScalarType argType ->
                queryAll.argument(newArgument()
                        .name(name)
                        .type(argType)
                        .defaultValue(null))
            }

            GraphQLInputObjectType createObjectType = typeManager.getCreateObjectType(entity)

            def create = newFieldDefinition()
                    .name(namingConvention.getCreate(entity))
                    .type(objectType)
                    .argument(newArgument()
                        .name(entity.decapitalizedName)
                        .type(createObjectType))
                    .dataFetcher(new BindingRuntimeDataFetcher(entity, dataFetcherManager, dataBinder, GraphQLDataFetcherType.CREATE))

            GraphQLInputObjectType updateObjectType = typeManager.getUpdateObjectType(entity)

            def update = newFieldDefinition()
                    .name(namingConvention.getUpdate(entity))
                    .type(objectType)
                    .argument(newArgument()
                        .name(entity.decapitalizedName)
                        .type(updateObjectType))
                    .dataFetcher(new BindingRuntimeDataFetcher(entity, dataFetcherManager, dataBinder, GraphQLDataFetcherType.UPDATE))

            def delete = newFieldDefinition()
                    .name(namingConvention.getDelete(entity))
                    .type(deleteResponseHandler.objectType)
                    .dataFetcher(new DeletingRuntimeDataFetcher(entity, dataFetcherManager, deleteResponseHandler))

            populateIdentityArguments(entity, queryOne, delete, update)

            queryType
                .field(queryOne)
                .field(queryAll)

            mutationType
                .field(create)
                .field(delete)
                .field(update)
        }


        GraphQLSchema.newSchema()
            .query(queryType)
            .mutation(mutationType)
            .build()
    }



}
