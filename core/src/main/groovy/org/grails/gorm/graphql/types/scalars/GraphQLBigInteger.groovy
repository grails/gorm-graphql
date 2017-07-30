package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.WholeNumberCoercion

/**
 * Default {@link BigInteger} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLBigInteger extends GraphQLScalarType {

    GraphQLBigInteger() {
        super('BigInteger', 'Built-in BigInteger', new WholeNumberCoercion<BigInteger>())
    }
}
