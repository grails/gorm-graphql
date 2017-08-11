package org.grails.gorm.graphql.entity.operations

import graphql.schema.*
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.entity.dsl.helpers.Deprecatable
import org.grails.gorm.graphql.entity.dsl.helpers.Describable
import org.grails.gorm.graphql.entity.dsl.helpers.ExecutesClosures
import org.grails.gorm.graphql.entity.dsl.helpers.Named
import org.grails.gorm.graphql.entity.operations.arguments.ComplexArgument
import org.grails.gorm.graphql.entity.operations.arguments.CustomArgument
import org.grails.gorm.graphql.entity.operations.arguments.SimpleArgument
import org.grails.gorm.graphql.fetcher.impl.CustomOperationInterceptorDataFetcher
import org.grails.gorm.graphql.interceptor.manager.GraphQLInterceptorManager
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition

/**
 * This class stores data about custom query operations
 * that users provide in the mapping of the entity.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
abstract class CustomOperation<T> implements Named<T>, Describable<T>, Deprecatable<T>, ExecutesClosures {

    private List<CustomArgument> arguments = []
    DataFetcher dataFetcher

    T dataFetcher(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher
        (T)this
    }

    OperationType operationType

    private void handleArgumentClosure(CustomArgument argument, Closure closure) {
        withDelegate(closure, argument)
        argument.validate()
        arguments.add(argument)
    }

    /**
     * Creates an argument to the operation that is a list of a simple type.
     * The list can not have more than 1 element and that element must be a class.
     *
     * @param name The name of the argument
     * @param type The returnType of the argument
     * @param closure To provide additional data about the argument
     * @return The operation in order to chain method calls
     */
    CustomOperation argument(String name, List<Class> type, @DelegatesTo(value = SimpleArgument, strategy = Closure.DELEGATE_ONLY) Closure closure = null) {
        CustomArgument argument = new SimpleArgument().name(name).returns(type)
        handleArgumentClosure(argument, closure)
        this
    }

    /**
     * Creates an argument to the operation that is of the returnType provided.
     *
     * @param name The name of the argument
     * @param type The returnType of the argument
     * @param closure To provide additional data about the argument
     * @return The operation in order to chain method calls
     */
    CustomOperation argument(String name, Class type, @DelegatesTo(value = SimpleArgument, strategy = Closure.DELEGATE_ONLY) Closure closure = null) {
        CustomArgument argument = new SimpleArgument().name(name).returns(type)
        handleArgumentClosure(argument, closure)
        this
    }

    /**
     * Creates an argument to the operation that is a custom type.
     *
     * @param name The name of the argument
     * @param closure To provide additional data about the argument
     * @return The operation in order to chain method calls
     */
    CustomOperation argument(String name, String typeName, @DelegatesTo(value = ComplexArgument, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        CustomArgument argument = new ComplexArgument().name(name).typeName(typeName)
        handleArgumentClosure(argument, closure)
        this
    }

    protected abstract GraphQLOutputType getType(GraphQLTypeManager typeManager, MappingContext mappingContext)

    void validate() {
        if (name == null) {
            throw new IllegalArgumentException('A name is required for creating custom operations')
        }
        if (dataFetcher == null) {
            throw new IllegalArgumentException('A data fetcher is required for creating custom operations')
        }
    }

    /**
     * Creates the field to be added to the query or mutation returnType in the schema.
     *
     * @param entity The persistent entity the returnType was created for
     * @param typeManager The returnType manager
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
                .deprecate(deprecationReason)
                .dataFetcher(new CustomOperationInterceptorDataFetcher(entity.javaClass, dataFetcher, interceptorManager, operationType))

        if (!arguments.isEmpty()) {
            for (CustomArgument argument: arguments) {
                customQuery.argument(argument.getArgument(typeManager, mappingContext))
            }
        }

        customQuery
    }
}
