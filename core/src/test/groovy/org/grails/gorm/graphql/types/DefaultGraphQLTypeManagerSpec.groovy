package org.grails.gorm.graphql.types

import graphql.Scalars
import graphql.schema.GraphQLType
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.property.manager.DefaultGraphQLDomainPropertyManager
import org.grails.gorm.graphql.response.errors.DefaultGraphQLErrorsResponseHandler
import org.grails.gorm.graphql.testing.GraphQLSchemaSpec
import org.grails.gorm.graphql.types.scalars.GormScalars
import org.grails.gorm.graphql.types.scalars.GraphQLByteArray
import org.grails.gorm.graphql.types.scalars.GraphQLCharacterArray
import org.springframework.context.support.StaticMessageSource
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Timestamp
import java.sql.Time

class DefaultGraphQLTypeManagerSpec extends Specification implements GraphQLSchemaSpec {

    GraphQLTypeManager typeManager
    
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
}
