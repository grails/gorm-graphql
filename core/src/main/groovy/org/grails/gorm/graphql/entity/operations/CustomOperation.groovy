package org.grails.gorm.graphql.entity.operations

import graphql.schema.*
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.fetcher.impl.CustomOperationInterceptorDataFetcher
import org.grails.gorm.graphql.interceptor.manager.GraphQLInterceptorManager
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager
import org.grails.gorm.graphql.types.TypeNotFoundException

import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition

/**
 * This class stores data about custom query operations
 * that users provide in the mapping of the entity.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class CustomOperation extends ReturnsType<CustomOperation> {

    private List<CustomArgument> arguments = []
    DataFetcher dataFetcher

    CustomOperation dataFetcher(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher
        this
    }

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

    CustomOperation argument(String name, Map<String, Class> returnType, @DelegatesTo(value = CustomArgument, strategy = Closure.DELEGATE_FIRST) Closure closure = null) {
        CustomArgument argument = new CustomArgument().name(name).type(returnType)
        handleArgumentClosure(argument, closure)
        this
    }

    CustomOperation argument(String name, List returnType, @DelegatesTo(value = CustomArgument, strategy = Closure.DELEGATE_FIRST) Closure closure = null) {
        CustomArgument argument = new CustomArgument().name(name).type(returnType)
        handleArgumentClosure(argument, closure)
        this
    }

    CustomOperation argument(String name, Class returnType, @DelegatesTo(value = CustomArgument, strategy = Closure.DELEGATE_FIRST) Closure closure = null) {
        CustomArgument argument = new CustomArgument().name(name).type(returnType)
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

    protected GraphQLOutputType getType(GraphQLTypeManager typeManager, MappingContext mappingContext) {
        GraphQLOutputType type
        if (customReturnFields != null) {
            GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
                    .name(name.capitalize() + 'Custom')

            for (Map.Entry<String, Class> entry: customReturnFields) {
                builder.field(newFieldDefinition()
                    .name(entry.key)
                    .type(resolveType(typeManager, mappingContext, entry.value)))
            }
            type = builder.build()
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

    GraphQLFieldDefinition.Builder createField(PersistentEntity entity,
                                               GraphQLTypeManager typeManager,
                                               GraphQLInterceptorManager interceptorManager,
                                               MappingContext mappingContext) {

        validate()

        GraphQLOutputType outputType = getType(typeManager, mappingContext)

        GraphQLFieldDefinition.Builder customQuery = newFieldDefinition()
                .name(name)
                .type(outputType)
                .dataFetcher(new CustomOperationInterceptorDataFetcher(entity.javaClass, dataFetcher, interceptorManager))

        if (!arguments.isEmpty()) {
            for (CustomArgument argument: arguments) {
                GraphQLInputType inputType = (GraphQLInputType)typeManager.getType(argument.returnType, argument.nullable)
                customQuery.argument(newArgument()
                        .name(argument.name)
                        .description(argument.description)
                        .defaultValue(argument.defaultValue)
                        .type(inputType))
            }
        }

        customQuery
    }
}
