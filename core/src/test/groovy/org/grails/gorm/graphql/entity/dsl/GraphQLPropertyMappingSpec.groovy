package org.grails.gorm.graphql.entity.dsl

import spock.lang.Specification

class GraphQLPropertyMappingSpec extends Specification {

    void "test build"() {
        given:
        GraphQLPropertyMapping mapping = GraphQLPropertyMapping.build {
            input false
            output false
            deprecated true
            deprecationReason 'reason'
            description 'description'
            argument 'argument', [String]
            dataFetcher {

            }
        }

        expect:
        !mapping.input
        !mapping.output
        mapping.deprecated
        mapping.deprecationReason == 'reason'
        mapping.description == 'description'
        mapping.arguments.size() == 1
        mapping.arguments.first().name == 'argument'
        mapping.dataFetcher instanceof Closure
    }
}
