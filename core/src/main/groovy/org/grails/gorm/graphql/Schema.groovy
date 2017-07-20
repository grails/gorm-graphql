package org.grails.gorm.graphql

import graphql.schema.*
import groovy.transform.CompileStatic
import javassist.Modifier
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.binding.manager.DefaultGraphQLDataBinderManager
import org.grails.gorm.graphql.binding.manager.GraphQLDataBinderManager
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.entity.operations.CustomOperation
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.DefaultGraphQLDomainPropertyManager
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.impl.EntityDataFetcher
import org.grails.gorm.graphql.fetcher.manager.DefaultGraphQLDataFetcherManager
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager
import org.grails.gorm.graphql.fetcher.runtime.BindingRuntimeDataFetcher
import org.grails.gorm.graphql.fetcher.runtime.DeletingRuntimeDataFetcher
import org.grails.gorm.graphql.fetcher.runtime.ReadingRuntimeDataFetcher
import org.grails.gorm.graphql.response.delete.DefaultGraphQLDeleteResponseHandler
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler
import org.grails.gorm.graphql.types.DefaultGraphQLTypeManager
import org.grails.gorm.graphql.types.GraphQLTypeManager
import org.grails.gorm.graphql.types.scalars.GraphQLDate
import org.grails.gorm.graphql.types.scalars.coercing.DateCoercion

import javax.annotation.PostConstruct

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLList.list
import static graphql.schema.GraphQLObjectType.newObject
import static org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType.*

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
        if (arguments != null) {
            listArguments = [:]
            for (Map.Entry<String, Class> entry: arguments) {
                GraphQLType type = typeManager.getType(entry.value)
                if (!(type instanceof GraphQLInputType)) {
                    throw new IllegalArgumentException("Error while setting list arguments. Invalid type found for ${entry.value.name}. GraphQLType found ${type.name} of type ${type.class.name} is not an instance of ${GraphQLInputType.name}")
                }
                listArguments.put(entry.key, (GraphQLInputType)type)
            }
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
        if (!typeManager.hasType(Date)) {
            typeManager.registerType(Date, new GraphQLDate(new DateCoercion(dateFormats, dateFormatLenient)))
        }
        if (deleteResponseHandler == null) {
            deleteResponseHandler = new DefaultGraphQLDeleteResponseHandler()
        }
        if (dataBinderManager == null) {
            dataBinderManager = new DefaultGraphQLDataBinderManager()
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
        Map<String, Class> identities = [:]

        if (entity.identity != null) {
            identities.put(entity.identity.name, entity.identity.type)
        }
        else if (entity.compositeIdentity != null) {
            for (PersistentProperty identity: entity.compositeIdentity) {
                if (identity instanceof Association) {
                    PersistentEntity associatedEntity = ((Association)identity).associatedEntity
                    if (associatedEntity.identity != null) {
                        identities.put(identity.name, associatedEntity.identity.type)
                    }
                    else {
                        throw new UnsupportedOperationException("Mapping domain classes with nested composite keys is not currently supported. ${identity.toString()} has a composite key.")
                    }
                }
                else {
                    identities.put(identity.name, identity.type)
                }
            }
        }

        for (Map.Entry<String, Class> identity: identities) {
            GraphQLInputType inputType = (GraphQLInputType)typeManager.getType(identity.value, false)

            for (GraphQLFieldDefinition.Builder builder: builders) {
                builder.argument(newArgument()
                        .name(identity.key)
                        .type(inputType))
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
            fetcher = new BindingRuntimeDataFetcher(entity, manager, type, dataBinderManager)
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

    @SuppressWarnings(['AbcMetric', 'NestedForLoop', 'MethodSize'])
    GraphQLSchema generate() {

        if (!initialized) {
            initialize()
        }

        GraphQLObjectType.Builder queryType = newObject().name('Query')
        GraphQLObjectType.Builder mutationType = newObject().name('Mutation')

        for (PersistentEntity entity: mappingContext.persistentEntities) {
            GraphQLMapping mapping = GraphQLEntityHelper.getMapping(entity)
            if (mapping == null) {
                continue
            }

            List<GraphQLFieldDefinition.Builder> queryFields = []
            List<GraphQLFieldDefinition.Builder> mutationFields = []

            GraphQLOutputType objectType = typeManager.getQueryType(entity, GraphQLPropertyType.OUTPUT)

            List<GraphQLFieldDefinition.Builder> requiresIdentityArguments = []
            List<Closure> postIdentityExecutables = []

            if (mapping.operations.get) {

                GraphQLFieldDefinition.Builder queryOne = newFieldDefinition()
                        .name(namingConvention.getGet(entity))
                        .type(objectType)
                        .dataFetcher(getReadingFetcher(entity, GET))

                requiresIdentityArguments.add(queryOne)
                queryFields.add(queryOne)
            }

            if (mapping.operations.list) {

                GraphQLFieldDefinition.Builder queryAll = newFieldDefinition()
                        .name(namingConvention.getList(entity))
                        .type(list(objectType))
                        .dataFetcher(getReadingFetcher(entity, LIST))

                queryFields.add(queryAll)

                for (Map.Entry<String, GraphQLInputType> argument: listArguments) {
                    queryAll.argument(newArgument()
                            .name(argument.key)
                            .type(argument.value))
                }
            }

            if (mapping.operations.create && !Modifier.isAbstract(entity.javaClass.modifiers)) {
                GraphQLInputType createObjectType = typeManager.getMutationType(entity, GraphQLPropertyType.CREATE, true)

                GraphQLFieldDefinition.Builder create = newFieldDefinition()
                        .name(namingConvention.getCreate(entity))
                        .type(objectType)
                        .argument(newArgument()
                        .name(entity.decapitalizedName)
                        .type(createObjectType))
                        .dataFetcher(getBindingFetcher(entity, CREATE, dataFetcherManager, dataBinderManager))

                mutationFields.add(create)
            }

            if (mapping.operations.update) {

                GraphQLInputType updateObjectType = typeManager.getMutationType(entity, GraphQLPropertyType.UPDATE, true)

                GraphQLFieldDefinition.Builder update = newFieldDefinition()
                        .name(namingConvention.getUpdate(entity))
                        .type(objectType)
                        .dataFetcher(getBindingFetcher(entity, UPDATE, dataFetcherManager, dataBinderManager))

                postIdentityExecutables.add {
                    update.argument(newArgument()
                            .name(entity.decapitalizedName)
                            .type(updateObjectType))
                }

                requiresIdentityArguments.add(update)
                mutationFields.add(update)
            }

            if (mapping.operations.delete) {

                GraphQLFieldDefinition.Builder delete = newFieldDefinition()
                        .name(namingConvention.getDelete(entity))
                        .type(deleteResponseHandler.objectType)
                        .dataFetcher(getDeletingFetcher(entity, dataFetcherManager, deleteResponseHandler))

                requiresIdentityArguments.add(delete)
                mutationFields.add(delete)
            }

            populateIdentityArguments(entity, requiresIdentityArguments.toArray(new GraphQLFieldDefinition.Builder[0]))

            for (Closure c: postIdentityExecutables) {
                c.call()
            }

            for (CustomOperation operation: mapping.customQueryOperations) {
                queryFields.add(operation.createField(typeManager, mappingContext))
            }

            for (CustomOperation operation: mapping.customMutationOperations) {
                mutationFields.add(operation.createField(typeManager, mappingContext))
            }

            queryType.fields(queryFields*.build())

            mutationType.fields(mutationFields*.build())
        }

        GraphQLSchema.newSchema()
            .query(queryType)
            .mutation(mutationType)
            .build()
    }

}
