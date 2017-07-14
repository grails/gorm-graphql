package org.grails.gorm.graphql.types.scalars

import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic

@CompileStatic
class GraphQLDate extends GraphQLScalarType {

    GraphQLDate(Coercing coercing) {
        super("Date", "Built-in Date", coercing)
    }
}
