package org.grails.gorm.graphql.entity.dsl

import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.DataBinder

import static org.grails.gorm.graphql.entity.property.AdditionalGraphQLProperty.newProperty
import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
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
    Map<String, GraphQLPropertyMapping> propertyMappings = [:]
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

    void property(@DelegatesTo(value = GraphQLPropertyMapping, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        GraphQLPropertyMapping.build(closure)
    }

    void property(Map namedArgs) {
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping()
        DataBinder dataBinder = new DataBinder(mapping)
        dataBinder.bind(new MutablePropertyValues(namedArgs))
        mapping
    }

    @CompileDynamic
    def methodMissing(String name, Object args) {
        if(args && args.getClass().isArray()) {

            GraphQLPropertyMapping propertyMapping

            if(args[0] instanceof Closure) {
                propertyMapping = property((Closure) args[0])
            }
            else if(args[0] instanceof GraphQLPropertyMapping) {
                propertyMapping = (GraphQLPropertyMapping) args[0]
            }
            else if(args[0] instanceof Map) {
                propertyMapping = property((Map) args[0])
            }
            else {
                throw new MissingMethodException(name, getClass(), args)
            }

            propertyMappings.put(name, propertyMapping)
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
