package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.IntValue
import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

import java.sql.Time

/**
 * Default {@link java.sql.Time} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class TimeCoercion implements Coercing<Time, Time> {

    @Override
    Time serialize(Object input) {
        if (input instanceof Time) {
            (Time) input
        }
        else {
            null
        }
    }

    @Override
    Time parseValue(Object input) {
        serialize(input)
    }

    @Override
    Time parseLiteral(Object input) {
        if (input instanceof IntValue) {
            new Time(((IntValue) input).value.longValue())
        }
        else if (input instanceof StringValue) {
            Time.valueOf(((StringValue) input).value)
        }
        else {
            null
        }
    }
}
