package org.grails.gorm.graphql.testing

import graphql.cachecontrol.CacheControl
import graphql.execution.ExecutionContext
import graphql.execution.ExecutionId
import graphql.execution.ExecutionStepInfo
import graphql.execution.MergedField
import graphql.execution.directives.QueryDirectives
import graphql.language.Document
import graphql.language.Field
import graphql.language.FragmentDefinition
import graphql.language.OperationDefinition
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLType
import groovy.transform.CompileStatic
import org.dataloader.DataLoader

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
    GraphQLFieldDefinition fieldDefinition
    Object root
    Field field
    ExecutionStepInfo executionStepInfo
    ExecutionContext executionContext

    @Override
    boolean containsArgument(String name) {
        arguments.containsKey(name)
    }

    @Override
    def <T> T getLocalContext() {
        return null
    }

    @Override
    MergedField getMergedField() {
        return null
    }

    @Override
    QueryDirectives getQueryDirectives() {
        return null
    }

    @Override
    def <K, V> DataLoader<K,V> getDataLoader(String dataLoaderName) {
        return null
    }

    @Override
    CacheControl getCacheControl() {
        return null
    }

    @Override
    OperationDefinition getOperationDefinition() {
        return null
    }

    @Override
    Document getDocument() {
        return null
    }

    @Override
    Map<String, Object> getVariables() {
        return null
    }

    @Override
    Object getArgument(String name) {
        arguments.get(name)
    }
}
