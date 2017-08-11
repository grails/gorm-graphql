package org.grails.gorm.graphql.types.scalars.coercing.jsr310

import graphql.language.IntValue
import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

import java.time.Instant

/**
 * Default {@link Instant} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@InheritConstructors
@CompileStatic
class InstantCoercion implements Coercing<Instant, Instant> {

    @Override
    Instant serialize(Object input) {
        if (input instanceof Instant) {
            (Instant) input
        }
        else {
            null
        }
    }

    @Override
    Instant parseValue(Object input) {
        serialize(input)
    }

    @Override
    Instant parseLiteral(Object input) {
        if (input instanceof IntValue) {
            Instant.ofEpochSecond(((IntValue) input).value.longValue())
        }
        else if (input instanceof StringValue) {
            Instant.parse(((StringValue) input).value)
        }
        else {
            null
        }
    }

}
