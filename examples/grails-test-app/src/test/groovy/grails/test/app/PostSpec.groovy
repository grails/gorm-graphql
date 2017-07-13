package grails.test.app

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PostSpec extends Specification implements DomainUnitTest<Post> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == false
    }
}
