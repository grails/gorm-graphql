package org.grails.gorm.graphql.entity.property.impl

import static org.grails.gorm.graphql.entity.property.GraphQLPropertyType.*
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLObjectType
import grails.gorm.annotation.Entity
import graphql.Scalars
import graphql.schema.GraphQLList
import graphql.schema.GraphQLType
import org.codehaus.groovy.util.HashCodeHelper
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.gorm.graphql.HibernateSpec
import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping
import org.grails.gorm.graphql.types.GraphQLTypeManager
import org.grails.orm.hibernate.cfg.HibernateMappingContext
import spock.lang.Shared

class HibernatePersistentGraphQLPropertySpec extends HibernateSpec {

    @Shared HibernateMappingContext mappingContext

    List<Class> getDomainClasses() { [Book, Book2, Author, Tag, Metadata, OtherMetadata] }

    void setupSpec() {
        mappingContext = hibernateDatastore.mappingContext
    }

    void "test constructor with simple property (non null)"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('title')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())

        expect:
        property.name == 'title'
        property.type == String
        !property.collection
        !property.nullable
    }

    void "test constructor with simple property (null)"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('description')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())

        expect:
        property.name == 'description'
        property.type == String
        !property.collection
        property.nullable
    }

    void "test constructor with id property"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).identity
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())

        expect:
        property.name == 'id'
        property.type == Long
        !property.collection
        !property.nullable
    }

    void "test constructor with composite id property"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book2.name).getPropertyByName('title')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())

        expect:
        property.name == 'title'
        property.type == String
        !property.collection
        !property.nullable
    }

    void "test constructor with an enum"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('bookType')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())

        expect:
        property.name == 'bookType'
        property.type == BookType
        !property.collection
        !property.nullable
    }

    void "test constructor with a toMany"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('authors')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())

        expect:
        property.name == 'authors'
        property.type == Author
        property.collection
        !property.nullable
    }

    void "test constructor with a simple toMany"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('basics')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())

        expect:
        property.name == 'basics'
        property.type == String
        property.collection
        !property.nullable
    }

    void "test constructor with a toOne"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('metadata')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())

        expect:
        property.name == 'metadata'
        property.type == Metadata
        !property.collection
        !property.nullable
    }

    void "test graphQL type with simple property (non null)"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('title')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        property.getGraphQLType(typeManager, CREATE)

        then:
        1 * typeManager.getType(String, false)
    }

    void "test graphQL type with simple property (null)"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('description')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        property.getGraphQLType(typeManager, CREATE)

        then:
        1 * typeManager.getType(String, true)
    }

    void "test graphQL type with id property"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).identity
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        property.getGraphQLType(typeManager, CREATE)

        then:
        1 * typeManager.getType(Long, false)
    }

    void "test graphQL type with composite id property"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book2.name).getPropertyByName('title')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        property.getGraphQLType(typeManager, CREATE)

        then:
        1 * typeManager.getType(String, false)
    }

    void "test graphQL type with an enum"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('bookType')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        property.getGraphQLType(typeManager, CREATE)

        then:
        1 * typeManager.getEnumType(BookType, false)
    }

    void "test graphQL type with a toMany that is mapped with graphql"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('authors')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        GraphQLType type = property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.createReference(mappingContext.getPersistentEntity(Author.name), OUTPUT) >> Scalars.GraphQLString
        type instanceof GraphQLList
    }

    void "test graphQL type with a toMany that is NOT mapped with graphql"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('tags')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        GraphQLType type = property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.getQueryType(mappingContext.getPersistentEntity(Tag.name)) >> GraphQLObjectType.newObject().name('x').build()
        type instanceof GraphQLList
    }

    void "test graphQL type with a simple toMany"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('basics')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        GraphQLType type = property.getGraphQLType(typeManager, CREATE)

        then:
        1 * typeManager.getType(String, false) >> Scalars.GraphQLString
        type instanceof GraphQLList
    }

    void "test graphQL type with a toOne that is mapped with graphql"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('otherMetadata')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        GraphQLType type = property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.createReference(mappingContext.getPersistentEntity(OtherMetadata.name), OUTPUT) >> Scalars.GraphQLString
        type ==  Scalars.GraphQLString
    }

    void "test graphQL type with a toOne that is mapped with graphql CREATE"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('otherMetadata')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        GraphQLType type = property.getGraphQLType(typeManager, CREATE)

        then:
        1 * typeManager.getMutationType(mappingContext.getPersistentEntity(OtherMetadata.name), INPUT_NESTED) >> GraphQLInputObjectType.newInputObject().name('x').build()
        type instanceof GraphQLInputObjectType
    }

    void "test graphQL type with a toOne that is mapped with graphql UPDATE"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('otherMetadata')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        GraphQLType type = property.getGraphQLType(typeManager, UPDATE)

        then:
        1 * typeManager.getMutationType(mappingContext.getPersistentEntity(OtherMetadata.name), INPUT_NESTED) >> GraphQLInputObjectType.newInputObject().name('x').build()
        type instanceof GraphQLInputObjectType
    }

    void "test graphQL type with a toOne that is NOT mapped with graphql"() {
        PersistentProperty p = mappingContext.getPersistentEntity(Book.name).getPropertyByName('metadata')
        PersistentGraphQLProperty property = new PersistentGraphQLProperty(mappingContext, p, new GraphQLPropertyMapping())
        GraphQLTypeManager typeManager = Mock(GraphQLTypeManager)

        when:
        GraphQLType type = property.getGraphQLType(typeManager, OUTPUT)

        then:
        1 * typeManager.getQueryType(mappingContext.getPersistentEntity(Metadata.name)) >> GraphQLObjectType.newObject().name('x').build()
        !(type instanceof GraphQLList)
    }

}

@Entity
class Book {
    Long id
    String title
    String description
    Metadata metadata
    BookType bookType
    
    OtherMetadata otherMetadata

    static hasMany = [authors: Author, tags: Tag, basics: String]

    static constraints = {
        description nullable: true
    }
}

@Entity
class Book2 implements Serializable {
    String title
    String description
    Metadata metadata
    BookType bookType

    static hasMany = [authors: Author, tags: Tag, basics: String]

    static mapping = {
        id composite: ['title', 'description']
    }

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
        if (other instanceof Book2) {
            other.title == title && other.description == description
        }
    }

    static graphql = true
}

@Entity
class Author {
    String name
    static belongsTo = [book: Book]

    static graphql = true
}

@Entity
class Tag {
    String name
}

@Entity
class Metadata {
    String x
    String y
}

@Entity
class OtherMetadata {
    String x
    String y

    static graphql = true
}

enum BookType {
    OLD, NEW
}
