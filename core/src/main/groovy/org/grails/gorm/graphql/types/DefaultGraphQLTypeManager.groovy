package org.grails.gorm.graphql.types

import graphql.Scalars
import graphql.schema.*
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.reflect.ClassUtils
import org.grails.gorm.graphql.GraphQL
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager
import org.grails.gorm.graphql.response.errors.GraphQLErrorsResponseHandler
import org.grails.gorm.graphql.types.input.*
import org.grails.gorm.graphql.types.output.EmbeddedObjectTypeBuilder
import org.grails.gorm.graphql.types.output.ObjectTypeBuilder
import org.grails.gorm.graphql.types.output.ShowObjectTypeBuilder
import org.grails.gorm.graphql.types.scalars.GraphQLFloat
import org.grails.gorm.graphql.types.scalars.GraphQLURL
import org.grails.gorm.graphql.types.scalars.GraphQLUUID

import java.util.concurrent.ConcurrentHashMap

/**
 * The default implementation of {@link GraphQLTypeManager}
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DefaultGraphQLTypeManager implements GraphQLTypeManager {

    protected static final Map<Class, GraphQLType> TYPE_MAP = new ConcurrentHashMap<>([
        (Integer): Scalars.GraphQLInt,
        (Long): Scalars.GraphQLLong,
        (Short): Scalars.GraphQLShort,
        (Byte): Scalars.GraphQLByte,
        (Double): Scalars.GraphQLFloat,
        (Float): new GraphQLFloat(),
        (BigInteger): Scalars.GraphQLBigInteger,
        (BigDecimal): Scalars.GraphQLBigDecimal,
        (String): Scalars.GraphQLString,
        (Boolean): Scalars.GraphQLBoolean,
        (Character): Scalars.GraphQLChar,
        (UUID): new GraphQLUUID(),
        (URL): new GraphQLURL()
    ])

    protected static final Map<Class, GraphQLEnumType> ENUM_TYPES = new ConcurrentHashMap<>()

    GraphQLEntityNamingConvention namingConvention
    GraphQLErrorsResponseHandler errorsResponseHandler
    GraphQLDomainPropertyManager propertyManager

    Map<GraphQLPropertyType, InputObjectTypeBuilder> inputObjectTypeBuilders = [:]
    Map<GraphQLPropertyType, ObjectTypeBuilder> objectTypeBuilders = [:]

    DefaultGraphQLTypeManager(GraphQLEntityNamingConvention namingConvention, GraphQLErrorsResponseHandler errorsResponseHandler, GraphQLDomainPropertyManager propertyManager) {
        this.namingConvention = namingConvention
        this.propertyManager = propertyManager
        this.errorsResponseHandler = errorsResponseHandler
        initialize()
    }

    void initialize() {
        List<InputObjectTypeBuilder> inputBuilders = []
        GraphQLTypeManager typeManager = this
        inputBuilders.with {
            add(new CreateInputObjectTypeBuilder(propertyManager, typeManager))
            add(new NestedInputObjectTypeBuilder(propertyManager, typeManager, GraphQLPropertyType.CREATE_NESTED))
            add(new NestedInputObjectTypeBuilder(propertyManager, typeManager, GraphQLPropertyType.UPDATE_NESTED))
            add(new UpdateInputObjectTypeBuilder(propertyManager, typeManager))
            add(new EmbeddedInputObjectTypeBuilder(propertyManager, typeManager, GraphQLPropertyType.UPDATE_EMBEDDED))
            add(new EmbeddedInputObjectTypeBuilder(propertyManager, typeManager, GraphQLPropertyType.CREATE_EMBEDDED))
        }

        for (InputObjectTypeBuilder builder: inputBuilders) {
            inputObjectTypeBuilders.put(builder.type, builder)
        }

        List<ObjectTypeBuilder> builders = []
        builders.add(new EmbeddedObjectTypeBuilder(propertyManager, typeManager, null))
        builders.add(new ShowObjectTypeBuilder(propertyManager, typeManager, errorsResponseHandler))

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
        if (type == null) {
            throw new TypeNotFoundException(clazz)
        }
        if (nullable) {
            type
        }
        else {
            GraphQLNonNull.nonNull(type)
        }
    }

    @Override
    boolean hasType(Class clazz) {
        if (clazz.isPrimitive()) {
            clazz = boxPrimitive(clazz)
        }
        TYPE_MAP.containsKey(clazz)
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
        if (type.operationType == GraphQLOperationType.OUTPUT) {
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
            throw new IllegalArgumentException("Invalid returnType specified. ${type.name()} is not a valid query returnType")
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
            throw new IllegalArgumentException("Invalid returnType specified. ${type.name()} is not a valid mutation returnType")
        }
    }

}
