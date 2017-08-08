package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.IntValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Coercion class for whole numbers
 *
 * @param <T> The type of the property
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class WholeNumberCoercion<T> implements Coercing<BigInteger, T> {

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
    BigInteger parseValue(Object input) {
        if (input instanceof Short) {
            BigInteger.valueOf((Short) input)
        }
        else if (input instanceof Integer) {
            BigInteger.valueOf((Integer) input)
        }
        else if (input instanceof Long) {
            BigInteger.valueOf((Long) input)
        }
        else if (input instanceof BigInteger) {
            (BigInteger) input
        }
        else {
            null
        }
    }

    @Override
    BigInteger parseLiteral(Object input) {
        if (input instanceof IntValue) {
            ((IntValue) input).value
        }
        else {
            null
        }
    }
}
