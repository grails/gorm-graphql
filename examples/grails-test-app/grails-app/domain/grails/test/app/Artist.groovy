package grails.test.app

import grails.test.app.pogo.Painting
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping

class Artist {

    String name

    static graphql = GraphQLMapping.build {
        add('paintings', [Painting]) {
            dataFetcher {
                return [new Painting(name: 'test', artistName: 'Picasso', heightCm: 60, widthCm: 120)]
            }
        }
    }
}
