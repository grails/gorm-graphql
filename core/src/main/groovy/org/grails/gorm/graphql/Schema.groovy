package org.grails.gorm.graphql

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLList.list
import static graphql.schema.GraphQLObjectType.newObject
import static org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType.GET
import static org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType.LIST
import static org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType.CREATE
import static org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType.UPDATE
import graphql.schema.DataFetcher
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.binding.manager.GraphQLDataBinderManager
import org.grails.gorm.graphql.entity.property.manager.DefaultGraphQLDomainPropertyManager
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.impl.EntityDataFetcher
import org.grails.gorm.graphql.fetcher.manager.DefaultGraphQLDataFetcherManager
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager
import org.grails.gorm.graphql.fetcher.runtime.BindingRuntimeDataFetcher
import org.grails.gorm.graphql.fetcher.runtime.DeletingRuntimeDataFetcher
import org.grails.gorm.graphql.fetcher.runtime.ReadingRuntimeDataFetcher
import org.grails.gorm.graphql.types.scalars.GraphQLDate
import org.grails.gorm.graphql.types.scalars.coercing.DateCoercion
import javax.annotation.PostConstruct
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.response.delete.DefaultGraphQLDeleteResponseHandler
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler
import org.grails.gorm.graphql.binding.GraphQLDataBinder
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
    boolean dateFormatLenient = false
    boolean runtimeDataFetching = false
    Map<String, GraphQLInputType> listArguments

    private boolean initialized = false

    Schema(MappingContext mappingContext, GraphQLTypeManager typeManager) {
        this(mappingContext)
        this.typeManager = typeManager
    }

    Schema(MappingContext mappingContext) {
        this.mappingContext = mappingContext
    }

    void setListArguments(Map<String, Class> arguments) {
        listArguments = [:]
        for (Map.Entry<String, Class> entry: arguments) {
            GraphQLType type = typeManager.getType(entry.value)
            if (type == null) {
                throw new IllegalArgumentException("Error while setting list arguments. GraphQLType could not be found for ${entry.value.name}")
            }
            if (!(type instanceof GraphQLInputType)) {
                throw new IllegalArgumentException("Error while setting list arguments. Invalid type found for ${entry.value.name}. GraphQLType found ${type.name} of type ${type.class.name} is not an instance of ${GraphQLInputType.name}")
            }
            listArguments.put(entry.key, (GraphQLInputType)type)
        }
    }

    @PostConstruct
    void initialize() {
        if (typeManager == null) {
            if (namingConvention == null) {
                namingConvention = new GraphQLEntityNamingConvention()
            }
            typeManager = new DefaultGraphQLTypeManager(namingConvention, null, new DefaultGraphQLDomainPropertyManager())
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
        if (listArguments == null) {
            listArguments = EntityDataFetcher.ARGUMENTS
        }
        initialized = true
    }

    @SuppressWarnings('NestedForLoop')
    protected void populateIdentityArguments(PersistentEntity entity, GraphQLFieldDefinition.Builder... builders) {
        List<PersistentProperty> identities = []
        if (entity.identity != null) {
            identities.add(entity.identity)
        }
        else if (entity.compositeIdentity != null) {
            identities.addAll(entity.compositeIdentity)
        }

        for (PersistentProperty identity: identities) {
            for (GraphQLFieldDefinition.Builder builder: builders) {
                builder.argument(newArgument()
                        .name(identity.name)
                        .type((GraphQLInputType)typeManager.getType(identity.type, false)))
            }
        }
    }

    protected DataFetcher getReadingFetcher(PersistentEntity entity, GraphQLDataFetcherType type) {
        DataFetcher fetcher
        if (runtimeDataFetching) {
            fetcher = new ReadingRuntimeDataFetcher(entity, dataFetcherManager, type)
        }
        else {
            fetcher = dataFetcherManager.getReadingFetcher(entity, type)
        }
        fetcher
    }

    protected DataFetcher getBindingFetcher(PersistentEntity entity, GraphQLDataFetcherType type, GraphQLDataFetcherManager manager, GraphQLDataBinderManager dataBinderManager) {
        DataFetcher fetcher
        if (runtimeDataFetching) {
            fetcher = new BindingRuntimeDataFetcher(entity, manager, dataBinderManager, type)
        }
        else {
            GraphQLDataBinder dataBinder = dataBinderManager.getDataBinder(entity.javaClass)
            fetcher = dataFetcherManager.getBindingFetcher(entity, dataBinder, type)
        }
        fetcher
    }

    protected DataFetcher getDeletingFetcher(PersistentEntity entity, GraphQLDataFetcherManager manager, GraphQLDeleteResponseHandler responseHandler) {
        DataFetcher fetcher
        if (runtimeDataFetching) {
            fetcher = new DeletingRuntimeDataFetcher(entity, manager, responseHandler)
        }
        else {
            fetcher = dataFetcherManager.getDeletingFetcher(entity, responseHandler)
        }
        fetcher
    }

    @SuppressWarnings(['AbcMetric', 'NestedForLoop'])
    GraphQLSchema generate() {

        if (!initialized) {
            initialize()
        }

        GraphQLObjectType.Builder queryType = newObject().name('Query')
        GraphQLObjectType.Builder mutationType = newObject().name('Mutation')

        for (PersistentEntity entity: mappingContext.persistentEntities) {

            if (GraphQLEntityHelper.getMapping(entity) == null) {
                continue
            }

            //GET
            GraphQLObjectType objectType = typeManager.getQueryType(entity)

            GraphQLFieldDefinition.Builder queryOne = newFieldDefinition()
                    .name(namingConvention.getGet(entity))
                    .type(objectType)
                    .dataFetcher(getReadingFetcher(entity, GET))

            GraphQLFieldDefinition.Builder queryAll = newFieldDefinition()
                    .name(namingConvention.getList(entity))
                    .type(list(objectType))
                    .dataFetcher(getReadingFetcher(entity, LIST))

            for (Map.Entry<String, GraphQLInputType> argument: listArguments) {
                queryAll.argument(newArgument()
                        .name(argument.key)
                        .type(argument.value)
                        .defaultValue(null))
            }

            GraphQLInputObjectType createObjectType = typeManager.getMutationType(entity, GraphQLPropertyType.CREATE)

            GraphQLFieldDefinition.Builder create = newFieldDefinition()
                    .name(namingConvention.getCreate(entity))
                    .type(objectType)
                    .argument(newArgument()
                        .name(entity.decapitalizedName)
                        .type(createObjectType))
                    .dataFetcher(getBindingFetcher(entity, CREATE, dataFetcherManager, dataBinderManager))

            GraphQLInputObjectType updateObjectType = typeManager.getMutationType(entity, GraphQLPropertyType.UPDATE)

            GraphQLFieldDefinition.Builder update = newFieldDefinition()
                    .name(namingConvention.getUpdate(entity))
                    .type(objectType)
                    .argument(newArgument()
                        .name(entity.decapitalizedName)
                        .type(updateObjectType))
                    .dataFetcher(getBindingFetcher(entity, UPDATE, dataFetcherManager, dataBinderManager))

            GraphQLFieldDefinition.Builder delete = newFieldDefinition()
                    .name(namingConvention.getDelete(entity))
                    .type(deleteResponseHandler.objectType)
                    .dataFetcher(getDeletingFetcher(entity, dataFetcherManager, deleteResponseHandler))

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
