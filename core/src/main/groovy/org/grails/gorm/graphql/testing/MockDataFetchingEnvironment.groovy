package org.grails.gorm.graphql.testing

import graphql.cachecontrol.CacheControl
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
import org.dataloader.DataLoaderRegistry

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
    Object localContext
    Map<String, Object> arguments = [:]
    List<Field> fields = []
    GraphQLOutputType fieldType
    GraphQLType parentType
    GraphQLSchema graphQLSchema
    Map<String, FragmentDefinition> fragmentsByName
    ExecutionId executionId
    DataLoaderRegistry dataLoaderRegistry
    CacheControl cacheControl
    OperationDefinition operationDefinition
    Locale locale
    DataFetchingFieldSelectionSet selectionSet
    GraphQLFieldDefinition fieldDefinition
    Object root
    MergedField mergedField
    Field field
    ExecutionStepInfo executionStepInfo
    Document document
    Map<String, Object> variables
    QueryDirectives queryDirectives

    @Override
    boolean containsArgument(String name) {
        arguments.containsKey(name)
    }

    @Override
    Object getArgumentOrDefault(String name, Object defaultValue) {
        arguments.getOrDefault(name, defaultValue)
    }

    @Override
    Object getLocalContext() {
        localContext
    }

    @Override
    MergedField getMergedField() {
        MergedField.newMergedField(fields).build()
    }

    @Override
    QueryDirectives getQueryDirectives() {
        queryDirectives
    }

    @Override
    def <K, V> DataLoader<K,V> getDataLoader(String dataLoaderName) {
        dataLoaderRegistry ? dataLoaderRegistry.getDataLoader(dataLoaderName) : null
    }

    @Override
    CacheControl getCacheControl() {
        cacheControl
    }

    @Override
    Locale getLocale() {
        locale
    }

    @Override
    OperationDefinition getOperationDefinition() {
         operationDefinition
    }

    @Override
    Document getDocument() {
        document
    }

    @Override
    Map<String, Object> getVariables() {
        variables
    }

    @Override
    Object getArgument(String name) {
        arguments.get(name)
    }
}
