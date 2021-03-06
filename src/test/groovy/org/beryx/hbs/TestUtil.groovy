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
import com.github.jknack.handlebars.helper.StringHelpers
import com.github.jknack.handlebars.io.TemplateLoader
import com.github.jknack.handlebars.io.TemplateSource
import org.yaml.snakeyaml.Yaml

import java.nio.charset.Charset

trait TestUtil  {
    static class StringTemplateLoader implements TemplateLoader {
        Charset charset
        static {
            String.metaClass.stripAll = { ->
                delegate.stripIndent().trim().replaceAll('(?m)^\\s*$\\n', '')
            }
        }

        @Override TemplateSource sourceAt(String location) {
            new TemplateSource() {
                @Override String content(Charset charset) { location }
                @Override String filename() { "N/A" }
                @Override long lastModified() { 0 }
            }
        }
        @Override String resolve(String location) { location }
        @Override String getPrefix() { "" }
        @Override String getSuffix() { "" }
        @Override void setPrefix(String prefix) {}
        @Override void setSuffix(String prefix) {}
    }


    String merge(String template, String context) {
        Handlebars handlebars = new Handlebars()
                .with(EscapingStrategy.NOOP)
                .with(new StringTemplateLoader())
        StringHelpers.register(handlebars)
        Helpers.register(handlebars)

        Template tmpl = handlebars.compile(template)
        Yaml yaml = new Yaml()
        def ctx = yaml.load(context)
        tmpl.apply(ctx).stripAll()
    }
}
