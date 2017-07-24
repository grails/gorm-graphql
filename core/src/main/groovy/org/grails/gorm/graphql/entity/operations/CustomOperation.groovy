package org.grails.gorm.graphql.entity.operations

import graphql.schema.*
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.fetcher.impl.CustomOperationInterceptorDataFetcher
import org.grails.gorm.graphql.interceptor.manager.GraphQLInterceptorManager
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager
import org.grails.gorm.graphql.types.TypeNotFoundException

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
/**
 * This class stores data about custom query operations
 * that users provide in the mapping of the entity.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Builder(builderStrategy = SimpleStrategy, prefix = '', includes = ['deprecated', 'deprecationReason', 'description', 'dataFetcher'])
@CompileStatic
class CustomOperation extends ReturnsType<CustomOperation> {

    private List<CustomArgument> arguments = []
    DataFetcher dataFetcher
    boolean deprecated = false
    String deprecationReason
    String description

    private void handleArgumentClosure(CustomArgument argument, Closure closure) {
        if (closure != null) {
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.delegate = argument

            try {
                closure.call()
            } finally {
                closure.delegate = null
            }
        }
        arguments.add(argument)
    }

    /**
     * Creates an argument to the operation of a custom type
     *
     * @param name The name of the argument
     * @param type The type of the argument
     * @param closure To provide additional data about the argument
     * @return The operation in order to chain method calls
     */
    CustomOperation argument(String name, Map<String, Class> type, @DelegatesTo(value = CustomArgument, strategy = Closure.DELEGATE_FIRST) Closure closure = null) {
        CustomArgument argument = new CustomArgument().name(name).type(type)
        handleArgumentClosure(argument, closure)
        this
    }

    /**
     * Creates an argument to the operation that is a list. The list
     * can not have more than 1 element. That element can either be
     * a class or a map.
     *
     * @param name The name of the argument
     * @param type The type of the argument
     * @param closure To provide additional data about the argument
     * @return The operation in order to chain method calls
     */
    CustomOperation argument(String name, List type, @DelegatesTo(value = CustomArgument, strategy = Closure.DELEGATE_FIRST) Closure closure = null) {
        CustomArgument argument = new CustomArgument().name(name).type(type)
        handleArgumentClosure(argument, closure)
        this
    }

    /**
     * Creates an argument to the operation that is of the type provided.
     *
     * @param name The name of the argument
     * @param type The type of the argument
     * @param closure To provide additional data about the argument
     * @return The operation in order to chain method calls
     */
    CustomOperation argument(String name, Class type, @DelegatesTo(value = CustomArgument, strategy = Closure.DELEGATE_FIRST) Closure closure = null) {
        CustomArgument argument = new CustomArgument().name(name).type(type)
        handleArgumentClosure(argument, closure)
        this
    }

    protected GraphQLOutputType resolveType(GraphQLTypeManager typeManager, MappingContext mappingContext, Class clazz) {
        GraphQLOutputType type
        if (typeManager.hasType(clazz)) {
            type = (GraphQLOutputType)typeManager.getType(clazz)
        }
        else {
            PersistentEntity entity = mappingContext.getPersistentEntity(clazz.name)
            if (entity != null) {
                type = typeManager.getQueryType(entity, GraphQLPropertyType.OUTPUT)
            }
            else {
                throw new TypeNotFoundException(clazz)
            }
        }
        type
    }

    /**
     * Builds a custom object type if the supplied return type is a Map
     *
     * @param typeManager The type manager
     * @param mappingContext The mapping context
     * @return The custom type
     */
    GraphQLObjectType buildCustomType(GraphQLTypeManager typeManager, MappingContext mappingContext) {
        GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
                .name(name.capitalize() + 'Custom')

        for (Map.Entry<String, Class> entry: customReturnFields) {
            builder.field(newFieldDefinition()
                    .name(entry.key)
                    .type(resolveType(typeManager, mappingContext, entry.value)))
        }
        builder.build()
    }

    protected GraphQLOutputType getType(GraphQLTypeManager typeManager, MappingContext mappingContext) {
        GraphQLOutputType type
        if (customReturnFields != null) {
            type = buildCustomType(typeManager, mappingContext)
        }
        else {
            type = resolveType(typeManager, mappingContext, returnType)
        }

        if (collection) {
            type = GraphQLList.list(type)
        }
        type
    }

    private void validate() {
        if (name == null) {
            throw new IllegalArgumentException('A name is required for creating custom operations')
        }
        if (dataFetcher == null) {
            throw new IllegalArgumentException('A data fetcher is required for creating custom operations')
        }
        if (returnType == null && customReturnFields == null) {
            throw new IllegalArgumentException('A return type is required for creating custom operations')
        }
    }

    /**
     * Creates the field to be added to the query or mutation type in the schema.
     *
     * @param entity The persistent entity the type was created for
     * @param typeManager The type manager
     * @param interceptorManager The interceptor manager to be used for executing
     * interceptors with the custom data fetcher
     * @param mappingContext The mapping context
     * @return The custom field
     */
    GraphQLFieldDefinition.Builder createField(PersistentEntity entity,
                                               GraphQLTypeManager typeManager,
                                               GraphQLInterceptorManager interceptorManager,
                                               MappingContext mappingContext) {

        validate()

        GraphQLOutputType outputType = getType(typeManager, mappingContext)

        GraphQLFieldDefinition.Builder customQuery = newFieldDefinition()
                .name(name)
                .type(outputType)
                .description(description)
                .deprecate(deprecationReason ?: (deprecated ? 'Deprecated' : null))
                .dataFetcher(new CustomOperationInterceptorDataFetcher(entity.javaClass, dataFetcher, interceptorManager))

        if (!arguments.isEmpty()) {
            for (CustomArgument argument: arguments) {
                customQuery.argument(argument.getArgument(typeManager))
            }
        }

        customQuery
    }
}
