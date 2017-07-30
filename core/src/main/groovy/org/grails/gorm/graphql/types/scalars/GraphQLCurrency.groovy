package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.CurrencyCoercion

/**
 * Default {@link Currency} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLCurrency extends GraphQLScalarType {

    GraphQLCurrency() {
        super('Currency', 'Accepts a string currency code', new CurrencyCoercion())
    }
}
