package org.grails.gorm.graphql

import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping

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
import org.grails.gorm.graphql.grails.GrailsGraphQLDataBinder
import org.grails.gorm.graphql.types.DefaultGraphQLTypeManager
import org.grails.gorm.graphql.types.GraphQLTypeManager
import java.lang.reflect.Method

/**
 * Created by jameskleeh on 5/19/17.
 */
class Schema {

    protected MappingContext mappingContext
    protected GraphQLErrorsResponseHandler errorsResponseHandler
    protected GraphQLTypeManager typeManager
    protected GraphQLDeleteResponseHandler deleteResponseHandler
    protected GraphQLEntityNamingConvention namingConvention

    protected Map<PersistentEntity, GraphQLMapping> mappedEntities = [:]

    Schema(MappingContext mappingContext, MessageSource messageSource) {
        this(mappingContext)
        errorsResponseHandler = new DefaultGraphQLErrorsResponseHandler(messageSource)
    }

    Schema(MappingContext mappingContext) {
        this.mappingContext = mappingContext
        typeManager = new DefaultGraphQLTypeManager()
        deleteResponseHandler = new DefaultGraphQLDeleteResponseHandler()
        namingConvention = new GraphQLEntityNamingConvention()
    }

    private static Method derivedMethod

    //To support older versions of GORM
    static {
        try {
            derivedMethod = Property.class.getMethod("isDerived", (Class<?>[]) null)
        } catch (NoSuchMethodException | SecurityException e) {
            // no-op
        }
    }

    protected Map<PersistentEntity, GraphQLObjectType> domainObjectTypes = [:]
    protected Map<PersistentEntity, GraphQLInputObjectType> domainInputObjectTypes = [:]

    protected GraphQLInputObjectField.Builder buildInputField(GraphQLDomainProperty prop) {
        newInputObjectField()
            .name(prop.name)
            .description(prop.description)
            .type((GraphQLInputType)prop.getGraphQLType(typeManager, GraphQLPropertyType.INPUT))
    }

    protected GraphQLFieldDefinition.Builder buildField(GraphQLDomainProperty prop) {
        newFieldDefinition()
            .name(prop.name)
            .deprecate(prop.deprecationReason)
            .description(prop.description)
            .type((GraphQLOutputType)prop.getGraphQLType(typeManager, GraphQLPropertyType.OUTPUT))
    }

    protected List<GraphQLDomainProperty> getProperties(PersistentEntity entity, GraphQLMapping mapping, boolean includeIdentifiers = true) {

        List<GraphQLDomainProperty> properties = []

        if (includeIdentifiers) {
            if (entity.identity != null) {
                properties.add(new PersistentGraphQLProperty(mappingContext, entity.identity, mapping.propertyMappings.getOrDefault(entity.identity.name, new GraphQLPropertyMapping(input: false))))
            }
            else if (entity.compositeIdentity?.length > 0) {
                properties.addAll(entity.compositeIdentity.collect {
                    mapping.propertyMappings.getOrDefault(it.name, new GraphQLPropertyMapping())
                    new PersistentGraphQLProperty(mappingContext, it, mapping.propertyMappings.getOrDefault(it.name, new GraphQLPropertyMapping()))
                })
            }
        }

        entity.persistentProperties.each { PersistentProperty prop ->
            if (!mapping.excluded.contains(prop.name)) {
                if (prop instanceof Embedded) {
                    properties.addAll(
                        getProperties(prop.associatedEntity,
                                      mapping.createEmbeddedMapping(prop.name), false))
                }
                else {
                    boolean input = true
                    if (derivedMethod != null) {
                        Property property = prop.mapping.mappedForm
                        if (derivedMethod.invoke(property, (Object[]) null)) {
                            input = false
                        }
                    }

                    properties.add(new PersistentGraphQLProperty(mappingContext, prop, mapping.propertyMappings.getOrDefault(prop.name, new GraphQLPropertyMapping(input: input))))
                }
            }
        }

        properties.addAll(mapping.additional)

        properties
    }

    protected void handleAssociatedEntity(PersistentProperty property) {
        if (property instanceof Association && !property.basic) {
            PersistentEntity associatedEntity = property.associatedEntity
            if (!mappedEntities.containsKey(associatedEntity)) {
                GraphQLMapping associatedMapping = new GraphQLMapping()
                handleEntity(associatedEntity, associatedMapping)
            }
        }
    }

    protected void handleEntity(PersistentEntity entity, GraphQLMapping mapping) {
        List<GraphQLDomainProperty> associatedProperties = getProperties(entity, mapping)
        final String description = getDescription(entity, mapping)
        buildObjectType(entity, associatedProperties, description)
        buildInputObjectType(entity, associatedProperties, description)
    }

    protected GraphQLObjectType buildObjectType(PersistentEntity entity, List<GraphQLDomainProperty> properties, String description) {

        if (!domainObjectTypes.containsKey(entity)) {

            GraphQLObjectType.Builder obj = newObject()
                    .name(namingConvention.getOutputType(entity))
                    .description(description)

            properties.each { GraphQLDomainProperty prop ->
                if (prop.output) {
                    obj.field(buildField(prop))
                    if (prop instanceof PersistentGraphQLProperty) {
                        handleAssociatedEntity(prop.property)
                    }
                }
            }

            obj.field(errorsResponseHandler.fieldDefinition)

            domainObjectTypes.put(entity, obj.build())
        }

        domainObjectTypes.get(entity)
    }

    protected GraphQLInputObjectType buildInputObjectType(PersistentEntity entity, List<GraphQLDomainProperty> properties, String description) {

        if (!domainInputObjectTypes.containsKey(entity)) {

            GraphQLInputObjectType.Builder inputObj = newInputObject()
                    .name(namingConvention.getCreateType(entity))
                    .description(description)

            properties.each { GraphQLDomainProperty prop ->
                if (prop.input) {
                    inputObj.field(buildInputField(prop))
                    if (prop instanceof PersistentGraphQLProperty) {
                        handleAssociatedEntity(prop.property)
                    }
                }
            }

            domainInputObjectTypes.put(entity, inputObj.build())
        }

        domainInputObjectTypes.get(entity)
    }

    protected void populateIdentityArguments(GraphQLFieldDefinition.Builder builder, List<PersistentProperty> identities) {
        identities.each {
            builder.argument(newArgument()
                    .name(it.name)
                    .type((GraphQLInputType)typeManager.getType(it.type, false)))
        }
    }

    protected GraphQLMapping getMapping(PersistentEntity entity) {
        def graphql = ClassPropertyFetcher.getStaticPropertyValue(entity.javaClass, 'graphql', Object)
        if (graphql != null) {
            if (graphql == Boolean.TRUE) {
                return new GraphQLMapping()
            }
            else if (graphql instanceof Closure) {
                return new GraphQLMapping().build(graphql)
            }

            if (!(graphql instanceof GraphQLMapping)) {
                throw new IllegalMappingException("The static graphql property on ${entity.name} is not a Boolean, Closure, or GraphQLMapping")
            }

            return graphql
        }
        null
    }

    GraphQLSchema generate() {

        mappingContext.persistentEntities.each { PersistentEntity entity ->
            GraphQLMapping mapping = getMapping(entity)
            if (mapping != null) {
                mappedEntities.put(entity, mapping)
            }
        }

        mappedEntities.each { PersistentEntity entity, GraphQLMapping mapping ->
            handleEntity(entity, mapping)
        }

        GraphQLObjectType.Builder queryType = newObject().name("Query")
        GraphQLObjectType.Builder mutationType = newObject().name("Mutation")

        GraphQLDataBinder dataBinder = new GrailsGraphQLDataBinder()

        mappedEntities.keySet().each { PersistentEntity entity ->

            List<PersistentProperty> identities = []
            if (entity.identity != null) {
                identities.add(entity.identity)
            }
            else if (entity.compositeIdentity != null) {
                identities.addAll(entity.compositeIdentity)
            }
            identities

            GraphQLObjectType objectType = domainObjectTypes.get(entity)

            def queryOne = newFieldDefinition()
                    .name(namingConvention.getReadSingle(entity))
                    .type(objectType)
                    .dataFetcher(new SingleEntityDataFetcher<>(entity))

            populateIdentityArguments(queryOne, identities)

            def queryAll = newFieldDefinition()
                    .name(namingConvention.getReadMany(entity))
                    .type(list(objectType))
                    .dataFetcher(new EntityDataFetcher<>(entity))

            EntityDataFetcher.ARGUMENTS.each { String name, GraphQLScalarType argType ->
                queryAll.argument(newArgument()
                        .name(name)
                        .type(argType)
                        .defaultValue(null))
            }

            GraphQLInputObjectType inputObjectType = domainInputObjectTypes.get(entity)

            def create = newFieldDefinition()
                    .name(namingConvention.getCreate(entity))
                    .type(domainObjectTypes.get(entity))
                    .argument(newArgument()
                        .name(entity.decapitalizedName)
                        .type(inputObjectType))
                    .dataFetcher(new CreateEntityDataFetcher<>(entity, dataBinder))

            def delete = newFieldDefinition()
                    .name(namingConvention.getDelete(entity))
                    .type(deleteResponseHandler.objectType)
                    .dataFetcher(new DeleteEntityDataFetcher<>(entity))

            populateIdentityArguments(delete, identities)

            def updateType = new GraphQLInputObjectType(namingConvention.getUpdateType(entity), inputObjectType.description, inputObjectType.fields.collect {
                GraphQLInputType unwrappedType
                if (it.type instanceof GraphQLNonNull) {
                    unwrappedType = (GraphQLInputType)((GraphQLNonNull)it.type).wrappedType
                }
                else {
                    unwrappedType = it.type
                }
                new GraphQLInputObjectField(it.name, it.description, unwrappedType, it.defaultValue)
            })

            def update = newFieldDefinition()
                    .name(namingConvention.getUpdate(entity))
                    .type(domainObjectTypes.get(entity))
                    .argument(newArgument()
                        .name(entity.decapitalizedName)
                        .type(updateType))
                    .dataFetcher(new UpdateEntityDataFetcher<>(entity, dataBinder))

            populateIdentityArguments(update, identities)

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

    protected String getDescription(PersistentEntity entity, GraphQLMapping graphQLMapping) {
        String description = graphQLMapping.description

        if (description == null) {
            GraphQL graphQL = entity.javaClass.getAnnotation(GraphQL)
            if (graphQL != null) {
                description = graphQL.value()
            }
            else {
                try {
                    Class hibernateMapping = Class.forName( "org.grails.orm.hibernate.cfg.Mapping" )
                    Entity mapping = entity.mapping.mappedForm
                    if (hibernateMapping.isAssignableFrom(mapping.class)) {
                        description = hibernateMapping.getMethod('getComment').invoke(mapping)
                    }
                } catch(ClassNotFoundException e) {}
            }
        }

        description
    }

}
