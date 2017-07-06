package org.grails.gorm.graphql

import graphql.Scalars
import graphql.schema.*
import org.grails.datastore.mapping.config.Entity
import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.model.IllegalMappingException
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Basic
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.springframework.context.MessageSource
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.PersistentGraphQLProperty
import org.grails.gorm.graphql.errors.DefaultGraphQLErrorsOutputHandler
import org.grails.gorm.graphql.errors.GraphQLErrorsOutputHandler
import org.grails.gorm.graphql.fetcher.*
import org.grails.gorm.graphql.grails.GrailsGraphQLDataBinder
import org.grails.gorm.graphql.types.DefaultGraphQLTypeManager
import org.grails.gorm.graphql.types.GraphQLTypeManager

import java.lang.reflect.Field
import java.lang.reflect.Method

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLInputObjectField.newInputObjectField
import static graphql.schema.GraphQLInputObjectType.newInputObject
import static graphql.schema.GraphQLList.list
import static graphql.schema.GraphQLObjectType.newObject

/**
 * Created by jameskleeh on 5/19/17.
 */
class Schema {

    protected MappingContext mappingContext
    protected MessageSource messageSource
    protected GraphQLErrorsOutputHandler errorsOutputHandler
    protected GraphQLTypeManager typeManager
    Map<PersistentEntity, GraphQLMapping> mappedEntities = [:]

    Schema(MappingContext mappingContext, MessageSource messageSource) {
        this.mappingContext = mappingContext
        this.messageSource = messageSource
        errorsOutputHandler = new DefaultGraphQLErrorsOutputHandler(messageSource)
        typeManager = new DefaultGraphQLTypeManager()
    }

    private static Method derivedMethod

    static {
        try {
            derivedMethod = Property.class.getMethod("isDerived", (Class<?>[]) null)
        } catch (NoSuchMethodException | SecurityException e) {
            // no-op
        }
    }

    Map<PersistentEntity, GraphQLObjectType> domainObjectTypes = [:]
    Map<PersistentEntity, GraphQLInputObjectType> domainInputObjectTypes = [:]
    Map<Class, GraphQLEnumType> enumTypes = [:]

    GraphQLEnumType buildEnumType(Class clazz) {
        if (!enumTypes.containsKey(clazz)) {
            enumTypes.put(clazz, typeManager.buildEnumType(clazz))
        }
        enumTypes.get(clazz)
    }

    private GraphQLType buildType(PersistentProperty prop) {

        boolean nullable = prop.owner.isIdentityName(prop.name) || prop.mapping.mappedForm.nullable

        if (prop instanceof Association) {
            GraphQLType type
            if (prop instanceof Basic) {
                Class componentType = prop.componentType
                if (mappingContext.mappingFactory.isSimpleType(componentType.name)) {
                    type = typeManager.getType(componentType)
                } else if (componentType.enum) {
                    type = buildEnumType(componentType)
                } else {
                    throw new RuntimeException("Unsure of how to handle type definition of basic association ${prop.toString()}. Not a simple type or enum.")
                }
            } else {
                type = GraphQLObjectType.reference(((Association)prop).associatedEntity.javaClass.simpleName)
            }
            list(type)
        } else {
            typeManager.getType(prop.type, nullable)
        }
    }

    private GraphQLInputObjectField.Builder buildInputField(GraphQLDomainProperty prop) {
        newInputObjectField()
            .name(prop.name)
            .description(prop.description)
            .type((GraphQLInputType)prop.getGraphQLType(typeManager, GraphQLPropertyType.INPUT))
    }

    private GraphQLFieldDefinition.Builder buildField(GraphQLDomainProperty prop) {
        newFieldDefinition()
            .name(prop.name)
            .deprecate(prop.deprecationReason)
            .description(prop.description)
            .type((GraphQLOutputType)prop.getGraphQLType(typeManager, GraphQLPropertyType.OUTPUT))
    }

    List<GraphQLDomainProperty> getProperties(PersistentEntity entity, GraphQLMapping mapping, boolean includeIdentifiers = true) {

        List<GraphQLDomainProperty> properties = []

        if (includeIdentifiers) {
            if (entity.compositeIdentity?.length > 0) {
                properties.addAll(entity.compositeIdentity.collect {
                    new PersistentGraphQLProperty(mappingContext, it, false, true)
                })
            } else if (entity.identity != null) {
                properties.add(new PersistentGraphQLProperty(mappingContext, entity.identity, false, true))
            }
        }

        entity.persistentProperties.each { PersistentProperty prop ->
            if (!mapping.excluded.contains(prop.name)) {
                if (prop instanceof Embedded) {
                    properties.addAll(
                        getProperties(prop.associatedEntity,
                                      mapping.createEmbeddedMapping(prop.name), false))
                } else {
                    boolean input = true
                    if (derivedMethod != null) {
                        Property property = prop.mapping.mappedForm
                        if (derivedMethod.invoke(property, (Object[]) null)) {
                            input = false
                        }
                    }
                    properties.add(new PersistentGraphQLProperty(mappingContext, prop, input, true))
                }
            }
        }

        properties.addAll(mapping.additional)

        properties
    }

    void handleAssociatedEntity(PersistentProperty property) {
        if (property instanceof Association && !property.basic) {
            PersistentEntity associatedEntity = property.associatedEntity
            if (!mappedEntities.containsKey(associatedEntity)) {
                List<GraphQLDomainProperty> associatedProperties = getProperties(associatedEntity, new GraphQLMapping())
                buildObjectType(associatedEntity, associatedProperties)
                buildInputObjectType(associatedEntity, associatedProperties)
            }
        }
    }

    GraphQLObjectType buildObjectType(PersistentEntity entity, List<GraphQLDomainProperty> properties) {

        if (!domainObjectTypes.containsKey(entity)) {
            Class clazz = entity.javaClass

            GraphQLObjectType.Builder obj = newObject().name(clazz.simpleName)

            setMetadata(obj, clazz, entity.mapping.mappedForm)

            properties.each { GraphQLDomainProperty prop ->
                if (prop.output) {
                    obj.field(buildField(prop))
                    if (prop instanceof PersistentGraphQLProperty) {
                        handleAssociatedEntity(prop.property)
                    }
                }
            }

            obj.field(errorsOutputHandler.fieldDefinition)

            domainObjectTypes.put(entity, obj.build())
        }

        domainObjectTypes.get(entity)
    }

    GraphQLInputObjectType buildInputObjectType(PersistentEntity entity, List<GraphQLDomainProperty> properties) {

        if (!domainInputObjectTypes.containsKey(entity)) {
            Class clazz = entity.javaClass

            GraphQLInputObjectType.Builder inputObj = newInputObject().name(clazz.simpleName + "Input")

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

    void populateIdentityArguments(GraphQLFieldDefinition.Builder builder, PersistentEntity entity) {
        if (entity.identity != null) {
            builder.argument(newArgument()
                .name(entity.identity.name)
                .type((GraphQLInputType)typeManager.getType(entity.identity.type, false)))
        } else if (entity.compositeIdentity != null) {
            entity.compositeIdentity.each { PersistentProperty prop ->
                builder.argument(newArgument()
                        .name(prop.name)
                        .type((GraphQLInputType)typeManager.getType(prop.type, false)))
            }
        }
    }

    GraphQLMapping getMapping(PersistentEntity entity) {
        def graphql = ClassPropertyFetcher.getStaticPropertyValue(entity.javaClass, 'graphql', Object)
        if (graphql != null) {
            if (graphql == Boolean.TRUE) {
                return new GraphQLMapping()
            } else if (graphql instanceof Closure) {
                return new GraphQLMapping().build(graphql)
            }

            if (!(graphql instanceof GraphQLMapping)) {
                throw new IllegalMappingException("The static graphql property on ${entity.name} is not a Boolean, Closure, or GraphQLMapping")
            }
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
            List<GraphQLDomainProperty> properties = getProperties(entity, mapping)
            buildObjectType(entity, properties)
            buildInputObjectType(entity, properties)
        }

        GraphQLObjectType.Builder queryType = newObject().name("Query")
        GraphQLObjectType.Builder mutationType = newObject().name("Mutation")
        GraphQLObjectType result = newObject()
                .name("Result")
                .description("Whether or not the operation was successful")
                .field(newFieldDefinition()
                    .name("success")
                    .type(Scalars.GraphQLBoolean)
                    .build()
                ).build()
        GraphQLDataBinder dataBinder = new GrailsGraphQLDataBinder()


        mappedEntities.keySet().each { PersistentEntity entity ->

            GraphQLObjectType objectType = domainObjectTypes.get(entity)
            GraphQLInputObjectType inputObjectType = domainInputObjectTypes.get(entity)

            def queryOne = newFieldDefinition()
                    .name(entity.decapitalizedName)
                    .type(objectType)
                    .dataFetcher(new SingleEntityDataFetcher<>(entity))

            populateIdentityArguments(queryOne, entity)

            def queryAll = newFieldDefinition()
                    .name(entity.decapitalizedName + "List")
                    .type(list(objectType))
                    .dataFetcher(new EntityDataFetcher<>(entity))

            EntityDataFetcher.ARGUMENTS.each { String name, GraphQLScalarType argType ->
                queryAll.argument(newArgument()
                        .name(name)
                        .type(argType)
                        .defaultValue(null))
            }

            def create = newFieldDefinition()
                    .name(entity.decapitalizedName + "Create")
                    .type(domainObjectTypes.get(entity))
                    .argument(newArgument()
                        .name(entity.decapitalizedName)
                        .type(inputObjectType))
                    .dataFetcher(new CreateEntityDataFetcher<>(entity, dataBinder))

            def delete = newFieldDefinition()
                    .name(entity.decapitalizedName + "Delete")
                    .type(result)
                    .dataFetcher(new DeleteEntityDataFetcher<>(entity))

            populateIdentityArguments(delete, entity)

            def updateType = new GraphQLInputObjectType(inputObjectType.name + "Update", inputObjectType.description, inputObjectType.fields.collect {
                GraphQLInputType unwrappedType
                if (it.type instanceof GraphQLNonNull) {
                    unwrappedType = (GraphQLInputType)((GraphQLNonNull)it.type).wrappedType
                } else {
                    unwrappedType = it.type
                }
                new GraphQLInputObjectField(it.name, it.description, unwrappedType, it.defaultValue)
            })

            def update = newFieldDefinition()
                    .name(entity.decapitalizedName + "Update")
                    .type(domainObjectTypes.get(entity))
                    .argument(newArgument()
                        .name(entity.decapitalizedName)
                        .type(updateType))
                    .dataFetcher(new UpdateEntityDataFetcher<>(entity, dataBinder))

            populateIdentityArguments(update, entity)

            queryType
                .field(queryOne)
                .field(queryAll)

            mutationType
                .field(create)
                .field(delete)
                .field(update)
        }


        return GraphQLSchema.newSchema()
                .query(queryType)
                .mutation(mutationType)
                .build()
    }

    private String getDescription(Class clazz, Entity mapping) {
        String comment = null

        GraphQL graphQL = clazz.getAnnotation(GraphQL)
        if (graphQL != null) {
            comment = graphQL.value()
        } else {
            try {
                Class hibernateMapping = Class.forName( "org.grails.orm.hibernate.cfg.Mapping" )
                if (hibernateMapping.isAssignableFrom(mapping.class)) {
                    comment = hibernateMapping.getMethod('getComment').invoke(mapping)
                }
            } catch(ClassNotFoundException e) {}
        }

        comment
    }

    private void setMetadata(GraphQLObjectType.Builder builder, Class clazz, Entity mapping) {
        String comment = getDescription(clazz, mapping)

        if (comment) {
            builder.description(comment)
        }
    }

    private boolean fieldIsDeprecated(Field field) {
        field.getAnnotation(Deprecated) != null
    }

    private boolean classIsDeprecated(Class clazz) {
        clazz.getAnnotation(Deprecated) != null
    }

    private void setMetadata(GraphQLFieldDefinition.Builder builder, Class clazz, String name) {
        try {
            def field = clazz.getField(name)
            if (field != null) {

                GraphQL graphQL = field.getAnnotation(GraphQL)
                if (graphQL != null) {
                    builder.description(graphQL.value())

                    if (graphQL.deprecationReason() || graphQL.deprecated() || fieldIsDeprecated(field)) {
                        builder.deprecate(graphQL.deprecationReason())
                    } else {
                        graphQL = clazz.getAnnotation(GraphQL)
                        if (graphQL?.deprecationReason() || graphQL?.deprecated() || classIsDeprecated(clazz)) {
                            builder.deprecate(graphQL?.deprecationReason() ?: "")
                        }
                    }
                }
            }
        } catch (NoSuchFieldException e) {}
    }


}
