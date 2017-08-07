package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.IntValue
import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

import java.sql.Date

/**
 * Default {@link java.sql.Date} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class SqlDateCoercion implements Coercing<Date, Date> {

    @Override
    Date serialize(Object input) {
        if (input instanceof Date) {
            (Date) input
        }
        else {
            null
        }
    }

    @Override
    Date parseValue(Object input) {
        serialize(input)
    }

    @Override
    Date parseLiteral(Object input) {
        if (input instanceof IntValue) {
            new Date(((IntValue) input).value.longValue())
        }
        else if (input instanceof StringValue) {
            Date.valueOf(((StringValue) input).value)
        }
        else {
            null
        }
    }
}
