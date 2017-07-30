package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Default {@link String} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class StringCoercion<T> implements Coercing<String, T> {

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
    String parseValue(Object input) {
        parseLiteral(input)
    }

    @Override
    String parseLiteral(Object input) {
        if (input instanceof StringValue) {
            ((StringValue) input).value
        }
        else {
            null
        }
    }
}
