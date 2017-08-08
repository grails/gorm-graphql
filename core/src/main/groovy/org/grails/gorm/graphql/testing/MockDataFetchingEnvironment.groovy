package org.grails.gorm.graphql.testing

import graphql.execution.ExecutionId
import graphql.language.Field
import graphql.language.FragmentDefinition
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLType
import groovy.transform.CompileStatic

/**
 * A class to use to provide a mock DataFetchingEnvironment to
 * test custom data fetchers.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class MockDataFetchingEnvironment implements DataFetchingEnvironment {

    Object source
    Object context
    Map<String, Object> arguments = [:]
    List<Field> fields = []
    GraphQLOutputType fieldType
    GraphQLType parentType
    GraphQLSchema graphQLSchema
    Map<String, FragmentDefinition> fragmentsByName
    ExecutionId executionId
    DataFetchingFieldSelectionSet selectionSet

    @Override
    boolean containsArgument(String name) {
        arguments.containsKey(name)
    }

    @Override
    Object getArgument(String name) {
        arguments.get(name)
    }
}
