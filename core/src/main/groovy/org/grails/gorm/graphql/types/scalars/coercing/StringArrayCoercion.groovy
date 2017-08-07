package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.ArrayValue
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import groovy.transform.CompileStatic

import java.lang.reflect.Array

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
        if (input instanceof Collection) {
            Collection collection = (Collection) input
            String[] strings = new String[collection.size()]
            for (int i = 0; i < strings.length; i++) {
                strings[i] = collection[i].toString()
            }
            strings
        }
        else if (input.class.array) {
            if (input instanceof String[]) {
                (String[]) input
            }
            else {
                String[] strings = new String[Array.getLength(input)]
                for (int i = 0; i < strings.length; i++) {
                    strings[i] = Array.get(input, i).toString()
                }
                strings
            }
        }
        else {
            null
        }
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
