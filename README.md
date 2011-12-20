agiledox
=============

agiledox generates a simple documentation from junit4-Tests.  
It is the _junit4-Version_ of [agiledox](http://agiledox.sourceforge.net/).  
The documentation-style is inspired by the spec-like tools (e.g. ruby-rspec). 

Installation
------------

Install clojure: (on OSX)

    brew install clj

Check it out:

    git clone git://github.com/jenshimmelreich/agiledox.git

Let the line in the shellscript agiledox point to agiledox.clj.  
Put agiledox in your path.

Usage
-----

Go into the root-directory of a java-project.   
Run agiledox

    agiledox

It would generate output like:
    
    CartAdjuster
    - should do nothing if cart and order have no items and there is no order
    - should correct the price of an entry
    - should correct the price of more than one entry
    - should save only the corrected items
    - should correct the quantity of an entry
    - should correct the quantity of more than one entry
    - should only save the corrected quantity
    - should throw an exception if an article number isnt in the order
    - should throw an exception if there are more items in the cart than in the order
    - should throw an exception if there are more items in the order than in the cart

