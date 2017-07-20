package org.grails.gorm.graphql.types

import org.grails.datastore.mapping.reflect.ClassUtils
import org.grails.gorm.graphql.types.input.CreateInputObjectTypeBuilder
import org.grails.gorm.graphql.types.input.EmbeddedInputObjectTypeBuilder
import org.grails.gorm.graphql.types.input.InputObjectTypeBuilder
import org.grails.gorm.graphql.types.input.NestedInputObjectTypeBuilder
import org.grails.gorm.graphql.types.input.UpdateInputObjectTypeBuilder
import org.grails.gorm.graphql.types.output.EmbeddedObjectTypeBuilder
import org.grails.gorm.graphql.types.output.ObjectTypeBuilder
import org.grails.gorm.graphql.types.output.ShowObjectTypeBuilder

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

    protected static final Map<Class, GraphQLType> TYPE_MAP = new ConcurrentHashMap<>()

    protected static final Map<Class, GraphQLEnumType> ENUM_TYPES = new ConcurrentHashMap<>()

    static {

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

        /*
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

    Map<GraphQLPropertyType, InputObjectTypeBuilder> inputObjectTypeBuilders = [:]
    Map<GraphQLPropertyType, ObjectTypeBuilder> objectTypeBuilders = [:]

    DefaultGraphQLTypeManager(GraphQLEntityNamingConvention namingConvention, GraphQLErrorsResponseHandler errorsResponseHandler, GraphQLDomainPropertyManager propertyManager) {
        this.namingConvention = namingConvention
        this.propertyManager = propertyManager
        this.errorsResponseHandler = errorsResponseHandler

        List<InputObjectTypeBuilder> inputBuilders = []
        inputBuilders.add(new CreateInputObjectTypeBuilder(propertyManager, this))
        inputBuilders.add(new NestedInputObjectTypeBuilder(propertyManager, this, GraphQLPropertyType.CREATE_NESTED))
        inputBuilders.add(new NestedInputObjectTypeBuilder(propertyManager, this, GraphQLPropertyType.UPDATE_NESTED))
        inputBuilders.add(new UpdateInputObjectTypeBuilder(propertyManager, this))
        inputBuilders.add(new EmbeddedInputObjectTypeBuilder(propertyManager, this, true))
        inputBuilders.add(new EmbeddedInputObjectTypeBuilder(propertyManager, this, false))

        for (InputObjectTypeBuilder builder: inputBuilders) {
            inputObjectTypeBuilders.put(builder.type, builder)
        }

        List<ObjectTypeBuilder> builders = []
        builders.add(new EmbeddedObjectTypeBuilder(propertyManager, this, null))
        builders.add(new ShowObjectTypeBuilder(propertyManager, this, errorsResponseHandler))

        for (ObjectTypeBuilder builder: builders) {
            objectTypeBuilders.put(builder.type, builder)
        }
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
        ClassUtils.PRIMITIVE_TYPE_COMPATIBLE_CLASSES.get(clazz)
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

    @Override
    GraphQLOutputType getQueryType(PersistentEntity entity, GraphQLPropertyType type) {
        if (objectTypeBuilders.containsKey(type)) {
            objectTypeBuilders.get(type).build(entity)
        }
        else {
            throw new IllegalArgumentException("Invalid type specified. ${type.name()} is not a valid query type")
        }
    }

    @Override
    GraphQLInputType getMutationType(PersistentEntity entity, GraphQLPropertyType type, boolean nullable) {
        if (inputObjectTypeBuilders.containsKey(type)) {
            GraphQLInputType inputType = inputObjectTypeBuilders.get(type).build(entity)
            if (nullable) {
                inputType
            }
            else {
                GraphQLNonNull.nonNull(inputType)
            }
        }
        else {
            throw new IllegalArgumentException("Invalid type specified. ${type.name()} is not a valid mutation type")
        }
    }

}
