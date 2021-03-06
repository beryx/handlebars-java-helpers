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

import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromAbstractTypeMethods
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.LocaleUtils

import java.nio.charset.StandardCharsets

@Slf4j
enum Helpers implements Helper {
    DEF ("def", { varName, options ->
        Context ctx = options.context
        def val = (options.params.length > 0) ? options.prm(0) : options.fn()
        varName = from(varName)
        ctx.combine(varName, val)
        options.buffer()
    }),

    IFB ("ifb", { value, options ->
        Options.Buffer buffer = options.buffer()
        value = from(value)
        if(isNumber(value)) {
            value = asNumber(value)
        }
        buffer.append(asBoolean(value) ? options.fn() : options.inverse())
        buffer
    }),

    DEFAULT ("default", { value, options ->
        if(!(value instanceof Boolean) && Handlebars.Utils.isEmpty(value)) {
            value = options.prm(0)
        }
        Options.Buffer buffer = options.buffer()
        buffer.append(String.valueOf(from(value)))
        buffer
    }),

    LENGTH ("length", { list, options ->
        Options.Buffer buffer = options.buffer()
        buffer.append("${list.size()}")
        buffer
    }),

    COMPARE ("compare", { operand1, options ->
        Options.Buffer buffer = options.buffer()
        operand1 = from(operand1)
        def op = options.prm(0)
        def operand2 = options.prm(1)
        boolean asString = options.hash("asString", false)
        if(!asString && isNumber(operand1) && isNumber(operand2)) {
            operand1 = operand1 as double
            operand2 = operand2 as double
        } else {
            operand1 = operand1.toString()
            operand2 = operand2.toString()
        }
        int res = operand1.compareTo(operand2)

        def cmp = {
            switch (op) {
                case '==': return res == 0
                case '!=': return res != 0
                case '<': return res < 0
                case '<=': return res <= 0
                case '>': return res > 0
                case '>=': return res >= 0
            }
        }
        buffer.append(cmp() ? 'true' : 'false')
        buffer
    }),

    MATH ("math", { value, options ->
        Options.Buffer buffer = options.buffer()
        def op = options.prm(0)
        def operand1 = asNumber(from(value))
        def operand2 = asNumber(options.prm(1))

        def cmp = {
            switch (op) {
                case '+': return operand1 + operand2
                case '-': return operand1 - operand2
                case '*': return operand1 * operand2
                case '/': return operand1 / operand2
                case '%': return operand1 % operand2
                case '**': return Math.pow(operand1, operand2)
            }
        }
        int decimals = options.hash('decimals', -1)
        if(decimals < 0) {
            buffer.append(cmp().toString())
        } else {
            def format = "%.${decimals}f"
            String localeStr = options.hash("locale", Locale.getDefault().toString());
            Locale locale = LocaleUtils.toLocale(localeStr);
            buffer.append(String.format(locale, format, cmp() as double))
        }
        buffer
    }),

    AS_BOOLEAN ("asBoolean", { value, options ->
        Options.Buffer buffer = options.buffer()
        value = from(value)
        buffer.append(Boolean.toString(asBoolean(value)))
        buffer
    }),

    NOT ("not", { value, options ->
        Options.Buffer buffer = options.buffer()
        value = from(value)
        buffer.append(Boolean.toString(!asBoolean(value)))
        buffer
    }),

    AND ("and", { operand1, options ->
        Options.Buffer buffer = options.buffer()
        boolean res = asBoolean(from(operand1))
        for(int i=0; res && (i < options.params.length); i++) {
            res = asBoolean(options.prm(i))
        }
        buffer.append(Boolean.toString(res))
        buffer
    }),

    OR ("or", { operand1, options ->
        Options.Buffer buffer = options.buffer()
        boolean res = asBoolean(from(operand1))
        for(int i=0; !res && (i < options.params.length); i++) {
            res = asBoolean(options.prm(i))
        }
        buffer.append(Boolean.toString(res))
        buffer
    }),

    AS_JAVA_ID("asJavaId", { text, options ->
        Options.Buffer buffer = options.buffer()
        text = from(text)
        if(!text) {
            buffer.append('_')
        } else {
            boolean useCamelCase = options.hash('camelCase', true)
            boolean useUnderscore = options.hash('underscore', false)
            boolean startNewPart = false
            boolean first = true
            StringBuilder sb = new StringBuilder()
            def chars = text.toString().trim().chars
            chars.each {ch ->
                if(Character.isJavaIdentifierPart(ch)) {
                    if((first && !Character.isJavaIdentifierStart(ch)) || (startNewPart && useUnderscore)) sb << '_'
                    sb << (first ? ch.toLowerCase() : (startNewPart ? (useCamelCase ? ch.toUpperCase() : ch.toLowerCase()) : ch))
                    startNewPart = false
                    first= false
                } else {
                    startNewPart = !first
                }
            }
            buffer.append(sb.toString())
        }
        buffer
    }),

    JAVA_COMMENT("javaComment", { String commentFile, Options options ->
        Options.Buffer buffer = options.buffer()

        String comment = null
        try {
            comment = options.handlebars.loader.sourceAt(commentFile).content(StandardCharsets.UTF_8)
        } catch (Exception e) {
            log.debug "Comment file $commentFile not found (${options.handlebars.loader.resolve(commentFile)})"
        }
        if(comment) {
            buffer.append('/*\n')
            comment.eachLine { line ->
                buffer.append(' * ').append(line).append('\n')
            }
            buffer.append(' */\n')
        }
        buffer
    }),

    AS_URL_PATH("asUrlPath", { path, options ->
        Options.Buffer buffer = options.buffer()
        path = from(path)
        if(path) {
            def uri = new URI(null, null, path, null, null)
            boolean ascii = options.hash("ascii", false)
            def encoded = ascii ? uri.toASCIIString() : uri.toString()
            buffer.append(encoded)
        }
        buffer
    }),

    AS_URL_QUERY("asUrlQuery", { query, options ->
        Options.Buffer buffer = options.buffer()
        query = from(query)
        if(query) {
            def encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
            buffer.append(encoded)
        }
        buffer
    }),


    static {
        Options.metaClass.prm = { index -> from(delegate.params[index]) }
    }

    final String helperName
    private final Closure<CharSequence> closure;

    Helpers(String helperName, @ClosureParams(value=FromAbstractTypeMethods, options="com.github.jknack.handlebars.Helper") Closure<CharSequence> closure) {
        this.closure = closure
        this.helperName = helperName
    }

    @Override
    CharSequence apply(context, Options options) throws IOException {
        closure.call(context, options)
    }

    static void register(Handlebars handlebars) {
        Helpers.values().each { helper -> handlebars.registerHelper(helper.helperName, helper)}
    }

    private static from(param) {
        if(param instanceof Options.NativeBuffer) {
            param.writer.toString()
        } else {
            param
        }
    }

    private static boolean asBoolean(value) {
        if(value instanceof CharSequence) {
            if('true'.equalsIgnoreCase(value.toString())) return true
            if('false'.equalsIgnoreCase(value.toString())) return false
        }
        !Handlebars.Utils.isEmpty(value)
    }

    private static boolean isNumber(value) {
        if(value == null) return false
        if(value instanceof Number) return true
        try {
            Double.parseDouble(value.toString())
            return true
        } catch (e) {
            return false
        }
    }

    private static Number asNumber(value) {
        if(value == null) return null
        if(value instanceof Number) return value
        try {
            return new Long(value.toString())
        } catch (e) {
            return new Double(value.toString())
        }
    }
}
