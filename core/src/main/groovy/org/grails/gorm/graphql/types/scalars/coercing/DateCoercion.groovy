package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.IntValue
import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat

/**
 * Default {@link Date} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DateCoercion implements Coercing<Date, Date> {

    protected List<String> formats
    protected boolean lenient

    DateCoercion(List<String> dateFormats, boolean lenient) {
        this.formats = dateFormats
        this.lenient = lenient
    }

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
            parseDate(((StringValue) input).value)
        }
        else {
            null
        }
    }

    protected Date parseDate(String value) {
        Date dateValue
        if (!value || !formats) {
            return null
        }
        Exception firstException
        for (String format: formats) {
            if (dateValue == null) {
                DateFormat formatter = new SimpleDateFormat(format)
                try {
                    formatter.lenient = lenient
                    dateValue = formatter.parse((String)value)
                } catch (ParseException e) {
                    firstException = firstException ?: e
                }
            }
        }
        if (dateValue == null && firstException) {
            throw firstException
        }
        dateValue
    }

}
