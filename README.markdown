# walton

walton is a tiny little utility that gives you a nice way of finding
examples of how to use functions in clojure.  Additionally, it may
help you traverse a dangerous dungeon full of snakes.

## Setup

First off, you're going to need the .log files!  You can download them
[here](http://www.devinwalters.com/clojure-logs.tar.bz2 "here").  Once
you've got the log files, unzip them into the project root directory
into a directory called `logs`.

## Usage

### From the REPL

    user> (use 'walton.core)
    
    user> (background-init-walton) ;; this will result in immediate search
    user> (init-walton) ;; this will not, and takes awhile to run

    user> (walton "concat")
    
    (concat [:a :b :c])
    => (:a :b :c)
    
    user> (concat ...)

### From the Command Line

You can also run it from the command line after you've run lein uberjar.  This will generate a "zipmap.html" file in the `project-root/walton-docs/zipmap.html.`:

    user@host(~)$ java -jar walton-standalone.jar "zipmap"

Note that similar to using `(walton-init)` this will likely take awhile as walton needs to scale the depths of quite a bit of irc logging.

## Building

Use leiningen in the project root directory to build a jar.

    $ lein deps
    $ lein uberjar

Move the resulting walton.jar onto your classpath if you'd like to `(use 'walton.core)` in your project.  Or add it to your `ns` macro: 

    (ns myproject.core
      (:use walton.core))

Again, note that you will need a populated `logs/` directory, and have created a `walton-docs/` directory for generated walton `.html` documents.

## License

See `epl-v10.html` in the project's root directory for more information.
