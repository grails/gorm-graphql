package org.grails.gorm.graphql.entity.dsl

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.grails.gorm.graphql.entity.operations.CustomOperation
import org.grails.gorm.graphql.entity.property.impl.CustomGraphQLProperty
import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.DataBinder

import static CustomGraphQLProperty.newProperty

/**
 * DSL to provide GraphQL specific data for a GORM entity
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

    List<CustomGraphQLProperty> additional = []
    Map<String, GraphQLPropertyMapping> propertyMappings = [:]
    Set<String> excluded = [] as Set
    boolean deprecated = false
    String deprecationReason
    String description
    Operations operations = new Operations()
    List<CustomOperation> customQueryOperations = []
    List<CustomOperation> customMutationOperations = []

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
    void add(CustomGraphQLProperty property) {
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

    private void handleAddClosure(CustomGraphQLProperty property, Closure closure) {
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
    void add(String name, Class type, @DelegatesTo(value = CustomGraphQLProperty, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        CustomGraphQLProperty property = newProperty().name(name).type(type)
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
    void add(@DelegatesTo(value = CustomGraphQLProperty, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        CustomGraphQLProperty property = newProperty()
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
        property(name, mapping)
    }

    /**
     * Supply metadata about an existing property
     *
     * Example: property('foo', [input: false])
     *
     * @param name The property name
     * @param namedArgs The arguments to build the mapping
     * @return The property mapping instance
     */
    GraphQLPropertyMapping property(String name, Map namedArgs) {
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping()
        DataBinder dataBinder = new DataBinder(mapping)
        dataBinder.bind(new MutablePropertyValues(namedArgs))
        property(name, mapping)
    }

    /**
     * Supply metadata about an existing property
     *
     * Example: property('foo', input: false)
     *
     * @param name The property name
     * @param namedArgs The arguments to build the mapping
     * @return The property mapping instance
     */
    GraphQLPropertyMapping property(Map namedArgs, String name) {
        property(name, namedArgs)
    }

    /**
     * Supply metadata about an existing property
     *
     * @param name The property name
     * @param mapping The property mapping instance
     * @return The property mapping instance provided
     */
    GraphQLPropertyMapping property(String name, GraphQLPropertyMapping mapping) {
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
     * Builder to provide code completion. The mapping instance will not be evaluated
     * until the schema is being generated
     *
     * @param closure The closure to execute in the context of a mapping
     * @return The mapping instance
     */
    static LazyGraphQLMapping lazy(@DelegatesTo(value = GraphQLMapping, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        new LazyGraphQLMapping(closure)
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
     * Controls whether query or mutation types will be created for the entity
     */
    class Operations {
        boolean get = true
        boolean list = true
        boolean create = true
        boolean update = true
        boolean delete = true

        boolean isOutput() {
            get || list
        }
    }

    private CustomOperation handleCustomOperation(String name, Closure closure) {
        CustomOperation operation = new CustomOperation().name(name)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = operation

        try {
            closure.call()
        } finally {
            closure.delegate = null
        }
        operation
    }

    void query(String name, @DelegatesTo(value = CustomOperation, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        customQueryOperations.add(handleCustomOperation(name, closure))
    }

    void mutation(String name, @DelegatesTo(value = CustomOperation, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        customMutationOperations.add(handleCustomOperation(name, closure))
    }
}
