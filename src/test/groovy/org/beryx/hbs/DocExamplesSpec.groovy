/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.beryx.hbs

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Specifications of the examples used in the documentation
 */
@Unroll
class DocExamplesSpec extends Specification implements TestUtil {
    def "doc example: def"() {
        given:
        def ctx = '''
            user: jsmith
            domain: example.com
            offline: false
        '''
        def template = '''
            {{def 'online' (not offline)}}
            {{#def 'email'}}{{user}}@{{domain}}{{/def}}
            Online: {{online}}
            You can contact me at {{email}}.
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            Online: true
            You can contact me at jsmith@example.com.
        '''.stripAll()
    }

    def "doc example: ifb"() {
        given:
        def ctx = '''
            offline: false
            value: 5
        '''
        def template = '''
            if : online = {{#if (not offline)}}YES{{else}}NO{{/if}}
            ifb: online = {{#ifb (not offline)}}YES{{else}}NO{{/ifb}}
            if : {{value}} is an {{#if (math value '%' 2)}}odd{{else}}even{{/if}} number
            ifb: {{value}} is an {{#ifb (math value '%' 2)}}odd{{else}}even{{/ifb}} number
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            if : online = NO
            ifb: online = YES
            if : 5 is an even number
            ifb: 5 is an odd number
        '''.stripAll()
    }

    def "doc example: default"() {
        given:
        def ctx = '''
            bonus: 100
        '''
        def template = '''
            bonus = {{default bonus 0}} points
            penalty = {{default penalty 0}} points
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            bonus = 100 points
            penalty = 0 points
        '''.stripAll()
    }

    def "doc example: length"() {
        given:
        def ctx = '''
            developers:
              - name: Alice
              - name: Brenda
              - name: Colin
        '''
        def template = '''
            Team size: {{length developers}} developers
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            Team size: 3 developers
        '''.stripAll()
    }

    def "doc example: math 1"() {
        given:
        def ctx = '''
            amount: 1500
            bonus: 100
            penalty: 30
        '''
        def template = '''
            Your score: {{math (math amount '+' bonus) '-' penalty}} points
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            Your score: 1570 points
        '''.stripAll()
    }

    def "doc example: math 2"() {
        given:
        def ctx = '''
            amount: 250
            rate: 0.15
        '''
        def template = '''
            interest: {{math amount '*' rate decimals=2 locale='de'}} EUR
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            interest: 37,50 EUR
        '''.stripAll()
    }

    def "doc example: compare"() {
        given:
        def ctx = '''
            bonus: 100
            penalty: 30
        '''
        def template = '''
            Exceeded allowed penalty: {{compare penalty '>=' 50}}
            {{#ifb (compare bonus '>' penalty)}}
            You won!
            {{else}}
            Game over
            {{/ifb}}
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            Exceeded allowed penalty: false
            You won!
        '''.stripAll()
    }

    def "doc example: asBoolean"() {
        given:
        def ctx = '''
            middleName: Maria
            debt: 0
            credit: 5
        '''
        def template = '''
            Has nickname: {{asBoolean nickname}}
            Has middle name: {{asBoolean middleName}}
            Has debt: {{asBoolean debt}}
            Has credit: {{asBoolean credit}}
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            Has nickname: false
            Has middle name: true
            Has debt: false
            Has credit: true
        '''.stripAll()
    }


    def "doc example: not"() {
        given:
        def ctx = '''
            offline: false
        '''
        def template = '''
            Online: {{not offline}}
            {{#ifb (not offline)}}
            Invite a friend to play with you!
            {{else}}
            Single-player mode.
            {{/ifb}}
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            Online: true
            Invite a friend to play with you!
        '''.stripAll()
    }


    def "doc example: and"() {
        given:
        def ctx = '''
            motorized: true
            aircraft: false
            wheels: 2
        '''
        def template = '''
            airliner: {{and motorized aircraft}}
            {{#ifb (and motorized (not aircraft) (compare wheels '==' 2))}}
            You won a motorcycle!
            {{else}}
            Sorry, we only offer motorcycles.
            {{/ifb}}
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            airliner: false
            You won a motorcycle!
        '''.stripAll()
    }


    def "doc example: or"() {
        given:
        def ctx = '''
            admin: false
            developer: true
            accessLevel: 2
        '''
        def template = '''
            committer: {{or admin developer}}
            {{#ifb (or admin developer (compare accessLevel '>=' 4))}}
            Click here to download the logs.
            {{else}}
            Sorry, you are not allowed to view the logs.
            {{/ifb}}
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            committer: true
            Click here to download the logs.
        '''.stripAll()
    }


    def "doc example: asJavaId"() {
        given:
        def ctx = '''
            service: byte-as-an-octet
        '''
        def template = '''
            serviceId: {{asJavaId service}}
            package: {{asJavaId service camelCase=false underscore=true}}
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == '''
            serviceId: byteAsAnOctet
            package: byte_as_an_octet
        '''.stripAll()
    }


    def "README example: the fifteenth anniversary of a person born in #year was in a #res year"() {
        given:
        def ctx = "birthYear: $year"
        def template = '''
            {{def 'fifteenYear' (math birthYear '+' 15)}}
            {{#ifb (or
                    (and
                        (compare (math fifteenYear '%' 4) '==' 0)
                        (compare (math fifteenYear '%' 100) '!=' 0)
                    )
                    (compare (math fifteenYear '%' 400) '==' 0)
                   )
            }}
            Your fifteenth anniversary was in a leap year!
            {{else}}
            Your fifteenth anniversary was in a non-leap year!
            {{/ifb}}
        '''

        when:
        def merged = merge(template, ctx)

        then:
        merged == """
            Your fifteenth anniversary was in a $res year!
        """.stripAll()

        where:
        year | res
        1895 | 'non-leap'
        1896 | 'non-leap'
        1985 | 'leap'
        1997 | 'leap'
        1998 | 'non-leap'
    }

}
