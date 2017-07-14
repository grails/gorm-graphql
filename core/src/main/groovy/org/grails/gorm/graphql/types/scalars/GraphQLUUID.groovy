package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.UUIDCoercion

@CompileStatic
class GraphQLUUID extends GraphQLScalarType {

    GraphQLUUID() {
        super("UUID", "Built-in UUID", new UUIDCoercion())
    }

}
