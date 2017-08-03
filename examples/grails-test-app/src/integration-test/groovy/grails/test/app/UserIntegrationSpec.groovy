package grails.test.app

import org.grails.gorm.graphql.plugin.testing.GraphQLSpec
import grails.testing.mixin.integration.Integration
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import spock.lang.Specification
import spock.lang.Stepwise

@Integration
@Stepwise
class UserIntegrationSpec extends Specification implements GraphQLSpec {

    void "test creating a user without a profile"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    address: {
                        city: "Youngstown",
                        state: "OH",
                        zip: 44512
                    }
                }) {
                    id
                }
            }
        """)
        JSONObject obj = resp.json

        then:
        obj.data == null
        obj.errors.size() == 1
        obj.errors[0].message.startsWith('Validation error of type WrongType')

        when: 'The profile is provided, but missing a required field'
        resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    profile: {
                        email: "email",
                        firstName: "First"
                    }
                    address: {
                        city: "Youngstown",
                        state: "OH",
                        zip: 44512
                    }
                }) {
                    id
                }
            }
        """)
        obj = resp.json

        then:
        obj.data == null
        obj.errors.size() == 1
        obj.errors[0].message.startsWith('Validation error of type WrongType')
    }

    void "test creating a user without an address"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    profile: {
                        email: "email",
                        firstName: "First",
                        lastName: "Last"
                    }
                }) {
                    id
                }
            }
        """)
        JSONObject obj = resp.json

        then:
        obj.data == null
        obj.errors.size() == 1
        obj.errors[0].message.startsWith('Validation error of type WrongType')

        when: 'The address is provided, but missing a required field'
        resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    profile: {
                        email: "email",
                        firstName: "First",
                        lastName: "Last"
                    },
                    address: {
                        city: "Youngstown",
                        state: "OH"
                    }
                }) {
                    id
                }
            }
        """)
        obj = resp.json

        then:
        obj.data == null
        obj.errors.size() == 1
        obj.errors[0].message.startsWith('Validation error of type WrongType')
    }

    void "test creating the top level manager"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 2,
                    secondNumber: 3,
                    profile: {
                        email: "email",
                        firstName: "First",
                        lastName: "Last"
                    }
                    address: {
                        city: "Youngstown",
                        state: "OH",
                        zip: 44512
                    }
                }) {
                    id
                    addedNumbers
                    profile {
                        email
                        firstName
                        lastName
                    }
                    address {
                        city
                        state
                        zip
                    }
                    manager {
                        id
                    }
                }
            }
        """)
        JSONObject obj = resp.json.data.userCreate

        then:
        obj.id == 1
        obj.addedNumbers == 5
        obj.profile.email == 'email'
        obj.profile.firstName == 'First'
        obj.profile.lastName == 'Last'
        obj.address.city == 'Youngstown'
        obj.address.state == 'OH'
        obj.address.zip == 44512
        obj.manager == null
    }

    void "create new user with manager"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userCreate(user: {
                    firstNumber: 4,
                    secondNumber: 7,
                    profile: {
                        email: "email",
                        firstName: "First",
                        lastName: "Last"
                    }
                    address: {
                        city: "Youngstown",
                        state: "OH",
                        zip: 44512
                    }
                    manager: {
                        id: 1
                    }
                }) {
                    id
                    addedNumbers
                    profile {
                        email
                        firstName
                        lastName
                    }
                    address {
                        city
                        state
                        zip
                    }
                    manager {
                        id
                    }
                }
            }
        """)
        JSONObject obj = resp.json.data.userCreate

        then:
        obj.id == 2
        obj.addedNumbers == 11
        obj.profile.email == 'email'
        obj.profile.firstName == 'First'
        obj.profile.lastName == 'Last'
        obj.address.city == 'Youngstown'
        obj.address.state == 'OH'
        obj.address.zip == 44512
        obj.manager.id == 1
    }

    void "test updating a user"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userUpdate(id: 2, user: {
                    firstNumber: 5,
                    secondNumber: 7,
                    profile: {
                        email: "emailUpdated",
                        firstName: "FirstUpdated",
                        lastName: "LastUpdated"
                    }
                    address: {
                        city: "Pittsburgh",
                        state: "PA",
                        zip: 90210
                    }
                }) {
                    id
                    addedNumbers
                    profile {
                        email
                        firstName
                        lastName
                    }
                    address {
                        city
                        state
                        zip
                    }
                    manager {
                        id
                    }
                }
            }
        """)
        JSONObject obj = resp.json.data.userUpdate

        then:
        obj.id == 2
        obj.addedNumbers == 12
        obj.profile.email == 'emailUpdated'
        obj.profile.firstName == 'FirstUpdated'
        obj.profile.lastName == 'LastUpdated'
        obj.address.city == 'Pittsburgh'
        obj.address.state == 'PA'
        obj.address.zip == 90210
        obj.manager.id == 1
    }

    void "test listing users"() {
        when:
        def resp = graphQL.graphql("""
            {
                userList(sort: "id") {
                    id
                    addedNumbers
                    profile {
                        email
                        firstName
                        lastName
                    }
                    address {
                        city
                        state
                        zip
                    }
                    manager {
                        id
                        addedNumbers
                    }
                }
            }
        """)
        JSONArray obj = resp.json.data.userList

        then:
        obj.size() == 2

        obj[0].id == 1
        obj[0].addedNumbers == 5
        obj[0].profile.email == 'email'
        obj[0].profile.firstName == 'First'
        obj[0].profile.lastName == 'Last'
        obj[0].address.city == 'Youngstown'
        obj[0].address.state == 'OH'
        obj[0].address.zip == 44512
        obj[0].manager == null

        obj[1].id == 2
        obj[1].addedNumbers == 12
        obj[1].profile.email == 'emailUpdated'
        obj[1].profile.firstName == 'FirstUpdated'
        obj[1].profile.lastName == 'LastUpdated'
        obj[1].address.city == 'Pittsburgh'
        obj[1].address.state == 'PA'
        obj[1].address.zip == 90210
        obj[1].manager.id == 1
        obj[1].manager.addedNumbers == 5
    }

    void "test querying a single user"() {
        when:
        def resp = graphQL.graphql("""
            {
                user(id: 2) {
                    id
                    addedNumbers
                    profile {
                        email
                        firstName
                        lastName
                    }
                    address {
                        city
                        state
                        zip
                    }
                    manager {
                        id
                        addedNumbers
                    }
                }
            }
        """)
        JSONObject obj = resp.json.data.user

        then:
        obj.id == 2
        obj.addedNumbers == 12
        obj.profile.email == 'emailUpdated'
        obj.profile.firstName == 'FirstUpdated'
        obj.profile.lastName == 'LastUpdated'
        obj.address.city == 'Pittsburgh'
        obj.address.state == 'PA'
        obj.address.zip == 90210
        obj.manager.id == 1
        obj.manager.addedNumbers == 5
    }

    void "test deleting a user that is a manager of another user"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userDelete(id: 1) {
                    success
                }
            }
        """)
        JSONObject obj = resp.json.data.userDelete

        then:
        !obj.success
    }

    void "test deleting a user that is NOT a manager of another user"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userDelete(id: 2) {
                    success
                }
            }
        """)
        JSONObject obj = resp.json.data.userDelete

        then:
        obj.success
    }

    void "test deleting the last user is now successful"() {
        when:
        def resp = graphQL.graphql("""
            mutation {
                userDelete(id: 1) {
                    success
                }
            }
        """)
        JSONObject obj = resp.json.data.userDelete

        then:
        obj.success
    }
}
