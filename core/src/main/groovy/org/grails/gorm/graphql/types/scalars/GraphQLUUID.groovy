package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.UUIDCoercion

/**
 * Default {@link UUID} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLUUID extends GraphQLScalarType {

    GraphQLUUID() {
        super('UUID', 'Accepts a string to be converted to a UUID', new UUIDCoercion())
    }

}
