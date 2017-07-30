package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.IntValue
import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

import java.sql.Timestamp

/**
 * Default {@link Timestamp} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class TimestampCoercion implements Coercing<Timestamp, Timestamp> {

    @Override
    Timestamp serialize(Object input) {
        if (input instanceof Timestamp) {
            (Timestamp) input
        }
        else {
            null
        }
    }

    @Override
    Timestamp parseValue(Object input) {
        parseLiteral(input)
    }

    @Override
    Timestamp parseLiteral(Object input) {
        if (input instanceof IntValue) {
            new Timestamp(((IntValue) input).value.longValue())
        }
        else if (input instanceof StringValue) {
            Timestamp.valueOf(((StringValue) input).value)
        }
        else {
            null
        }
    }
}
