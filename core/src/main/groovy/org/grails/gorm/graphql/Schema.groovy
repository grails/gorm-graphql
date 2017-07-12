package org.grails.gorm.graphql

import groovy.transform.CompileStatic
import org.grails.gorm.graphql.binding.manager.GraphQLDataBinderManager
import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping
import org.grails.gorm.graphql.fetcher.manager.DefaultGraphQLDataFetcherManager
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLInputObjectField.newInputObjectField
import static graphql.schema.GraphQLInputObjectType.newInputObject
import static graphql.schema.GraphQLList.list
import static graphql.schema.GraphQLObjectType.newObject

import graphql.schema.*
import org.grails.datastore.mapping.config.Entity
import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.model.IllegalMappingException
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.response.delete.DefaultGraphQLDeleteResponseHandler
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler
import org.springframework.context.MessageSource
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.PersistentGraphQLProperty
import org.grails.gorm.graphql.response.errors.DefaultGraphQLErrorsResponseHandler
import org.grails.gorm.graphql.response.errors.GraphQLErrorsResponseHandler
import org.grails.gorm.graphql.fetcher.*
import org.grails.gorm.graphql.types.DefaultGraphQLTypeManager
import org.grails.gorm.graphql.types.GraphQLTypeManager
import java.lang.reflect.Method

/**
 * Created by jameskleeh on 5/19/17.
 */
@CompileStatic
class Schema {

    protected MappingContext mappingContext

    GraphQLErrorsResponseHandler errorsResponseHandler
    GraphQLTypeManager typeManager
    GraphQLDeleteResponseHandler deleteResponseHandler
    GraphQLEntityNamingConvention namingConvention
    GraphQLDataBinderManager dataBinderManager
    GraphQLDataFetcherManager dataFetcherManager


    protected Map<PersistentEntity, GraphQLMapping> mappedEntities = [:]

    Schema(MappingContext mappingContext, MessageSource messageSource) {
        this(mappingContext)
        errorsResponseHandler = new DefaultGraphQLErrorsResponseHandler(messageSource)
    }

    Schema(MappingContext mappingContext) {
        this.mappingContext = mappingContext
        deleteResponseHandler = new DefaultGraphQLDeleteResponseHandler()
        namingConvention = new GraphQLEntityNamingConvention()
        typeManager = new DefaultGraphQLTypeManager(namingConvention)
        dataBinderManager = new GraphQLDataBinderManager()
        dataFetcherManager = new DefaultGraphQLDataFetcherManager()
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
                    .dataFetcher(dataFetcherManager.createGetFetcher(entity))

            def queryAll = newFieldDefinition()
                    .name(namingConvention.getReadMany(entity))
                    .type(list(objectType))
                    .dataFetcher(dataFetcherManager.createListFetcher(entity))

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
                    .dataFetcher(dataFetcherManager.createCreateFetcher(entity, dataBinder))

            def delete = newFieldDefinition()
                    .name(namingConvention.getDelete(entity))
                    .type(deleteResponseHandler.objectType)
                    .dataFetcher(dataFetcherManager.createDeleteFetcher(entity, deleteResponseHandler))

            GraphQLInputObjectType updateObjectType = typeManager.getUpdateObjectType(entity)

            def update = newFieldDefinition()
                    .name(namingConvention.getUpdate(entity))
                    .type(objectType)
                    .argument(newArgument()
                        .name(entity.decapitalizedName)
                        .type(updateObjectType))
                    .dataFetcher(dataFetcherManager.createUpdateFetcher(entity, dataBinder))

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
