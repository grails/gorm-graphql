package org.grails.gorm.graphql.entity.dsl

import static org.grails.gorm.graphql.entity.property.impl.AdditionalGraphQLProperty.newProperty
import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.DataBinder
import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.grails.gorm.graphql.entity.property.impl.AdditionalGraphQLProperty

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
 * //OR: For code completion
 * static graphql = GraphQLMapping.build {
 *     ...
 * }
 * }
 * </pre>
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Builder(builderStrategy = SimpleStrategy, prefix = '', includes = ['deprecated', 'deprecationReason', 'description'])
@CompileStatic
class GraphQLMapping {

    List<AdditionalGraphQLProperty> additional = []
    Map<String, GraphQLPropertyMapping> propertyMappings = [:]
    Set<String> excluded = [] as Set
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
     * not the property is to be used for response.
     *
     * @param property The property to include
     */
    void add(AdditionalGraphQLProperty property) {
        if (property.name == null || property.type == null) {
            throw new IllegalArgumentException("${property.name}: GraphQL properties must have both a name and type")
        }
        additional.add(property)
    }

    /**
     * Add a new property to be included in the schema. The property may
     * or may not be backed by an instance method depending on whether or
     * not the property is to be used for response.
     *
     * @param name The name of property to include
     * @param type The type of property to include
     */
    void add(String name, Class type) {
        add(newProperty().name(name).type(type))
    }

    private void handleAddClosure(AdditionalGraphQLProperty property, Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = property

        try {
            closure.call()
        } finally {
            closure.delegate = null
        }

        add(property)
    }

    /**
     * Add a new property to be included in the schema. The property may
     * or may not be backed by an instance method depending on whether or
     * not the property is to be used for response.
     *
     * @param name The name of property to include
     * @param type The type of property to include
     * @param closure A closure to further configure the property
     */
    void add(String name, Class type, @DelegatesTo(value = AdditionalGraphQLProperty, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        AdditionalGraphQLProperty property = newProperty().name(name).type(type)
        handleAddClosure(property, closure)
    }

    /**
     * Add a new property to be included in the schema. The property may
     * or may not be backed by an instance method depending on whether or
     * not the property is to be used for response.
     *
     * Both name and type must be configured in the provided closure
     *
     * @param closure A closure to configure the property
     */
    void add(@DelegatesTo(value = AdditionalGraphQLProperty, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        AdditionalGraphQLProperty property = newProperty()
        handleAddClosure(property, closure)
    }

    /**
     * Supply metadata about an existing property
     *
     * @param name The property name
     * @param closure The closure to build the metadata
     * @return The property mapping instance
     */
    GraphQLPropertyMapping property(String name, @DelegatesTo(value = GraphQLPropertyMapping, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        GraphQLPropertyMapping mapping = GraphQLPropertyMapping.build(closure)
        propertyMappings.put(name, mapping)
        mapping
    }

    /**
     * Supply metadata about an existing property
     *
     * @param name The property name
     * @param namedArgs The arguments to build the mapping
     * @return The property mapping instance
     */
    GraphQLPropertyMapping property(String name, Map namedArgs) {
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping()
        DataBinder dataBinder = new DataBinder(mapping)
        dataBinder.bind(new MutablePropertyValues(namedArgs))
        propertyMappings.put(name, mapping)
        mapping
    }

    /**
     * Supplies configuration for an existing property
     *
     * Usage:
     *
     * foo {
     *     description "Foo"
     * }
     *
     * foo description: "Foo"
     *
     * //Provides code completion
     * foo GraphQLPropertyMapping.build {
     *     description("Foo")
     * }
     *
     * @see GraphQLPropertyMapping
     */
    @CompileDynamic
    Object methodMissing(String name, Object args) {
        if (args && args.getClass().isArray()) {

            if (args[0] instanceof Closure) {
                property(name, (Closure) args[0])
            }
            else if (args[0] instanceof GraphQLPropertyMapping) {
                propertyMappings.put(name, (GraphQLPropertyMapping) args[0])
            }
            else if (args[0] instanceof Map) {
                property(name, (Map) args[0])
            }
            else {
                throw new MissingMethodException(name, getClass(), args)
            }
        }
        else {
            throw new MissingMethodException(name, getClass(), args)
        }
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

        try {
            closure.call()
        } finally {
            closure.delegate = null
        }

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
        final String SUB_NAME = propertyName + '.'
        Set<String> excluded = [] as Set
        for (String prop: this.excluded) {
            if (prop.startsWith(SUB_NAME)) {
                excluded.add(prop.replace(SUB_NAME, ''))
            }
        }
        new GraphQLMapping(excluded: excluded)
    }
}
