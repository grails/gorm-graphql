package gorm.graphql

import graphql.ErrorType
import graphql.GraphQLError
import graphql.language.SourceLocation
import groovy.transform.CompileStatic

@CompileStatic
class GraphQLErrorImpl implements GraphQLError {
    String message

    List<SourceLocation> locations = []

    ErrorType errorType
}
