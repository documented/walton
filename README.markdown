# walton

walton is a tiny little utility that gives you a nice way of finding
examples of how to use functions in clojure.  Additionally, it may
help you traverse a dangerous dungeon full of snakes.

## Usage

    (use 'walton.core)

"Hmm... I wonder how I use..."

    user> (walton "concat")

    => (concat :a :b :c)

"Oh.  Right..."

You can also run it from the command line after you've run lein
uberjar:

    $ java -jar walton-standalone.jar "zipmap"
    
Optionally, you can make it into sweet, sweet HTML.  (Syntax
hilighting and other goodies coming soon?!)

    $ java -jar walton-standalone.jar "zipmap" "html"

## Installation

First off, you're going to need the .log files!  You can download them
[here](http://www.devinwalters.com/clojure-logs.tar.bz2 "here").  Once
you've got the log files, unzip them into the project root directory
into a directory called `logs`.

Use leiningen in the project root directory to build a jar.

    $ lein deps
    $ lein uberjar

Move the resulting .jar onto your classpath.

*OR*

Edit your project.clj to include the relevant entry for walton, which
you can find on clojars.org. 

    $ lein deps
    $ lein repl

    user> (use 'walton.core)
    user> (walton "zipmap")

## License

<pre>
           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                   Version 2, December 2004

Copyright (C) 2004 Sam Hocevar
 14 rue de Plaisance, 75014 Paris, France
Everyone is permitted to copy and distribute verbatim or modified
copies of this license document, and changing it is allowed as long
as the name is changed.

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

 0. You just DO WHAT THE FUCK YOU WANT TO.
</pre>
