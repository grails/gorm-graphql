package org.grails.gorm.graphql.errors

import graphql.schema.GraphQLFieldDefinition

interface GraphQLErrorsOutputHandler {

    GraphQLFieldDefinition getFieldDefinition()

}