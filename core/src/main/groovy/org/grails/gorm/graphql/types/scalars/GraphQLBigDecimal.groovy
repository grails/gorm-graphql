package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.FloatingNumberCoercion

/**
 * Default {@link BigDecimal} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLBigDecimal extends GraphQLScalarType {

    GraphQLBigDecimal() {
        super('BigDecimal', 'Built-in BigDecimal', new FloatingNumberCoercion<BigDecimal>())
    }
}
