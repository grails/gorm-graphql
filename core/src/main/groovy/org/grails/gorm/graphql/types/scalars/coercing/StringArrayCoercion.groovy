package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.ArrayValue
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Conversion class for string arrays
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class StringArrayCoercion<T> implements Coercing<String[], T> {

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
    String[] parseValue(Object input) {
        parseLiteral(input)
    }

    @Override
    String[] parseLiteral(Object input) {
        if (input instanceof ArrayValue) {
            List<String> returnList = []
            List<Value> values = ((ArrayValue) input).values
            for (Value value: values) {
                String result
                if (value instanceof StringValue) {
                    result = ((StringValue) value).value
                }
                else {
                    result = null
                }
                returnList.add(result)
            }
            (String[])returnList.toArray()
        }
        else {
            null
        }
    }
}
