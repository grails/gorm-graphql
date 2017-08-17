package org.grails.gorm.graphql.domain

import graphql.schema.*
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.core.DatastoreUtils
import org.grails.gorm.graphql.Schema
import org.grails.gorm.graphql.testing.GraphQLSchemaSpec
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.orm.hibernate.HibernateDatastore
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

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

    private String normalizeType(GraphQLPropertyType type) {
        type.name().split('_').collect { String name ->
            name.toLowerCase().capitalize()
        }.join('').replace('Output', '')
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
        type.getFieldDefinition('bar').type == schema.getType('DebugBar')
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

    @Unroll
    void "test !ToOne#type"() {
        expect:
        schema.getType('ToOne' + type) == null

        where:
        type << [GraphQLPropertyType.CREATE_NESTED,
                 GraphQLPropertyType.CREATE_EMBEDDED,
                 GraphQLPropertyType.UPDATE_NESTED,
                 GraphQLPropertyType.UPDATE_EMBEDDED].collect{ normalizeType(it) }
    }

    void "test ToOne"() {
        GraphQLObjectType type = schema.getType('ToOne')

        expect:
        type.getFieldDefinition('circularOne').type == schema.getType('CircularOne')
        type.getFieldDefinition('one').type == schema.getType('One')
        type.getFieldDefinition('anEnum').type == schema.getType('Enum')

        //everything else is a scalar.. not worth testing every property
    }

    void "test ToOneUpdate"() {
        GraphQLInputObjectType type = schema.getType('ToOneUpdate')

        expect:
        type.getFieldDefinition('circularOne').type == schema.getType('CircularOneUpdateNested')
        type.getFieldDefinition('one').type == schema.getType('OneUpdateNested')
        type.getFieldDefinition('anEnum').type == schema.getType('Enum')

        //everything else is a scalar.. not worth testing every property
    }

    void "test ToOneCreate"() {
        GraphQLInputObjectType type = schema.getType('ToOneCreate')

        expect:
        unwrap(null, type.getFieldDefinition('circularOne').type) == schema.getType('CircularOneCreateNested')
        unwrap(null, type.getFieldDefinition('one').type) == schema.getType('OneCreateNested')
        unwrap(null, type.getFieldDefinition('anEnum').type) == schema.getType('Enum')

        //everything else is a scalar.. not worth testing every property
    }

    @Unroll
    void "test !One#type"() {
        expect:
        schema.getType('One' + type) == null

        where:
        type << [GraphQLPropertyType.CREATE,
                 GraphQLPropertyType.CREATE_EMBEDDED,
                 GraphQLPropertyType.UPDATE,
                 GraphQLPropertyType.UPDATE_EMBEDDED].collect{ normalizeType(it) }
    }

    @Unroll
    void "test One#type"() {
        expect:
        schema.getType('One' + type) != null

        where:
        type << [GraphQLPropertyType.OUTPUT,
                 GraphQLPropertyType.CREATE_NESTED,
                 GraphQLPropertyType.UPDATE_NESTED].collect{ normalizeType(it) }

    }

    void "test CircularOneCreateNested"() {
        GraphQLInputObjectType nestedCircular = schema.getType('CircularOneCreateNested')

        expect:
        nestedCircular.getFieldDefinition('one').type == nestedCircular
    }

}
