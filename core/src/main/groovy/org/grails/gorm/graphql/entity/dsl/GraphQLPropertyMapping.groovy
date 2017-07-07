package org.grails.gorm.graphql.entity.dsl

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
@CompileStatic
class GraphQLPropertyMapping {
    boolean input = true
    boolean output = true
    boolean deprecated = false
    String deprecationReason
    String description

    static GraphQLPropertyMapping build(@DelegatesTo(value = GraphQLPropertyMapping, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping()
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = mapping

        try {
            closure.call()
        } finally {
            closure.delegate = null
        }

        mapping
    }
}
