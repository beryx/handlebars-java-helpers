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

import com.github.jknack.handlebars.EscapingStrategy
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class HelpersSpec extends Specification {
    def setupSpec() {
        String.metaClass.stripAll = { ->
            delegate.stripIndent().trim().replaceAll('(?m)^\\s*$\\n', '')
        }
    }

    def context = '''
        company: Acme Corporation
        location: Grand Canyon
        employees: 77
        managers: 7
        private: false

        customer:
          - name: Wile E. Coyote
            age: 25
          - name: Road Runner
            age: 21

        product:
          - name: dynamite
            price: 15
            available: true
          - name: rocket
            price: 200
            available: false
          - name: rubber band
            price: 10
        '''.stripIndent()

    String merge(String template, String context) {
        Handlebars handlebars = new Handlebars()
                .with(EscapingStrategy.NOOP)
                .with(new StringTemplateLoader())
        Helpers.values().each { helper -> handlebars.registerHelper(helper.helperName, helper)}
        Template tmpl = handlebars.compile(template)
        Yaml yaml = new Yaml()
        def ctx = yaml.load(context)
        tmpl.apply(ctx).stripAll()
    }

    def "should define variables"() {
        given:
        def template = '''
            {{#each product}}
            {{#def 'prd'}}{{name}}-{{price}}{{/def}}
            product-{{@index}}: {{prd}}
            {{/each}}
            out of scope: '{{prd}}'
        '''

        when:
        def merged = merge(template, context)

        then:
        println merged
        merged == '''
            product-0: dynamite-15
            product-1: rocket-200
            product-2: rubber band-10
            out of scope: ''
        '''.stripAll()
    }

    def "should compute the length of a list"() {
        given:
        def template = '''
            len = {{length product}}
        '''

        when:
        def merged = merge(template, context)

        then:
        println merged
        merged == 'len = 3'
    }

    def "string compare should evaluate to #res: company #op location"() {
        given:
        def template = "$op : {{compare company '$op' location}}"

        when:
        def merged = merge(template, context)

        then:
        merged == "$op : $res"

        where:
        op   | res
        '==' | false
        '!=' | true
        '<'  | true
        '<=' | true
        '>'  | false
        '>=' | false
    }

    def "numeric compare should evaluate to #res: employees #op managers"() {
        given:
        def template = "$op : {{compare employees '$op' managers}}"

        when:
        def merged = merge(template, context)

        then:
        merged == "$op : $res"

        where:
        op   | res
        '==' | false
        '!=' | true
        '<'  | false
        '<=' | false
        '>'  | true
        '>=' | true
    }

    def "compare should evaluate to #res: #expr"() {
        given:
        def template = "{{compare $expr}}"

        when:
        def merged = merge(template, context)

        then:
        merged == "$res"

        where:
        expr                            | res
        "7 '==' 7"                      | true
        "7 '==' '7'"                    | true
        "7 '==' '7.0'"                  | true
        "7 '==' '7.0' asString=true"    | false
        "7 '==' '7.0' asString=false"   | true
        "'7' '==' 7"                    | true
        "'7' '==' '7'"                  | true
        "'7' '==' '7.0'"                | true
        "'7' '==' '7.0' asString=true"  | false
        "'7' '==' '7.0' asString=false" | true
    }


    def "the negated value of #value should be #negated"() {
        given:
        def template = "not: {{not $value}}"

        when:
        def merged = merge(template, context)

        then:
        merged == "not: $negated"

        where:
        value     | negated
        "private" | true
        "true"    | false
        "'true'"  | false
        "'TRUE'"  | false
        "'tRuE'"  | false
        "false"   | true
        "'false'" | true
        "'FALSE'" | true
        "'fAlSe'" | true
        "''"      | true
        "' '"     | false
        "zzz"     | true
        "'zzz'"   | false
        "'3.14'"  | false
        "0"       | true
        "'0'"     | false
        "1"       | false
        "'1'"     | false
    }

    def "AND should evaluate to #res: #operands"() {
        given:
        def template = "{{and $operands}}"

        when:
        def merged = merge(template, context)

        then:
        merged == "${Boolean.toString(res)}"

        where:
        operands            | res
        'false'             | false
        'false false'       | false
        'false true'        | false
        'false false false' | false
        'false false true'  | false
        'false true false'  | false
        'false true true'   | false
        'true'              | true
        'true false'        | false
        'true true'         | true
        'true false false'  | false
        'true false true'   | false
        'true true false'   | false
        'true true true'    | true
    }

    def "OR should evaluate to #res: #operands"() {
        given:
        def template = "{{or $operands}}"

        when:
        def merged = merge(template, context)

        then:
        merged == "${Boolean.toString(res)}"

        where:
        operands            | res
        'false'             | false
        'false false'       | false
        'false true'        | true
        'false false false' | false
        'false false true'  | true
        'false true false'  | true
        'false true true'   | true
        'true'              | true
        'true false'        | true
        'true true'         | true
        'true false false'  | true
        'true false true'   | true
        'true true false'   | true
        'true true true'    | true
    }

    def "expression should evaluate to #res: #expr"() {
        given:
        def template = "res: {{$expr}}"

        when:
        def merged = merge(template, context)

        then:
        merged == "res: $res"

        where:
        expr                                                                                        | res
        "not (compare employees '<' managers)"                                                      | true
        "and (compare 5 '<' 17) (compare 13 '>=' 9)"                                                | true
        "or (not (compare 7 '==' '7.0' asString: true)) private (compare 13 '<=' (length product))" | false
    }

    def "asJavaId should convert '#text' to '#id'"() {
        given:
        def template = "{{asJavaId $text}}"

        when:
        def merged = merge(template, context)

        then:
        merged == "$id"

        where:
        text                                                | id
        "company"                                           | 'acmeCorporation'
        "'Hello, world!'"                                   | 'helloWorld'
        "'Hello, world!' camelCase=false"                   | 'helloworld'
        "'Hello, world!' underscore=true"                   | 'hello_World'
        "'Hello, world!' underscore=true camelCase=false"   | 'hello_world'
        "''"                                                | '_'
        "'3.14'"                                            | '_314'
        "'3.14' underscore=true"                            | '_3_14'
        "'   Should be trimmed   '"                         | 'shouldBeTrimmed'
        "'###...-- *** This is a warning !!! *** --...###'" | 'thisIsAWarning'
        "'!!! 6 x 9 = 42'"                                  | '_6X942'
        "'!!! 6 x 9 = 42' camelCase=false underscore=true"  | '_6_x_9_42'
    }
}
