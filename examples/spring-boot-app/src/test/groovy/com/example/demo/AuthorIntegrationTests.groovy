package com.example.demo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.ResponseEntity
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthorIntegrationTests extends Specification {

    @Autowired
    private TestRestTemplate restTemplate

    void createClient() {
        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity("/graphql", "{ authorList { id } }", String)
        String response = responseEntity.body

        expect:
        response == '{"data":{"authorList":[]}}'
    }


}