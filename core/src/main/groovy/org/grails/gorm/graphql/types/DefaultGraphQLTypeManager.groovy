package org.grails.gorm.graphql.types

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLInputObjectField.newInputObjectField
import static graphql.schema.GraphQLInputObjectType.newInputObject
import static graphql.schema.GraphQLObjectType.newObject
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLType
import graphql.Scalars
import org.grails.gorm.graphql.types.scalars.GraphQLFloat
import org.grails.gorm.graphql.types.scalars.GraphQLURL
import org.grails.gorm.graphql.types.scalars.GraphQLUUID
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.gorm.graphql.GraphQL
import org.grails.gorm.graphql.GraphQLEntityHelper
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager
import org.grails.gorm.graphql.fetcher.impl.ClosureDataFetcher
import org.grails.gorm.graphql.response.errors.GraphQLErrorsResponseHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * The default implementation of {@link GraphQLTypeManager}
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DefaultGraphQLTypeManager implements GraphQLTypeManager {

    protected static final Map<Class, Class> PRIMITIVE_BOXES = [:]

    protected static final Map<Class, GraphQLType> TYPE_MAP = new ConcurrentHashMap<>()

    protected static final Map<Class, GraphQLEnumType> ENUM_TYPES = new ConcurrentHashMap<>()

    static {
        PRIMITIVE_BOXES.with {
            put(int, Integer)
            put(long, Long)
            put(double, Double)
            put(float, Float)
            put(boolean, Boolean)
            put(char, Character)
            put(byte, Byte)
            put(short, Short)
        }

        TYPE_MAP.with {
            put(Integer, Scalars.GraphQLInt)
            put(Long, Scalars.GraphQLLong)
            put(Short, Scalars.GraphQLShort)
            put(Byte, Scalars.GraphQLByte)
            put(Double, Scalars.GraphQLFloat)
            put(Float, new GraphQLFloat())
            put(BigInteger, Scalars.GraphQLBigInteger)
            put(BigDecimal, Scalars.GraphQLBigDecimal)
            put(String, Scalars.GraphQLString)
            put(Boolean, Scalars.GraphQLBoolean)
            put(Character, Scalars.GraphQLChar)
            put(UUID, new GraphQLUUID())
            put(URL, new GraphQLURL())
        }

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
    GraphQLDomainPropertyManager propertyManager

    DefaultGraphQLTypeManager(GraphQLEntityNamingConvention namingConvention, GraphQLErrorsResponseHandler errorsResponseHandler, GraphQLDomainPropertyManager propertyManager) {
        this.namingConvention = namingConvention
        this.propertyManager = propertyManager
        this.errorsResponseHandler = errorsResponseHandler
    }

    @Override
    GraphQLType getType(Class clazz, boolean nullable = true) {
        if (clazz.isPrimitive()) {
            clazz = boxPrimitive(clazz)
        }
        GraphQLType type = TYPE_MAP.get(clazz)
        if (nullable) {
            type
        }
        else {
            GraphQLNonNull.nonNull(type)
        }
    }

    protected Class boxPrimitive(Class clazz) {
        PRIMITIVE_BOXES.get(clazz)
    }

    @Override
    void registerType(Class clazz, GraphQLType type) {
        TYPE_MAP.put(clazz, type)
    }

    @Override
    GraphQLType getEnumType(Class<? extends Enum> clazz, boolean nullable) {
        GraphQLEnumType enumType

        if (ENUM_TYPES.containsKey(clazz)) {
            enumType = ENUM_TYPES.get(clazz)
        }
        else {
            GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
                    .name(clazz.simpleName)

            GraphQL annotation = clazz.getAnnotation(GraphQL)

            if (annotation?.value()) {
                builder.description(annotation.value())
            }

            for (Enum anEnum: clazz.enumConstants) {
                builder.value(anEnum.name(), anEnum)
            }

            enumType = builder.build()
            ENUM_TYPES.put(clazz, enumType)
        }

        if (nullable) {
            enumType
        }
        else {
            GraphQLNonNull.nonNull(enumType)
        }
    }

    @Override
    GraphQLType createReference(PersistentEntity entity, GraphQLPropertyType type) {
        final String REF_NAME = namingConvention.getType(entity, type)
        if (type == GraphQLPropertyType.OUTPUT) {
            GraphQLObjectType.reference(REF_NAME)
        }
        else {
            GraphQLInputObjectType.reference(REF_NAME)
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

    @Override
    GraphQLObjectType getQueryType(PersistentEntity entity) {

        if (!domainObjectTypes.containsKey(entity)) {

            final String DESCRIPTION = GraphQLEntityHelper.getDescription(entity)

            List<GraphQLDomainProperty> properties = propertyManager.builder().getProperties(entity)

            GraphQLObjectType.Builder obj = newObject()
                    .name(namingConvention.getType(entity, GraphQLPropertyType.OUTPUT))
                    .description(DESCRIPTION)

            for (GraphQLDomainProperty prop: properties) {
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

    private GraphQLInputObjectType buildInputObjectType(PersistentEntity entity, GraphQLDomainPropertyManager.Builder builder, GraphQLPropertyType type) {

        final String DESCRIPTION = GraphQLEntityHelper.getDescription(entity)

        List<GraphQLDomainProperty> properties = builder.getProperties(entity)

        GraphQLInputObjectType.Builder inputObj = newInputObject()
                .name(namingConvention.getType(entity, type))
                .description(DESCRIPTION)

        for (GraphQLDomainProperty prop: properties) {
            if (prop.input) {
                inputObj.field(buildInputField(prop, type))
            }
        }

        inputObj.build()
    }

    @Override
    GraphQLInputObjectType getMutationType(PersistentEntity entity, GraphQLPropertyType type) {
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
                throw new IllegalArgumentException("Invalid type specified. ${type.name()} is not a valid mutation type")
        }
    }

    GraphQLInputObjectType getCreateObjectType(PersistentEntity entity) {

        if (!domainCreateObjectTypes.containsKey(entity)) {

            GraphQLDomainPropertyManager.Builder builder = propertyManager.builder()
                .excludeTimestamps()
                .excludeVersion()
                .excludeIdentifiers()

            GraphQLInputObjectType inputObj = buildInputObjectType(entity, builder, GraphQLPropertyType.CREATE)

            domainCreateObjectTypes.put(entity, inputObj)
        }

        domainCreateObjectTypes.get(entity)
    }

    GraphQLInputObjectType getUpdateObjectType(PersistentEntity entity) {
        if (!domainUpdateObjectTypes.containsKey(entity)) {

            GraphQLDomainPropertyManager.Builder builder = propertyManager.builder()
                    .excludeTimestamps()
                    .excludeIdentifiers()

            GraphQLInputObjectType inputObj = buildInputObjectType(entity, builder, GraphQLPropertyType.UPDATE)

            domainUpdateObjectTypes.put(entity, inputObj)
        }

        domainUpdateObjectTypes.get(entity)
    }

    GraphQLInputObjectType getInputNestedObjectType(PersistentEntity entity) {
        if (!domainInputNestedObjectType.containsKey(entity)) {

            GraphQLDomainPropertyManager.Builder builder = propertyManager.builder()
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

            GraphQLInputObjectType inputObj = buildInputObjectType(entity, builder, GraphQLPropertyType.INPUT_NESTED)

            domainInputNestedObjectType.put(entity, inputObj)
        }

        domainInputNestedObjectType.get(entity)
    }
}
