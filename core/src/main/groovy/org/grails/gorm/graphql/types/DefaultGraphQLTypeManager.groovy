package org.grails.gorm.graphql.types

import graphql.Scalars
import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.schema.*
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.GraphQL
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by jameskleeh on 7/5/17.
 */
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

        GraphQLScalarType GraphQLFloat = new GraphQLScalarType("Float", "Built-in Float", new Coercing<Float, Float>() {
            @Override
            Float serialize(Object input) {
                if (input instanceof Float) {
                    return (Float) input
                }
                else if (input instanceof Number) {
                    return input.floatValue()
                }
                else if (input instanceof String) {
                    Float.parseFloat(input)
                }
                else {
                    return null
                }
            }

            @Override
            Float parseValue(Object input) {
                return serialize(input)
            }

            @Override
            Float parseLiteral(Object input) {
                if (input instanceof IntValue) {
                    return ((IntValue) input).getValue().floatValue()
                }
                else if (input instanceof FloatValue) {
                    return ((FloatValue) input).getValue().floatValue()
                }
                else {
                    return null
                }
            }
        })

        typeMap.put(Integer, Scalars.GraphQLInt)
        typeMap.put(Long, Scalars.GraphQLLong)
        typeMap.put(Short, Scalars.GraphQLShort)
        typeMap.put(Byte, Scalars.GraphQLByte)
        typeMap.put(Double, Scalars.GraphQLFloat)
        typeMap.put(Float, GraphQLFloat)
        typeMap.put(BigInteger, Scalars.GraphQLBigInteger)
        typeMap.put(BigDecimal, Scalars.GraphQLBigDecimal)
        typeMap.put(String, Scalars.GraphQLString)
        typeMap.put(Boolean, Scalars.GraphQLBoolean)
        typeMap.put(Character, Scalars.GraphQLChar)

        /*       java.util.Date.class.getName(),
            Time.class.getName(),
            Timestamp.class.getName(),
            java.sql.Date.class.getName(),
            java.util.Currency.class.getName(),
            TimeZone.class.getName(),

            Class.class.getName(),
            byte[].class.getName(),
            Byte[].class.getName(),
            char[].class.getName(),
            Character[].class.getName(),
            Blob.class.getName(),
            Clob.class.getName(),
            Serializable.class.getName(),
            URI.class.getName(),
            URL.class.getName(),
            UUID.class.getName(),   */

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

    GraphQLEnumType buildEnumType(Class clazz) {
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

            GraphQLEnumType enumType = builder.build()

            enumTypes.put(clazz, enumType)

            enumType
        }
        else {
            enumTypes.get(clazz)
        }
    }

    GraphQLType getReference(PersistentEntity entity, GraphQLPropertyType type) {
        final String referenceName = entity.javaClass.simpleName
        if (type == GraphQLPropertyType.INPUT) {
            GraphQLInputObjectType.reference(referenceName + "Input")
        }
        else {
            GraphQLObjectType.reference(referenceName)
        }
    }
}
