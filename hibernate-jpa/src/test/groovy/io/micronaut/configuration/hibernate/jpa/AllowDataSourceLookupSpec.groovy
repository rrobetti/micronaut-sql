/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.hibernate.jpa

import io.micronaut.context.ApplicationContext
import spock.lang.Specification

class AllowDataSourceLookupSpec extends Specification {

    void "test allow-datasource-lookup defaults to true"() {
        given:
        def ctx = ApplicationContext.run(
                'jpa.default.properties.foo':'bar'
        )

        def config = ctx.getBean(JpaConfiguration)

        expect:
        config.allowDataSourceLookup == true

        cleanup:
        ctx?.close()
    }

    void "test allow-datasource-lookup can be set to false"() {
        given:
        def ctx = ApplicationContext.run(
                'jpa.default.allowDataSourceLookup':'false'
        )

        def config = ctx.getBean(JpaConfiguration)

        expect:
        config.allowDataSourceLookup == false

        cleanup:
        ctx?.close()
    }

    void "test allow-datasource-lookup can be set to true explicitly"() {
        given:
        def ctx = ApplicationContext.run(
                'jpa.default.allowDataSourceLookup':'true'
        )

        def config = ctx.getBean(JpaConfiguration)

        expect:
        config.allowDataSourceLookup == true

        cleanup:
        ctx?.close()
    }

    void "test copy of JPA configuration preserves allow-datasource-lookup setting"() {
        given:
        def ctx = ApplicationContext.run(
                'jpa.default.allowDataSourceLookup':'false',
                'jpa.default.reactive':'true'
        )

        def config = ctx.getBean(JpaConfiguration)
        def configCopy = config.copy('configCopy')

        expect:
        config.allowDataSourceLookup == false
        configCopy.allowDataSourceLookup == false
        config.reactive == configCopy.reactive

        cleanup:
        ctx?.close()
    }

    void "test allow-datasource-lookup per datasource"() {
        given:
        def ctx = ApplicationContext.run(
                'jpa.default.allowDataSourceLookup':'true',
                'jpa.other.allowDataSourceLookup':'false'
        )

        def defaultConfig = ctx.getBean(JpaConfiguration)

        expect:
        defaultConfig.allowDataSourceLookup == true
        defaultConfig.name == 'default'

        cleanup:
        ctx?.close()
    }
}
