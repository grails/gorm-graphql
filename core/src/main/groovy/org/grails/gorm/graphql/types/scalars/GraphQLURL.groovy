package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.URLCoercion

/**
 * Default {@link URL} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLURL extends GraphQLScalarType {

    GraphQLURL() {
        super('URL', 'Accepts a string in the form of a URL', new URLCoercion())
    }
}
