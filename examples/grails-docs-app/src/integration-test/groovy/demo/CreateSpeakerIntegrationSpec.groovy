package demo

import grails.testing.mixin.integration.Integration
import groovy.json.JsonOutput
import org.grails.gorm.graphql.plugin.testing.GraphQLSpec
import spock.lang.IgnoreIf
import spock.lang.Specification

@Integration
@IgnoreIf({ os.windows })
class CreateSpeakerIntegrationSpec extends Specification implements GraphQLSpec {

    void "test creating a speaker"() {
        when:
        String curlCommand = '''
            // tag::curlCommand[]
curl -X "POST" "{url}" \
     -H "Content-Type: application/graphql" \
     -d $'
mutation {
  speakerCreate(speaker: {
    firstName: "James"
    lastName: "Kleeh"
  }) {
    id
    firstName
    lastName
    errors {
      field
      message
    }
  }
}'
            // end::curlCommand[]
        '''.toString().replace('{url}', getUrl())

        Process process = [ 'bash', '-c', curlCommand ].execute()
        process.waitFor()

        then:
        JsonOutput.prettyPrint(process.text) ==
        """
// tag::response[]
{
    "data": {
        "speakerCreate": {
            "id": 1,
            "firstName": "James",
            "lastName": "Kleeh",
            "errors": [
                
            ]
        }
    }
}
// end::response[]
""".replace('\n// tag::response[]\n', '')
   .replace('\n// end::response[]\n', '')

    }
}
