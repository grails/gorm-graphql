package org.grails.gorm.graphql.types

import graphql.Scalars
import graphql.schema.*
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.gorm.graphql.GraphQL
import org.grails.gorm.graphql.GraphQLEntityHelper
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.entity.property.GraphQLDomainPropertyManager
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.fetcher.ClosureDataFetcher
import org.grails.gorm.graphql.response.errors.GraphQLErrorsResponseHandler
import org.grails.gorm.graphql.types.scalars.GraphQLFloat
import org.grails.gorm.graphql.types.scalars.GraphQLURL
import org.grails.gorm.graphql.types.scalars.GraphQLUUID

import java.util.concurrent.ConcurrentHashMap

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLInputObjectField.newInputObjectField
import static graphql.schema.GraphQLInputObjectType.newInputObject
import static graphql.schema.GraphQLObjectType.newObject

@CompileStatic
class DefaultGraphQLTypeManager implements GraphQLTypeManager {

    protected static final Map<Class, Class> primitiveBoxes = [:]

    protected static final ConcurrentHashMap<Class, GraphQLType> typeMap = new ConcurrentHashMap<>()

    protected static final ConcurrentHashMap<Class, GraphQLEnumType> enumTypes = new ConcurrentHashMap<>()

    static {
        primitiveBoxes.put(int, Integer)
        primitiveBoxes.put(long, Long)
        primitiveBoxes.put(double, Double)
        primitiveBoxes.put(float, Float)
        primitiveBoxes.put(boolean, Boolean)
        primitiveBoxes.put(char, Character)
        primitiveBoxes.put(byte, Byte)
        primitiveBoxes.put(short, Short)

        typeMap.put(Integer, Scalars.GraphQLInt)
        typeMap.put(Long, Scalars.GraphQLLong)
        typeMap.put(Short, Scalars.GraphQLShort)
        typeMap.put(Byte, Scalars.GraphQLByte)
        typeMap.put(Double, Scalars.GraphQLFloat)
        typeMap.put(Float, new GraphQLFloat())
        typeMap.put(BigInteger, Scalars.GraphQLBigInteger)
        typeMap.put(BigDecimal, Scalars.GraphQLBigDecimal)
        typeMap.put(String, Scalars.GraphQLString)
        typeMap.put(Boolean, Scalars.GraphQLBoolean)
        typeMap.put(Character, Scalars.GraphQLChar)
        typeMap.put(UUID, new GraphQLUUID())
        typeMap.put(URL, new GraphQLURL())

        /*       java.util.Date.class.getName(),
            Time.class.getName(),
            Timestamp.class.getName(),
            java.sql.Date.class.getName(),
            java.util.Currency.class.getName(),
            TimeZone.class.getName(),

            byte[].class.getName(),
            Byte[].class.getName(),
            char[].class.getName(),
            Character[].class.getName(),
            Blob.class.getName(),
            Clob.class.getName(),
            URI.class.getName(),   */

    }

    GraphQLEntityNamingConvention namingConvention
    GraphQLErrorsResponseHandler errorsResponseHandler

    DefaultGraphQLTypeManager(GraphQLEntityNamingConvention namingConvention) {
        this.namingConvention = namingConvention
    }

    DefaultGraphQLTypeManager(GraphQLEntityNamingConvention namingConvention, GraphQLErrorsResponseHandler errorsResponseHandler) {
        this(namingConvention)
        this.errorsResponseHandler = errorsResponseHandler
    }

    @Override
    GraphQLType getType(Class clazz, boolean nullable = true) {
        if (clazz.isPrimitive()) {
            clazz = boxPrimitive(clazz)
        }
        GraphQLType type = typeMap.get(clazz)
        if (!nullable) {
            GraphQLNonNull.nonNull(type)
        }
        else {
            type
        }
    }

    protected Class boxPrimitive(Class clazz) {
        primitiveBoxes.get(clazz)
    }

    @Override
    void registerType(Class clazz, GraphQLType type) {
        typeMap.put(clazz, type)
    }

    @Override
    GraphQLType getEnumType(Class clazz, boolean nullable) {
        GraphQLEnumType enumType

        if (!enumTypes.containsKey(clazz)) {
            GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
                    .name(clazz.simpleName)

            GraphQL annotation = clazz.getAnnotation(GraphQL)

            if (annotation != null && annotation.value()) {
                builder.description(annotation.value())
            }

            clazz.enumConstants.each { Enum anEnum ->
                builder.value(anEnum.name(), anEnum)
            }

            enumType = builder.build()
            enumTypes.put(clazz, enumType)
        }
        else {
            enumType = enumTypes.get(clazz)
        }

        if (!nullable) {
            GraphQLNonNull.nonNull(enumType)
        }
        else {
            enumType
        }
    }

    @Override
    GraphQLType createReference(PersistentEntity entity, GraphQLPropertyType type) {
        final String referenceName = namingConvention.getType(entity, type)
        if (type == GraphQLPropertyType.OUTPUT) {
            GraphQLObjectType.reference(referenceName)
        }
        else {
            GraphQLInputObjectType.reference(referenceName)
        }
    }

    protected Map<PersistentEntity, GraphQLObjectType> domainObjectTypes = [:]
    protected Map<PersistentEntity, GraphQLInputObjectType> domainCreateObjectTypes = [:]
    protected Map<PersistentEntity, GraphQLInputObjectType> domainUpdateObjectTypes = [:]
    protected Map<PersistentEntity, GraphQLInputObjectType> domainInputNestedObjectType = [:]

    protected GraphQLInputObjectField.Builder buildInputField(GraphQLDomainProperty prop, GraphQLPropertyType type) {
        newInputObjectField()
                .name(prop.name)
                .description(prop.description)
                .type((GraphQLInputType)prop.getGraphQLType(this, type))
    }

    protected GraphQLFieldDefinition.Builder buildField(GraphQLDomainProperty prop) {
        newFieldDefinition()
                .name(prop.name)
                .deprecate(prop.deprecationReason)
                .description(prop.description)
                .dataFetcher(prop.dataFetcher ? new ClosureDataFetcher(prop.dataFetcher) : null)
                .type((GraphQLOutputType)prop.getGraphQLType(this, GraphQLPropertyType.OUTPUT))
    }

    GraphQLObjectType getObjectType(PersistentEntity entity) {

        if (!domainObjectTypes.containsKey(entity)) {

            final String description = GraphQLEntityHelper.getDescription(entity)

            GraphQLDomainPropertyManager manager = new GraphQLDomainPropertyManager(entity)

            List<GraphQLDomainProperty> properties = manager.getProperties()

            GraphQLObjectType.Builder obj = newObject()
                    .name(namingConvention.getType(entity, GraphQLPropertyType.OUTPUT))
                    .description(description)

            properties.each { GraphQLDomainProperty prop ->
                if (prop.output) {
                    obj.field(buildField(prop))
                }
            }

            if (errorsResponseHandler != null) {
                obj.field(errorsResponseHandler.fieldDefinition)
            }

            domainObjectTypes.put(entity, obj.build())
        }

        domainObjectTypes.get(entity)
    }

    private GraphQLInputObjectType buildInputObjectType(PersistentEntity entity, GraphQLDomainPropertyManager manager, GraphQLPropertyType type) {

        final String description = GraphQLEntityHelper.getDescription(entity)

        List<GraphQLDomainProperty> properties = manager.getProperties()

        GraphQLInputObjectType.Builder inputObj = newInputObject()
                .name(namingConvention.getType(entity, type))
                .description(description)

        properties.each { GraphQLDomainProperty prop ->
            if (prop.input) {
                inputObj.field(buildInputField(prop, type))
            }
        }

        inputObj.build()
    }

    GraphQLType getType(PersistentEntity entity, GraphQLPropertyType type) {
        switch (type) {
            case GraphQLPropertyType.CREATE:
                getCreateObjectType(entity)
                break
            case GraphQLPropertyType.UPDATE:
                getUpdateObjectType(entity)
                break
            case GraphQLPropertyType.INPUT_NESTED:
                getInputNestedObjectType(entity)
                break
            default:
                getObjectType(entity)
        }
    }

    GraphQLInputObjectType getCreateObjectType(PersistentEntity entity) {

        if (!domainCreateObjectTypes.containsKey(entity)) {

            GraphQLDomainPropertyManager manager = new GraphQLDomainPropertyManager(entity)
                .excludeTimestamps()
                .excludeVersion()
                .identifiers(false)

            GraphQLInputObjectType inputObj = buildInputObjectType(entity, manager, GraphQLPropertyType.CREATE)

            domainCreateObjectTypes.put(entity, inputObj)
        }

        domainCreateObjectTypes.get(entity)
    }

    GraphQLInputObjectType getUpdateObjectType(PersistentEntity entity) {
        if (!domainUpdateObjectTypes.containsKey(entity)) {

            GraphQLDomainPropertyManager manager = new GraphQLDomainPropertyManager(entity)
                    .excludeTimestamps()
                    .identifiers(false)

            GraphQLInputObjectType inputObj = buildInputObjectType(entity, manager, GraphQLPropertyType.UPDATE)

            domainUpdateObjectTypes.put(entity, inputObj)
        }

        domainUpdateObjectTypes.get(entity)
    }

    GraphQLInputObjectType getInputNestedObjectType(PersistentEntity entity) {
        if (!domainInputNestedObjectType.containsKey(entity)) {

            GraphQLDomainPropertyManager manager = new GraphQLDomainPropertyManager(entity)
                .excludeTimestamps()
                .excludeVersion()
                .condition { PersistentProperty prop ->
                    if (prop instanceof Association) {
                        Association association = (Association)prop
                        association.owningSide || !association.bidirectional
                    } else {
                        true
                    }
                }

            GraphQLInputObjectType inputObj = buildInputObjectType(entity, manager, GraphQLPropertyType.INPUT_NESTED)

            domainInputNestedObjectType.put(entity, inputObj)
        }

        domainInputNestedObjectType.get(entity)
    }
}
