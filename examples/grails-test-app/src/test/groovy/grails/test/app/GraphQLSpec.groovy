package grails.test.app

import grails.core.GrailsApplication
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

trait GraphQLSpec {

    private static RestBuilder _rest
    private static String _url

    @Value('${local.server.port}')
    String serverPort

    RestResponse post(String request) {
        rest.post(url) {
            contentType('application/graphql')
            body(request)
        }
    }

    RestBuilder getRest() {
        if (_rest == null) {
            _rest = new RestBuilder()
        }
        _rest
    }

    String getUrl() {
        if (_url == null) {
            _url = "http://localhost:${serverPort}/graphql"
        }
        _url
    }

}
