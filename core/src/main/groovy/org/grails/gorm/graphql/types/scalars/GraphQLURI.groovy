package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.URICoercion

/**
 * Default {@link URI} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLURI extends GraphQLScalarType {

    GraphQLURI() {
        super('URI', 'Accepts a string in the form of a URI', new URICoercion())
    }
}
