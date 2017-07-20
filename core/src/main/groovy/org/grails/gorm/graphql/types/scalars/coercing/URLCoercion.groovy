package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Default {@link URL} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class URLCoercion implements Coercing<URL, URL> {

    @Override
    URL serialize(Object input) {
        if (input instanceof URL) {
            (URL) input
        }
        else {
            null
        }
    }

    @Override
    URL parseValue(Object input) {
        serialize(input)
    }

    @Override
    URL parseLiteral(Object input) {
        if (input instanceof StringValue) {
            new URL(((StringValue)input).value)
        }
        else {
            null
        }
    }
}
