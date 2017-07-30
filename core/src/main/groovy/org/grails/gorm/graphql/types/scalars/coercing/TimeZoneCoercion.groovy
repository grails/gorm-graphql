package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Default {@link TimeZone} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class TimeZoneCoercion implements Coercing<TimeZone, TimeZone> {

    @Override
    TimeZone serialize(Object input) {
        if (input instanceof TimeZone) {
            (TimeZone) input
        }
        else {
            null
        }
    }

    @Override
    TimeZone parseValue(Object input) {
        parseLiteral(input)
    }

    @Override
    TimeZone parseLiteral(Object input) {
        if (input instanceof StringValue) {
            TimeZone.getTimeZone(((StringValue)input).value)
        }
        else {
            null
        }
    }
}
