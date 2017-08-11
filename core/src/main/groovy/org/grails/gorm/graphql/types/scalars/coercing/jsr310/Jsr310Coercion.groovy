package org.grails.gorm.graphql.types.scalars.coercing.jsr310

import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

import java.time.format.DateTimeParseException

/**
 * Base class for Java 8 date types to extend for GraphQL coercion
 *
 * @param <T> The JSR310 class
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
abstract class Jsr310Coercion<T> implements Coercing<T, T> {

    protected List<String> formats

    Jsr310Coercion(List<String> dateFormats) {
        this.formats = dateFormats
    }

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
    T parseValue(Object input) {
        serialize(input)
    }

    @Override
    T parseLiteral(Object input) {
        if (input instanceof StringValue) {
            convert(((StringValue) input).value)
        }
        else {
            null
        }
    }

    abstract T parse(String value, String format)

    T convert(String value) {
        T dateValue
        if (!value) {
            return null
        }
        Exception firstException
        formats.each { String format ->
            if (dateValue == null) {
                try {
                    dateValue = parse(value, format)
                } catch (DateTimeParseException e) {
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
