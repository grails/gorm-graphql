package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.TimeZoneCoercion

/**
 * Default {@link TimeZone} scalar type
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLTimeZone extends GraphQLScalarType {

    GraphQLTimeZone() {
        super('TimeZone', 'Accepts a string time zone id', new TimeZoneCoercion())
    }
}
