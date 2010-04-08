sh-clojure
==========

This is a brush used to provide syntax highlighting for Clojure with the
popular [SyntaxHighlighter][sh] library.  It is intended as a drop-in
replacement of the brush developed by Travis Whitton.

  [sh]: http://alexgorbatchev.com/wiki/SyntaxHighlighter


Features
--------

The key features of this brush include highlighting:

* Special forms 
* all forms from `clojure.core` of Clojure 1.1
* Number, character, boolean, and nil values
* Strings and regular expressions
* Parentheses and other syntax elements such as reader macros for collections
  and type hinting
* Special highlighting for quoted and unquoted symbols


Installation
------------

In order to use this brush, you simply need to load the `shBrushClojure.js`
script along with the other SyntaxHighlighter files loaded by your web page.
For more information, see the [official installation instructions][sh-install].

  [sh-install]: http://alexgorbatchev.com/wiki/SyntaxHighlighter:Usage


Shortcomings
------------

Unfortunately, the brush is not perfect and does not support any of the following:

* Comment highlighting of `(comment …)` forms: I do not think this can be
  accomplished with SyntaxHighlighter.
* Highlighting of the `(.javaMethodOrField …)` special form
* Special highlighting of quoted and unquoted s-expressions


Contact
-------

I welcome any feedback or patches.  [The latest news about sh-clojure is
available from the sh-clojure page on Deep Blue Lambda.][blog-page]  [The
source for sh-clojure is available from GitHub][github].

  [blog-page]: http://www.deepbluelambda.org/programs/sh-clojure
  [github]: http://github.com/sattvik/sh-clojure

License
-------

Copyright © 2010 Sattvik Software &amp; Technology Resources, Ltd. Co.

This script is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either version 3 of the License, or (at your option) any
later version.

This script is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
details.

You should have received a copy of the GNU Lesser General Public License along
with this script.  If not, see <http://www.gnu.org/licenses/>.
