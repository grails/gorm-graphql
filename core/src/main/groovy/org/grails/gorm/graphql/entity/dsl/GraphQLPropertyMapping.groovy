package org.grails.gorm.graphql.entity.dsl

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

/**
 * Builder to provide GraphQL specific data for a GORM entity property
 *
 * Usage:
 * <pre>
 * {@code
 * static graphql = {
 *     someProperty input: false, description: "foo"
 *     otherProperty {
 *         input false
 *         description "otherFoo"
 *     }
 *     //OR: For code completion
 *     otherProperty GraphQLPropertyMapping.build {
 *
 *     }
 *     //If the property name conflicts with a existing method name ex: "description"
 *     property("description") {
 *         ...
 *     }
 *     property "description", [:]
 * }
 * }
 * </pre>
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Builder(builderStrategy = SimpleStrategy, prefix = '')
@CompileStatic
class GraphQLPropertyMapping {
    boolean input = true
    boolean output = true
    boolean deprecated = false
    Boolean nullable
    String deprecationReason
    String description
    Closure dataFetcher

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
