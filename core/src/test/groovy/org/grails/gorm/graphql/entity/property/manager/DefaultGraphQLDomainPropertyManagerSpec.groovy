package org.grails.gorm.graphql.entity.property.manager

import grails.gorm.annotation.Entity
import org.codehaus.groovy.util.HashCodeHelper
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.gorm.graphql.HibernateSpec
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.orm.hibernate.cfg.HibernateMappingContext
import spock.lang.Shared

class DefaultGraphQLDomainPropertyManagerSpec extends HibernateSpec {

    @Shared HibernateMappingContext mappingContext
    @Shared GraphQLDomainPropertyManager manager

    List<Class> getDomainClasses() { [NormalId, CompositeId, EmbeddedEntity] }

    void setupSpec() {
        mappingContext = hibernateDatastore.mappingContext
        manager = new DefaultGraphQLDomainPropertyManager()
    }

    void "test retrieving domain properties"() {
        when:
        List<GraphQLDomainProperty> properties = manager.builder().getProperties(mappingContext.getPersistentEntity(NormalId.name))

        then:
        //The timestamp, version, identifiers of the embedded properties are ignored
        properties*.name.toSet() == ['id', 'version', 'age', 'embeddedPogo', 'embeddedEntity', 'price', 'taxRate', 'tax'] as Set
        !properties.find { it.name == 'tax' }.input // Derived properties are input false by default
        !properties.find { it.name == 'id' }.nullable
    }

    void "test retrieving domain properties exclude properties"() {
        when:
        List<GraphQLDomainProperty> properties = manager
                .builder()
                .exclude('version', 'age')
                .getProperties(mappingContext.getPersistentEntity(NormalId.name))

        then:
        properties*.name.toSet() == ['id', 'embeddedPogo', 'embeddedEntity', 'price', 'taxRate', 'tax'] as Set
    }

    void "test retrieving domain properties exclude identifers"() {
        when:
        List<GraphQLDomainProperty> properties = manager
                .builder()
                .excludeIdentifiers()
                .getProperties(mappingContext.getPersistentEntity(NormalId.name))

        then:
        properties*.name.toSet() == ['version', 'age', 'embeddedPogo', 'embeddedEntity', 'price', 'taxRate', 'tax'] as Set
    }

    void "test retrieving domain properties exclude version"() {
        when:
        List<GraphQLDomainProperty> properties = manager
                .builder()
                .excludeVersion()
                .getProperties(mappingContext.getPersistentEntity(NormalId.name))

        then:
        properties*.name.toSet() == ['id', 'age', 'embeddedPogo', 'embeddedEntity', 'price', 'taxRate', 'tax'] as Set
    }

    void "test retrieving domain properties always nullable"() {
        when:
        List<GraphQLDomainProperty> properties = manager
                .builder()
                .alwaysNullable()
                .getProperties(mappingContext.getPersistentEntity(NormalId.name))

        then:
        properties*.name.toSet() == ['id', 'version', 'age', 'embeddedPogo', 'embeddedEntity', 'price', 'taxRate', 'tax'] as Set
        properties.findAll { !it.nullable }.empty
    }

    void "test retrieving domain properties with condition"() {
        when:
        List<GraphQLDomainProperty> properties = manager
                .builder()
                .condition { PersistentProperty prop ->
                    prop.name.contains('i')
                }
                .getProperties(mappingContext.getPersistentEntity(NormalId.name))

        then:
        properties*.name.toSet() == ['id', 'version', 'embeddedEntity', 'price'] as Set
    }

    void "test retrieving domain properties with composite id"() {
        when:
        List<GraphQLDomainProperty> properties = manager
                .builder()
                .getProperties(mappingContext.getPersistentEntity(CompositeId.name))

        then: //bar is excluded via the mapping, foo is added
        properties*.name.toSet() == ['title', 'description', 'dateCreated', 'lastUpdated', 'foo'] as Set
    }

    void "test retrieving domain properties excluding timestamps"() {
        when:
        List<GraphQLDomainProperty> properties = manager
                .builder()
                .excludeTimestamps()
                .getProperties(mappingContext.getPersistentEntity(CompositeId.name))

        then: //bar is excluded via the mapping, foo is added
        properties*.name.toSet() == ['title', 'description', 'foo'] as Set
    }

}

@Entity
class NormalId {
    String age
    Embedded embeddedPogo
    EmbeddedEntity embeddedEntity

    Float price
    Float taxRate
    Float tax

    static mapping = {
        tax formula: 'PRICE * TAX_RATE'
    }

    static embedded = ['embeddedEntity', 'embeddedPogo']

    static graphql = true

}

@Entity
class CompositeId implements Serializable {

    String title
    String description

    Long bar

    Date dateCreated
    Date lastUpdated

    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (title) {
            hashCode = HashCodeHelper.updateHash(hashCode, title)
        }
        if (description) {
            hashCode = HashCodeHelper.updateHash(hashCode, description)
        }
        hashCode
    }

    @Override
    boolean equals(Object other) {
        if (other instanceof CompositeId) {
            other.title == title && other.description == description
        }
    }

    static mapping = {
        id composite: ['title', 'description']
        version false
    }

    static graphql = GraphQLMapping.build {
        exclude('bar')
        add('foo', String)
    }
}

class Embedded {
    String name
}

@Entity
class EmbeddedEntity {
    String title

    Date dateCreated
    Date lastUpdated
}
