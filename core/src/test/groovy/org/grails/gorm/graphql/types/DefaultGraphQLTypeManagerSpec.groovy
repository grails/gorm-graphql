package org.grails.gorm.graphql.types

import graphql.Scalars
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeReference
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.GraphQL
import org.grails.gorm.graphql.HibernateSpec
import org.grails.gorm.graphql.domain.general.toone.One
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.property.manager.DefaultGraphQLDomainPropertyManager
import org.grails.gorm.graphql.response.errors.DefaultGraphQLErrorsResponseHandler
import org.grails.gorm.graphql.testing.GraphQLSchemaSpec
import org.grails.gorm.graphql.types.input.InputObjectTypeBuilder
import org.grails.gorm.graphql.types.output.ObjectTypeBuilder
import org.grails.gorm.graphql.types.scalars.GormScalars
import org.grails.gorm.graphql.types.scalars.GraphQLByteArray
import org.grails.gorm.graphql.types.scalars.GraphQLCharacterArray
import org.springframework.context.support.StaticMessageSource
import spock.lang.Unroll

import java.sql.Timestamp
import java.sql.Time
import java.util.concurrent.atomic.AtomicLong

class DefaultGraphQLTypeManagerSpec extends HibernateSpec implements GraphQLSchemaSpec {

    List<Class> getDomainClasses() { [One] }

    DefaultGraphQLTypeManager typeManager
    
    void setup() {
        typeManager = new DefaultGraphQLTypeManager(new GraphQLEntityNamingConvention(), new DefaultGraphQLErrorsResponseHandler(new StaticMessageSource()), new DefaultGraphQLDomainPropertyManager())
    }

    @Unroll
    void "test default type exists for #clazz"() {
        when:
        typeManager.getType(clazz)
        
        then:
        notThrown(TypeNotFoundException)
        
        where:
        clazz << [Integer, Long, Short, Byte, Byte[], Double, Float, BigInteger, BigDecimal, String, Boolean, Character, Character[], UUID, URL, URI, Time, java.sql.Date, Timestamp, Currency, TimeZone]
    }

    @Unroll
    void "test getType works with primitive type #clazz"() {
        when:
        GraphQLType type = typeManager.getType(clazz)

        then:
        type == expectedType

        where:
        clazz   | expectedType
        boolean | Scalars.GraphQLBoolean
        int     | GormScalars.GraphQLInt
        short   | GormScalars.GraphQLShort
        byte    | GormScalars.GraphQLByte
        char    | Scalars.GraphQLChar
        long    | GormScalars.GraphQLLong
        float   | Scalars.GraphQLFloat
        double  | Scalars.GraphQLFloat
    }

    @Unroll
    void "test getType works with primitive array type #clazz"() {
        when:
        GraphQLType type = typeManager.getType(clazz)

        then:
        expectedType.isAssignableFrom(type.class)

        where:
        clazz  | expectedType
        byte[] | GraphQLByteArray
        char[] | GraphQLCharacterArray
    }

    void "test getType nullable false"() {
        when:
        GraphQLType type = typeManager.getType(String, false)

        then:
        type instanceof GraphQLNonNull
        unwrap(null, type) == Scalars.GraphQLString
    }

    void "test registerType"() {
        when:
        typeManager.getType(AtomicLong)

        then:
        thrown(TypeNotFoundException)

        when:
        typeManager.registerType(AtomicLong, GormScalars.GraphQLLong)

        then:
        typeManager.getType(AtomicLong) == GormScalars.GraphQLLong
    }

    void "test getEnumType Foo"() {
        when:
        GraphQLEnumType enumType = (GraphQLEnumType)typeManager.getEnumType(Foo, true)

        then:
        enumType.description == 'Foo type'
        enumType.name == 'Foo'
        enumType.values[0].name == 'X'
        enumType.values[0].description == null
        enumType.values[0].deprecationReason == 'Deprecated'
        enumType.values[0].deprecated
        enumType.values[1].name == 'Y'
        enumType.values[1].description == null
        enumType.values[1].deprecationReason == 'Y is deprecated'
        enumType.values[1].deprecated
        enumType.values[2].name == 'Z'
        enumType.values[2].description == 'This is the Z property'
        enumType.values[2].deprecationReason == null
        !enumType.values[2].deprecated
    }

    void "test getEnumType Bar"() {
        when:
        GraphQLEnumType enumType = (GraphQLEnumType)typeManager.getEnumType(Bar, true)

        then:
        enumType.description == null
        enumType.name == 'Bar'
        enumType.values[0].name == 'A'
        enumType.values[0].description == null
        enumType.values[0].deprecationReason == null
        !enumType.values[0].deprecated
    }

    void "test getQueryType"() {
        given:
        PersistentEntity entity = One.gormPersistentEntity
        GraphQLObjectType type = GraphQLObjectType.newObject().name("One").field(GraphQLFieldDefinition.newFieldDefinition().name('foo').type(Scalars.GraphQLString).build()).build()
        typeManager.objectTypeBuilders.put(GraphQLPropertyType.OUTPUT, Mock(ObjectTypeBuilder) {
            1 * build(entity) >> type
        })

        expect:
        typeManager.getQueryType(entity, GraphQLPropertyType.OUTPUT) == type
    }

    void "test getQueryType circular"() {
        given:
        PersistentEntity entity = One.gormPersistentEntity
        GraphQLObjectType type = GraphQLObjectType.newObject().name("One").field(GraphQLFieldDefinition.newFieldDefinition().name('foo').type(Scalars.GraphQLString).build()).build()
        GraphQLOutputType nestedType = null
        typeManager.objectTypeBuilders.put(GraphQLPropertyType.OUTPUT, Mock(ObjectTypeBuilder) {
            1 * build(entity) >> {
                nestedType = typeManager.getQueryType(entity, GraphQLPropertyType.OUTPUT)
                type
            }
        })

        when:
        GraphQLOutputType returnedType = typeManager.getQueryType(entity, GraphQLPropertyType.OUTPUT)

        then:
        returnedType == type
        nestedType instanceof GraphQLTypeReference
        notThrown(StackOverflowError)
    }

    void "test get mutation type"() {
        given:
        PersistentEntity entity = One.gormPersistentEntity
        GraphQLInputObjectType type = GraphQLInputObjectType.newInputObject().name('Test').field(GraphQLInputObjectField.newInputObjectField().name('foo').type(Scalars.GraphQLString)).build()
        typeManager.inputObjectTypeBuilders.put(GraphQLPropertyType.CREATE, Mock(InputObjectTypeBuilder) {
            2 * build(entity) >> type
        })

        expect:
        typeManager.getMutationType(entity, GraphQLPropertyType.CREATE, true) == type
        unwrap(null, typeManager.getMutationType(entity, GraphQLPropertyType.CREATE, false)) == type
    }

    @GraphQL('Foo type')
    enum Foo {

        @GraphQL(deprecated = true)
        X,

        @GraphQL(deprecationReason = 'Y is deprecated')
        Y,

        @GraphQL('This is the Z property')
        Z

    }

    enum Bar {
        A
    }
}
