package org.grails.gorm.graphql.types.scalars

import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic

/**
 * A class to store scalar types for GORM simple types
 *
 * @author James Kleeh
 */
@CompileStatic
class GormScalars {

    public static GraphQLScalarType GraphQLFloat = new GraphQLScalarType("Float", "Built-in Float", new Coercing<Float, Float>() {
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

    public static GraphQLScalarType GraphQLUUID = new GraphQLScalarType("UUID", "Built-in UUID", new Coercing<UUID, UUID>() {
        @Override
        UUID serialize(Object input) {
            if (input instanceof UUID) {
                return (UUID) input
            }
            else if (input instanceof String) {
                return UUID.fromString(input)
            }
            else if (input instanceof byte[]) {
                return UUID.nameUUIDFromBytes(input)
            }
            else {
                return null
            }
        }

        @Override
        UUID parseValue(Object input) {
            return serialize(input)
        }

        @Override
        UUID parseLiteral(Object input) {
            if (input instanceof String) {
                return UUID.fromString(input)
            }
            else {
                return null
            }
        }
    })

}
