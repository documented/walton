# walton

walton is a little utility that gives you a nice way of finding
examples of how to use functions in clojure.  Additionally, it may
help you traverse a dangerous dungeon full of snakes.

## Usage

### From the Web

If you want to check out the result of Walton, you can mosey on over to the following examples:  [concat](http://getclojure.org:8080/examples/concat "concat"), [let](http://getclojure.org:8080/examples/let "let"), [3.14](http://getclojure.org:8080/examples/3.14 "3.14"), and so on.  Basically you can put anything in place of http://www.getclojure.org:8080/examples/`foo`, where foo is the function or keyword you'd like to investigate.

Note, click one of the examples and it will expand to reveal the result by running it in the clj-sandbox.

### From the REPL

    user> (use 'walton.core)

    user> (init-walton)
    => true

    user> (walton "concat")

    (concat [:a :b :c])
    => (:a :b :c)

    user> (concat ...)

### Browse docs

Point your browser at localhost:8080/examples/zipmap to see examples
for zipmap.  Click on an example and it will expand to show you the
result of that function.

## Building

Use leiningen in the project root directory to build a jar.

    $ lein deps
    $ lein uberjar

Move the resulting walton.jar onto your classpath if you'd like to `(use 'walton.core)` in your project.  Or add it to your `ns` macro:

    (ns myproject.core
      (:use walton.core))

## License

See `epl-v10.html` in the project's root directory for more information.
