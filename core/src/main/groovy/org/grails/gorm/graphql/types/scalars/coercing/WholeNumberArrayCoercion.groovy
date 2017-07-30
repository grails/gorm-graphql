package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.ArrayValue
import graphql.language.IntValue
import graphql.language.Value
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Coercion class for whole number arrays
 *
 * @param <T> The type of the property
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class WholeNumberArrayCoercion<T> implements Coercing<BigInteger[], T> {

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
    BigInteger[] parseValue(Object input) {
        parseLiteral(input)
    }

    @Override
    BigInteger[] parseLiteral(Object input) {
        if (input instanceof ArrayValue) {
            List<BigInteger> returnList = []
            List<Value> values = ((ArrayValue) input).values
            for (Value value: values) {
                BigInteger result
                if (value instanceof IntValue) {
                    result = ((IntValue) value).value
                }
                else {
                    result = null
                }
                returnList.add(result)
            }
            (BigInteger[])returnList.toArray()
        }
        else {
            null
        }
    }
}
