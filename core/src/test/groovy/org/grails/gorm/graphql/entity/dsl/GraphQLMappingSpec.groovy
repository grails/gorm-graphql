package org.grails.gorm.graphql.entity.dsl

import org.grails.gorm.graphql.entity.property.impl.AdditionalGraphQLProperty
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
            add(AdditionalGraphQLProperty.newProperty().name('barFoo').type(Long))
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
        mapping.add(AdditionalGraphQLProperty.newProperty())

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

    void "test create embedded mapping"() {
        given:
        GraphQLMapping mapping = GraphQLMapping.build {
            exclude('foo.bar', 'x', 'foo.y')
            deprecated true
        }

        expect:
        mapping.excluded.size() == 3
        mapping.excluded.contains('foo.bar')
        mapping.excluded.contains('x')
        mapping.excluded.contains('foo.y')
        mapping.deprecated

        when:
        mapping = mapping.createEmbeddedMapping('foo')

        then: 'A new mapping is created with only excluded properties starting with "foo"'
        mapping.excluded.size() == 2
        mapping.excluded.contains('bar')
        mapping.excluded.contains('y')
        !mapping.deprecated
    }
}
