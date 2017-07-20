package org.grails.gorm.graphql.entity.property.impl

import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.PropertyMapping
import org.grails.datastore.mapping.model.types.ToMany
import org.grails.gorm.graphql.GraphQL
import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping
import org.grails.gorm.graphql.entity.property.impl.PersistentGraphQLProperty
import spock.lang.Specification

class PersistentGraphQLPropertySpec extends Specification {

    @SuppressWarnings('UnnecessaryGetter')
    PersistentProperty buildProperty(String name, boolean identityName = false, boolean nullable = true, boolean association = false) {
        Stub(association ? ToMany : PersistentProperty) {
            getName() >> name
            getType() >> String
            getOwner() >> Mock(PersistentEntity) {
                getIdentity() >> Mock(PersistentProperty) {
                    getName() >> {
                        identityName ? name : 'sdflksjdf'
                    }
                }
                getJavaClass() >> Test
            }
            getMapping() >> Mock(PropertyMapping) {
                getMappedForm() >> Mock(Property) {
                    getNullable() >> nullable
                }
            }
            if (association) {
                getAssociatedEntity() >> Mock(PersistentEntity) {
                    getJavaClass() >> String
                }
            }
        }
    }

    void "test deprecation with foo property"() {
        given:
        PersistentProperty property = buildProperty('foo')
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping()

        when:
        PersistentGraphQLProperty prop = new PersistentGraphQLProperty(null, property, mapping)

        then:
        prop.description == 'Foo'
        prop.deprecated
        prop.deprecationReason == 'Deprecated'
    }

    void "test deprecation with bar property"() {
        given:
        PersistentProperty property = buildProperty('bar')
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping()

        when:
        PersistentGraphQLProperty prop = new PersistentGraphQLProperty(null, property, mapping)

        then:
        prop.description == 'Bar'
        prop.deprecated
        prop.deprecationReason == 'Bar is deprecated'
    }

    void "test deprecation with fooBar property"() {
        given:
        PersistentProperty property = buildProperty('fooBar')
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping(description: 'Foo Bar')

        when:
        PersistentGraphQLProperty prop = new PersistentGraphQLProperty(null, property, mapping)

        then:
        prop.description == 'Foo Bar'
        prop.deprecated
        prop.deprecationReason == 'Deprecated'
    }

    void "test deprecation with barFoo property"() {
        given:
        PersistentProperty property = buildProperty('barFoo')
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping(deprecated: true)

        when:
        PersistentGraphQLProperty prop = new PersistentGraphQLProperty(null, property, mapping)

        then:
        prop.description == 'Bar Foo'
        prop.deprecated
        prop.deprecationReason == 'Deprecated'
    }

    void "test nullable with mapping"() {
        given:
        PersistentProperty property = buildProperty('foo', true)
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping(nullable: false)

        when:
        PersistentGraphQLProperty prop = new PersistentGraphQLProperty(null, property, mapping)

        then:
        !prop.nullable
    }

    void "test nullable with mapped form"() {
        given:
        PersistentProperty property = buildProperty('foo', false, false)
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping()

        when:
        PersistentGraphQLProperty prop = new PersistentGraphQLProperty(null, property, mapping)

        then:
        !prop.nullable
    }

    void "test nullable mapping overrides mapped form"() {
        given:
        PersistentProperty property = buildProperty('foo', false, false)
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping(nullable: true)

        when:
        PersistentGraphQLProperty prop = new PersistentGraphQLProperty(null, property, mapping)

        then:
        prop.nullable
    }

    void "test collection if association"() {
        given:
        PersistentProperty property = buildProperty('foo', false, true, true)
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping()

        when:
        PersistentGraphQLProperty prop = new PersistentGraphQLProperty(null, property, mapping)

        then:
        prop.collection
    }

    void "test description in mapping overrides annotation"() {
        given:
        PersistentProperty property = buildProperty('foo')
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping(description: 'Foo from mapping')

        when:
        PersistentGraphQLProperty prop = new PersistentGraphQLProperty(null, property, mapping)

        then:
        prop.description == 'Foo from mapping'
    }

    void "test deprecationReason in mapping overrides annotation"() {
        given:
        PersistentProperty property = buildProperty('bar')
        GraphQLPropertyMapping mapping = new GraphQLPropertyMapping(deprecationReason: 'Bar deprecated from mapping')

        when:
        PersistentGraphQLProperty prop = new PersistentGraphQLProperty(null, property, mapping)

        then:
        prop.deprecationReason == 'Bar deprecated from mapping'
    }

    @SuppressWarnings('JUnitPublicProperty')
    class Test {

        @GraphQL(value = 'Foo', deprecated = true)
        String foo

        @GraphQL(value = 'Bar', deprecationReason = 'Bar is deprecated')
        String bar

        @Deprecated
        String fooBar

        @GraphQL(value = 'Bar Foo')
        String barFoo

    }
}
