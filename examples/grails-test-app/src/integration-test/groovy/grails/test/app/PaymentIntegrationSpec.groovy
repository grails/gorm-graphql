package grails.test.app

import grails.testing.mixin.integration.Integration
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import spock.lang.Specification
import spock.lang.Stepwise

@Integration
@Stepwise
class PaymentIntegrationSpec extends Specification implements GraphQLSpec {

    void "test payments can not be created because the class is abstract"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
              paymentCreate(payment: {amount: 5}) {
                id
              }
            }
        """)

        JSONObject result = resp.json

        then:
        result.errors.size() == 1
        result.errors[0].message == "Validation error of type FieldUndefined: Field paymentCreate is undefined"
    }

    void "test creating a credit card payment"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
              creditCardPaymentCreate(creditCardPayment: {
                cardNumber: "1234 5678 9012 3456",
                amount: 56.73
              }) {
                id
                amount
                cardNumber
                errors {
                  field
                  message
                }
              }
            }
        """)
        JSONObject obj = resp.json.data.creditCardPaymentCreate

        then:
        obj.id
        obj.cardNumber == '1234 5678 9012 3456'
        obj.amount == new BigDecimal('56.73')
        obj.errors.empty
    }

    void "test querying a single credit card payment"() {
        when:
        def resp = graphQL.graphql("""
            {
              creditCardPayment(id: 1) {
                id
                amount
                cardNumber
              }
            }
        """)
        JSONObject obj = resp.json.data.creditCardPayment

        then:
        obj.id
        obj.cardNumber == '1234 5678 9012 3456'
        obj.amount == new BigDecimal('56.73')

        when:
        resp = graphQL.graphql("""
            {
              payment(id: 1) {
                id
                amount
                className
              }
            }
        """)
        obj = resp.json.data.payment

        then:
        obj.id
        obj.amount == new BigDecimal('56.73')
        obj.className == 'CreditCardPayment' //The className property is added automatically for entities with children

        when: 'You attempt to query something not available in the parent class, but available in the subclass'
        resp = graphQL.graphql("""
            {
              payment(id: 1) {
                id
                amount
                cardNumber
                className
              }
            }
        """)
        obj = resp.json

        then: 'An error is returned'
        obj.data == null
        obj.errors.size() == 1
        obj.errors[0].message == 'Validation error of type FieldUndefined: Field cardNumber is undefined'
    }

    void "test querying a list of credit card payments"() {
        given:
        graphQL.graphql("""
            mutation {
              creditCardPaymentCreate(creditCardPayment: {
                cardNumber: "xxx yyy zzz aaa",
                amount: 34.43
              }) {
                id
              }
            }
        """)

        when:
        def resp = graphQL.graphql("""
            {
              creditCardPaymentList {
                id
                amount
                cardNumber
              }
            }
        """)
        JSONArray obj = resp.json.data.creditCardPaymentList

        then:
        obj.size() == 2
        obj.find { it.id == 1 }.cardNumber == '1234 5678 9012 3456'
        obj.find { it.id == 2 }.cardNumber == 'xxx yyy zzz aaa'

        when:
        resp = graphQL.graphql("""
            {
              paymentList {
                id
                amount
                className
              }
            }
        """)
        obj = resp.json.data.paymentList

        then:
        obj.size() == 2
        obj.find { it.id == 1 }.amount == new BigDecimal('56.73')
        obj.find { it.id == 1 }.className == 'CreditCardPayment'
        obj.find { it.id == 2 }.amount == new BigDecimal('34.43')
        obj.find { it.id == 2 }.className == 'CreditCardPayment'
    }

    void "test updating a credit card payment"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                creditCardPaymentUpdate(id: 1, creditCardPayment: {
                    cardNumber: "foo",
                    amount: 1
                }) {
                    id
                    cardNumber
                    amount
                }
            }
        """)
        JSONObject obj = resp.json.data.creditCardPaymentUpdate

        then:
        obj.id == 1
        obj.cardNumber == 'foo'
        obj.amount == new BigDecimal('1')

        when: 'A subclass property is provided in the parent class update'
        resp = graphQL.graphql("""
            mutation {
                paymentUpdate(id: 1, payment: {
                    cardNumber: "foo",
                    amount: 1
                }) {
                    id
                    amount
                }
            }
        """)
        obj = resp.json

        then: 'An error is thrown'
        obj.data == null
        obj.errors.size() == 1
        obj.errors[0].message.startsWith('Validation error of type WrongType')

        when:
        resp = graphQL.graphql("""
            mutation {
                paymentUpdate(id: 1, payment: {
                    amount: 2
                }) {
                    id
                    amount
                }
            }
        """)
        obj = resp.json.data.paymentUpdate

        then:
        obj.amount == new BigDecimal('2')
    }

    void "test deleting a credit card payment"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                creditCardPaymentDelete(id: 1) {
                    success
                }
            }
        """)
        JSONObject obj = resp.json.data.creditCardPaymentDelete

        then:
        obj.success

        when:
        resp = graphQL.graphql("""
            mutation {
                paymentDelete(id: 2) {
                    success                  
                }
            }
        """)
        obj = resp.json.data.paymentDelete

        then:
        obj.success
    }

}
