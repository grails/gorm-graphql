package grails32.test.app

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class AuthorSpec extends Specification implements DomainUnitTest<Author> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == false
    }
}
