package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Default {@link URI} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class URICoercion implements Coercing<URI, URI> {

    @Override
    URI serialize(Object input) {
        if (input instanceof URI) {
            (URI) input
        }
        else {
            null
        }
    }

    @Override
    URI parseValue(Object input) {
        serialize(input)
    }

    @Override
    URI parseLiteral(Object input) {
        if (input instanceof StringValue) {
            new URI(((StringValue)input).value)
        }
        else {
            null
        }
    }
}