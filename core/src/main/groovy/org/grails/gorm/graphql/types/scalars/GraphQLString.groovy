package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.StringCoercion

/**
 * Default {@link String} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLString extends GraphQLScalarType {

    GraphQLString() {
        super('String', 'Built-in String', new StringCoercion<String>())
    }
}
