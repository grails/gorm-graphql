package org.grails.gorm.graphql.types.output

import graphql.schema.GraphQLCodeRegistry
import org.grails.gorm.graphql.HibernateSpec
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.entity.property.manager.CompositeId
import org.grails.gorm.graphql.entity.property.manager.DefaultGraphQLDomainPropertyManager
import org.grails.gorm.graphql.entity.property.manager.EmbeddedEntity
import org.grails.gorm.graphql.types.GraphQLPropertyType
import spock.lang.Shared

class EmbeddedObjectTypeBuilderSpec extends HibernateSpec {

    List<Class> getDomainClasses() { [
            CompositeId, EmbeddedEntity
    ] }

    @Shared GraphQLCodeRegistry.Builder codeRegistry
    EmbeddedObjectTypeBuilder builder

    void setup() {
        builder = new EmbeddedObjectTypeBuilder(codeRegistry, new DefaultGraphQLDomainPropertyManager(), null, null)
    }

    void "test type"() {
        expect:
        builder.type == GraphQLPropertyType.OUTPUT_EMBEDDED
    }

    void "test property excluded via mapping"() {
        when:
        List<GraphQLDomainProperty> props = builder.builder.getProperties(CompositeId.gormPersistentEntity)

        then:
        !props*.name.contains('bar')
    }

    void "test property added via mapping"() {
        when:
        List<GraphQLDomainProperty> props = builder.builder.getProperties(CompositeId.gormPersistentEntity)

        then:
        props*.name.contains('foo')
    }

    void "test timestamps are included"() {
        when:
        List<GraphQLDomainProperty> props = builder.builder.getProperties(EmbeddedEntity.gormPersistentEntity)

        then:
        props*.name.contains('dateCreated')
        props*.name.contains('lastUpdated')
    }

    void "test version is excluded"() {
        when:
        List<GraphQLDomainProperty> props = builder.builder.getProperties(EmbeddedEntity.gormPersistentEntity)

        then:
        !props*.name.contains('version')
    }

    void "test properties are nullable"() {
        when:
        List<GraphQLDomainProperty> props = builder.builder.getProperties(EmbeddedEntity.gormPersistentEntity)

        then:
        props.find { it.name == 'title' }.nullable
    }

    void "test id is excluded"() {
        when:
        List<GraphQLDomainProperty> props = builder.builder.getProperties(EmbeddedEntity.gormPersistentEntity)

        then:
        !props*.name.contains('id')
    }

    void "test composite ids are excluded"() {
        when:
        List<GraphQLDomainProperty> props = builder.builder.getProperties(CompositeId.gormPersistentEntity)

        then:
        !props*.name.contains('title')
        !props*.name.contains('description')
    }
}
