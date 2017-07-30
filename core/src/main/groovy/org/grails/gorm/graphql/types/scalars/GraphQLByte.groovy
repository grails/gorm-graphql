package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.WholeNumberCoercion

/**
 * Default {@link Byte} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLByte extends GraphQLScalarType {

    GraphQLByte() {
        super('Byte', 'Built-in Byte', new WholeNumberCoercion<Byte>())
    }
}
