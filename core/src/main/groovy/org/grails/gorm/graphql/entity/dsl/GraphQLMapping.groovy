package org.grails.gorm.graphql.entity.dsl

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.grails.gorm.graphql.entity.property.AdditionalGraphQLProperty

/**
 * Builder to provide GraphQL specific data for a GORM entity
 *
 * Usage:
 * <pre>
 * {@code
 * static graphql = {
 *     exclude 'foo'
 *     add new AdditionalGraphQLProperty().name('bar').type(String)
 *     description 'Business users'
 * }
 * }
 * </pre>
 *
 * The closure can also be passed to {@link GraphQLMapping#build(Closure)} to provide code completion
 *
 * @author James Kleeh
 */
@Builder(builderStrategy = SimpleStrategy, prefix = '', includes = ['deprecated', 'deprecationReason', 'description'])
@CompileStatic
class GraphQLMapping {

    List<AdditionalGraphQLProperty> additional = []
    Set<String> excluded = new HashSet<String>()
    boolean deprecated = false
    String deprecationReason
    String description

    /**
     * Exclude one or more properties from being included in the schema
     *
     * @param properties One or more property names
     */
    void exclude(String... properties) {
        excluded.addAll(properties)
    }

    /**
     * Add a new property to be included in the schema. The property may
     * or may not be backed by an instance method depending on whether or
     * not the property is to be used for output.
     *
     * @param property The property to include
     */
    void add(AdditionalGraphQLProperty property) {
        if (property.name == null || property.type == null) {
            throw new IllegalArgumentException("GraphQL properties must have both a name and type")
        }
        additional.add(property)
    }

    /**
     * Builder to provide code completion
     *
     * @param closure The closure to execute in the context of a mapping
     * @return The mapping instance
     */
    static GraphQLMapping build(@DelegatesTo(value = GraphQLMapping, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        GraphQLMapping mapping = new GraphQLMapping()
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = mapping
        closure.call()
        mapping
    }

    /**
     * Internal use only. Used to populate a mapping to support nested property names.
     * Current use case is embedded properties.
     *
     *  * <pre>
     * {@code
     *
     * Foo foo
     *
     * static embedded = ['foo']
     *
     * static graphql = {
     *     exclude 'foo.bar'
     * }
     * }
     * </pre>
     *
     * @param propertyName The embedded property name
     * @return A new mapping with excluded that doesn't include the parent name
     */
    GraphQLMapping createEmbeddedMapping(String propertyName) {
        final String subName = propertyName + '.'
        Set<String> excluded = new HashSet<>()
        this.excluded.each {
            if (it.startsWith(subName)) {
                excluded.add(it.replace(subName, ''))
            }
        }
        new GraphQLMapping(excluded: excluded)
    }
}
