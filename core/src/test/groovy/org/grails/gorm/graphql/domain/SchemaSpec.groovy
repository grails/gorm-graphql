package org.grails.gorm.graphql.domain

import graphql.schema.*
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.core.DatastoreUtils
import org.grails.gorm.graphql.Schema
import org.grails.gorm.graphql.testing.GraphQLSchemaSpec
import org.grails.orm.hibernate.HibernateDatastore
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

//@Ignore('Until the rest of the basic graphql types are created')
class SchemaSpec extends Specification implements GraphQLSchemaSpec {

    @Shared @AutoCleanup HibernateDatastore hibernateDatastore
    @Shared GraphQLSchema schema
    @Shared GraphQLObjectType queryType
    @Shared GraphQLObjectType mutationType

    void setupSpec() {
        hibernateDatastore = new HibernateDatastore(
                DatastoreUtils.createPropertyResolver(Collections.singletonMap(Settings.SETTING_DB_CREATE, 'create-drop')),
                getClass().classLoader.getPackage('org.grails.gorm.graphql.domain'))

        schema = new Schema(hibernateDatastore.mappingContext).generate()
        queryType = schema.queryType
        mutationType = schema.mutationType
    }

    void notNull(GraphQLType type, GraphQLType expectedType) {
        type instanceof GraphQLNonNull && ((GraphQLNonNull)type).wrappedType == expectedType
    }

    void list(GraphQLType type, GraphQLType expectedType) {
        type instanceof GraphQLList && ((GraphQLList)type).wrappedType == expectedType
    }

    void "test DebugBar"() {
        given:
        GraphQLObjectType type = schema.getType('DebugBar')

        expect:
        type.getFieldDefinition('foo').type == schema.getType('DebugFoo')
        type.getFieldDefinition('circular').type == schema.getType('DebugCircular')
    }

    void "test DebugFoo"() {
        given:
        GraphQLObjectType type = schema.getType('DebugFoo')

        expect:
        unwrap([], type.getFieldDefinition('items').type) == schema.getType('DebugFooItem')
    }

    void "test DebugFooItem"() {
        given:
        GraphQLObjectType type = schema.getType('DebugFooItem')

        expect:
        type.getFieldDefinition('foo').type == schema.getType('DebugFoo')
    }

    void "test DebugCircular"() {
        given:
        GraphQLObjectType type = schema.getType('DebugCircular')

        expect:
        type.getFieldDefinition('otherCircular').type == schema.getType('DebugCircular')
        unwrap([], type.getFieldDefinition('circulars').type) == schema.getType('DebugCircular')
        type.getFieldDefinition('parentCircular').type == schema.getType('DebugCircular')
    }

}
