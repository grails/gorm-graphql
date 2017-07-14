package org.grails.gorm.graphql.types.scalars

import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.types.scalars.coercing.URLCoercion

@CompileStatic
class GraphQLURL extends GraphQLScalarType {

    GraphQLURL() {
        super("URL", "Built-in URL", new URLCoercion())
    }

}