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

You:

    (use 'walton.core)
    
Then you, can either:

    (background-init-walton)

Note that `(background-init-walton)` will result in immediate search
functionality while the logs are parsed.

Or you can:

    (init-walton)
    
"Hmm... I wonder how I use..."

    user> (walton "concat") 
    
    (concat [:a :b :c])
    => (:a :b :c)
    
"Oh.  Right..."

You can also run it from the command line after you've run lein
uberjar.  This will generate a "zipmap.html" file in the `project-root/walton-docs/zipmap.html.`:

    $ java -jar walton-standalone.jar "zipmap"
    
Note that this will likely take awhile as walton needs to scale the depths of
quite a bit of irc logging.

## Building

Use leiningen in the project root directory to build a jar.

    $ lein deps
    $ lein uberjar

Move the resulting walton.jar onto your classpath if you'd like to
`(use 'walton.core)`.

Again, note that you will need a populated `logs/` directory, and a
`walton-docs/` directory for generated walton `.html` documents.

## License

See `epl-v10.html` in the project's root direction for more information.
