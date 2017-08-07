package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Coercion class for floating points. Always returns a BigDecimal
 *
 * @param <T> The type of the property
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class FloatingNumberCoercion<T> implements Coercing<BigDecimal, T> {

    @Override
    T serialize(Object input) {
        if (input instanceof T) {
            (T) input
        }
        else {
            null
        }
    }

    @Override
    BigDecimal parseValue(Object input) {
        if (input instanceof Float) {
            new BigDecimal((Float) input)
        }
        else if (input instanceof Double) {
            new BigDecimal((Double) input)
        }
        else if (input instanceof BigDecimal) {
            (BigDecimal) input
        }
    }

    @Override
    BigDecimal parseLiteral(Object input) {
        if (input instanceof FloatValue) {
            ((FloatValue) input).value
        }
        else if (input instanceof IntValue) {
            new BigDecimal(((IntValue) input).value)
        }
        else {
            null
        }
    }
}
