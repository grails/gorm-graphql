package org.grails.gorm.graphql.entity.dsl

import graphql.Scalars
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.property.impl.CustomGraphQLProperty
import org.grails.gorm.graphql.entity.property.manager.DefaultGraphQLDomainPropertyManager
import org.grails.gorm.graphql.fetcher.impl.CustomOperationInterceptorDataFetcher
import org.grails.gorm.graphql.interceptor.manager.DefaultGraphQLInterceptorManager
import org.grails.gorm.graphql.interceptor.manager.GraphQLInterceptorManager
import org.grails.gorm.graphql.types.DefaultGraphQLTypeManager
import org.grails.gorm.graphql.types.GraphQLTypeManager
import spock.lang.Specification

class GraphQLMappingSpec extends Specification {

    void "test exclude"() {
        given:
        GraphQLMapping mapping = GraphQLMapping.build {
            exclude('foo')
            exclude('bar')
        }

        expect:
        mapping.excluded.contains('foo')
        mapping.excluded.contains('bar')
    }

    void "test add"() {
        given:
        GraphQLMapping mapping = GraphQLMapping.build {
            add {
                name 'fooBar'
                type Integer
                description 'Foo Bar'
            }
            add('foo', String)
            add(CustomGraphQLProperty.newProperty().name('barFoo').type(Long))
            add('bar', String) {
                deprecationReason 'Deprecated'
            }
        }

        expect:
        mapping.additional.size() == 4
        mapping.additional[0].name == 'fooBar'
        mapping.additional[0].type == Integer
        mapping.additional[0].description == 'Foo Bar'
        mapping.additional[1].name == 'foo'
        mapping.additional[1].type == String
        mapping.additional[2].name == 'barFoo'
        mapping.additional[2].type == Long
        mapping.additional[3].name == 'bar'
        mapping.additional[3].type == String
        mapping.additional[3].deprecationReason == 'Deprecated'

        when:
        mapping.add { }

        then:
        thrown(IllegalArgumentException)

        when:
        mapping.add(CustomGraphQLProperty.newProperty())

        then:
        Exception ex = thrown(IllegalArgumentException)
        ex.message == 'null: GraphQL properties must have both a name and type'
    }

    void "test modify existing property"() {
        when:
        GraphQLMapping mapping = GraphQLMapping.build {
            foo {
                description 'Foo'
            }
            bar deprecated: true
            fooBar GraphQLPropertyMapping.build {
                dataFetcher {

                }
            }
        }

        then:
        mapping.propertyMappings.size() == 3
        mapping.propertyMappings.get('foo').description == 'Foo'
        mapping.propertyMappings.get('bar').deprecated
        mapping.propertyMappings.get('fooBar').dataFetcher != null
    }

    void "test modify existing property with property method"() {
        when:
        GraphQLMapping mapping = GraphQLMapping.build {
            property('foo') {
                description 'Foo'
            }
            property('bar', [deprecated: true])
            property('fooBar', GraphQLPropertyMapping.build {
                dataFetcher {

                }
            })
        }

        then:
        mapping.propertyMappings.size() == 3
        mapping.propertyMappings.get('foo').description == 'Foo'
        mapping.propertyMappings.get('bar').deprecated
        mapping.propertyMappings.get('fooBar').dataFetcher != null
    }

    void "test adding an operation" () {
        when:
        GraphQLTypeManager typeManager = new DefaultGraphQLTypeManager(new GraphQLEntityNamingConvention(), null, new DefaultGraphQLDomainPropertyManager())
        GraphQLInterceptorManager interceptorManager = new DefaultGraphQLInterceptorManager()
        PersistentEntity entity = Stub(PersistentEntity) {
            getJavaClass() >> GraphQLMappingSpec
        }

        DataFetcher defaultFetcher = new DataFetcher() {
            @Override
            Object get(DataFetchingEnvironment environment) {
                return null
            }
        }

        GraphQLMapping mapping = GraphQLMapping.build {
            query('foo') {
                argument('bar', String) {
                    description('Bar argument')
                    defaultValue 'b'
                    nullable false
                }
                type(BigDecimal)
                dataFetcher(defaultFetcher)
                description('Foo Query')
                deprecationReason('Foo Query is deprecated')
            }

            query('bar') {
                argument('foo', [String])
                type([BigDecimal])
                dataFetcher(defaultFetcher)
                description('Bar Query')
                deprecated(true)
            }

            mutation('xyz') {
                argument('fooBar', [foo: Integer])
                type([bar: String])
                dataFetcher(defaultFetcher)
                description('ZYX mutation')
            }
        }
        GraphQLFieldDefinition foo = mapping.customQueryOperations.find { it.name == 'foo' }.createField(entity, typeManager, interceptorManager, null).build()
        GraphQLFieldDefinition bar = mapping.customQueryOperations.find { it.name == 'bar' }.createField(entity, typeManager, interceptorManager, null).build()
        GraphQLFieldDefinition xyz = mapping.customMutationOperations.find { it.name == 'xyz' }.createField(entity, typeManager, interceptorManager, null).build()

        then:
        foo.description == 'Foo Query'
        foo.deprecated
        foo.deprecationReason == 'Foo Query is deprecated'
        foo.type == Scalars.GraphQLBigDecimal
        foo.dataFetcher instanceof CustomOperationInterceptorDataFetcher
        foo.arguments.size() == 1
        foo.getArgument('bar').type instanceof GraphQLNonNull
        ((GraphQLNonNull)foo.getArgument('bar').type).wrappedType == Scalars.GraphQLString
        foo.getArgument('bar').description == 'Bar argument'
        foo.getArgument('bar').defaultValue == 'b'

        bar.description == 'Bar Query'
        bar.deprecated
        bar.deprecationReason == 'Deprecated'
        bar.type instanceof GraphQLList
        ((GraphQLList)bar.type).wrappedType == Scalars.GraphQLBigDecimal
        bar.dataFetcher instanceof CustomOperationInterceptorDataFetcher
        bar.arguments.size() == 1
        bar.getArgument('foo').type instanceof GraphQLList
        bar.getArgument('foo').description == null
        bar.getArgument('foo').defaultValue == null
        ((GraphQLList)bar.getArgument('foo').type).wrappedType == Scalars.GraphQLString

        xyz.description == 'ZYX mutation'
        !xyz.deprecated
        xyz.type instanceof GraphQLObjectType
        ((GraphQLObjectType)xyz.type).fieldDefinitions[0].name == 'bar'
        ((GraphQLObjectType)xyz.type).fieldDefinitions[0].type == Scalars.GraphQLString
        ((GraphQLObjectType)xyz.type).fieldDefinitions.size() == 1
        xyz.dataFetcher instanceof CustomOperationInterceptorDataFetcher
        xyz.arguments.size() == 1
        xyz.arguments[0].type instanceof GraphQLInputObjectType
        xyz.arguments[0].name == 'fooBar'
        ((GraphQLInputObjectType)xyz.arguments[0].type).getField('foo').type == Scalars.GraphQLInt
        ((GraphQLInputObjectType)xyz.arguments[0].type).fieldDefinitions.size() == 1
    }
}
