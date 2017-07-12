package grails.test.app

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class BookSpec extends Specification implements DomainUnitTest<Book> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == false
    }
}
