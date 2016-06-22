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
{{#if (or
        (and
            (compare (math fifteenYear '%' 4) '==' 0)
            (compare (math fifteenYear '%' 100) '!=' 0)
        )
        (compare (math fifteenYear '%' 400) '==' 0)
       )
}}
Your fifteenth anniversary was in a leap year!
{{/if}}
```
The resulting text will be:
```
Your fifteenth anniversary was in a leap year!
```



Please read the **[documentation](http://handlebars-java-helpers.beryx.org)** before using the handlebars-java-helpers library.
