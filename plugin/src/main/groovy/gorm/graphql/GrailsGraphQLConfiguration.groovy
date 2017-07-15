package gorm.graphql

import grails.config.Config
import grails.config.Settings
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import org.grails.plugins.databinding.DataBindingGrailsPlugin
import org.springframework.boot.context.properties.ConfigurationProperties

import javax.annotation.PostConstruct

@CompileStatic
@ConfigurationProperties(prefix = 'grails.gorm.graphql')
class GrailsGraphQLConfiguration implements GrailsConfigurationAware {

    private Config config

    List<String> dateFormats

    Boolean dateFormatLenient

    Boolean runtimeDataFetching

    Map<String, Class> listArguments

    Boolean browser

    @Override
    void setConfiguration(Config co) {
        config = co
    }

    @PostConstruct
    void init() {
        if (dateFormats == null) {
            dateFormats = config.getProperty('grails.databinding.dateFormats', List, DataBindingGrailsPlugin.DEFAULT_DATE_FORMATS)
        }
        if (dateFormatLenient == null) {
            dateFormatLenient = config.getProperty('grails.databinding.dateParsingLenient', Boolean, false)
        }
        if (runtimeDataFetching == null) {
            runtimeDataFetching = true
        }
        if (browser == null) {
            browser = false
        }
    }
}
