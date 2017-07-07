package org.grails.gorm.graphql.response.errors

import graphql.schema.GraphQLFieldDefinition

interface GraphQLErrorsResponseHandler {

    GraphQLFieldDefinition getFieldDefinition()

}