package org.grails.gorm.graphql.types.scalars.coercing

import graphql.language.StringValue
import graphql.schema.Coercing
import groovy.transform.CompileStatic

/**
 * Default {@link Currency} coercion
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class CurrencyCoercion implements Coercing<Currency, Currency> {

    @Override
    Currency serialize(Object input) {
        if (input instanceof Currency) {
            (Currency) input
        }
        else {
            null
        }
    }

    @Override
    Currency parseValue(Object input) {
        parseLiteral(input)
    }

    @Override
    Currency parseLiteral(Object input) {
        if (input instanceof StringValue) {
            Currency.getInstance(((StringValue)input).value)
        }
        else {
            null
        }
    }
}
