[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/beryx/handlebars-java-helpers/blob/master/LICENSE)
[![Build Status](https://img.shields.io/travis/beryx/handlebars-java-helpers/master.svg?label=Build)](https://travis-ci.org/beryx/handlebars-java-helpers)
## Handlebars.java Helpers ##

This library provides various helpers for adding logic to [Handlebars.java](https://github.com/jknack/handlebars.java).
Most of them are basic helpers that can be used as subexpressions in the built-in block helpers of Handlebars.java.
This allows writing the templates in a fluent way.

**Example**

Given the following YAML model:
```yaml
birthYear: 1997
```
and the following template:

```hbs
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
```
The resulting text will be:
```
Your fifteenth anniversary was in a leap year!
```



Please read the **[documentation](http://handlebars-java-helpers.beryx.org)** before using the handlebars-java-helpers library.
