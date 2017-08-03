package grails.test.app

import org.grails.gorm.graphql.plugin.testing.GraphQLSpec
import grails.testing.mixin.integration.Integration
import org.grails.web.json.JSONObject
import spock.lang.Specification

@Integration
class TypeTestIntegrationSpec extends Specification implements GraphQLSpec {

    void "test create"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
              typeTestCreate(typeTest: {
                 integer: 500,
                 aLong: 5000,
                 aShort: 6,
                 aByte: 1,
                 aDouble: 10.5,
                 aFloat: 15.1,
                 bigInteger: 1000000,
                 bigDecimal: 1000000.99,
                 string: "a string",
                 aBoolean: false,
                 character: "z",
                 uuid: "e1c6f838-f24b-46de-ad93-40f5a4762ec2",
                 url: "http://www.google.com",
                 uri: "//www.google.com",
                 date: "1941-01-05 08:30:00.0",
                 bytes: [1,2,3],
                 characters: ["a", "b", "c"],
                 time: "08:01:02",
                 sqlDate: "1941-01-05",
                 timestamp: "1941-01-05 08:01:02",
                 currency: "USD",
                 timeZone: "EST",
                 charsPrimitive: ["a", "b", "c"],
                 bytesPrimitive: [1,2,3],
                 intPrimitive: 500,
                 longPrimitive: 5000,
                 shortPrimitive: 5,
                 bytePrimitive: 6,
                 doublePrimitive: 10.8,
                 floatPrimitive: 1908.6,
                 charPrimitive: "x",
                 booleanPrimitive: true             
              }) {
                id
                errors {
                  field
                  message
                }  
              }
            }
        """)

        JSONObject json = resp.json.data.typeTestCreate

        then:
        json.id
    }


}
