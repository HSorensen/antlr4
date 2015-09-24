# Fork of ANTLR v4

**ANTLR** (ANother Tool for Language Recognition) is a powerful parser generator for reading, processing, executing, or translating structured text or binary files. It's widely used to build languages, tools, and frameworks. From a grammar, ANTLR generates a parser that can build parse trees and also generates a listener interface (or visitor) that makes it easy to respond to the recognition of phrases of interest.

## About this fork

This fork is an attempt to solve inclusion of source into the scanning step using ANTLR natively.

Analyzing grammars for programming languages like **C**, **PL/I** or **COBOL** requires support for handling inclusion of source into the scanning stream. C uses `#include`, PL/I uses `%INCLUDE` and COBOL uses `COPY`.
ANTLR does not support pulling additional source into its lexer scan directly; but it can be achieved by expanding the source code 
before invoking ANTLR. This fork is adding new grammar lexer actions to address the inclusion of source code as part of the lexer grammar itself. 

The current version is very much WIP and focuses on Java target platform. If you use this fork, expect things to break until the interface has solidified. 

### Background
#### ~~version 1: a hasty hack~~
* ~~[rfc v1 issue #305](https://github.com/antlr/antlr4/issues/305)~~
* ~~[rfc v1 pull request #306](https://github.com/antlr/antlr4/pull/306)~~

#### version 2: much improved simplified interface
* [rfc v2 pull request #979](https://github.com/antlr/antlr4/pull/979)

### How does it work

A new lexer grammar action has been defined:

* performIncludeSourceFile

Sample lexer grammar:

10. `lexer grammar L;`
20. `I : 'A'..'Z' ;`
20. `CP: '#' ('0'|'1') { performIncludeSourceFile(getText()); skip(); };`
40. `WS: (' '|'\n') -> skip ;`

Sample source to scan `"A B C D #0 N O P"`, when `#0` is read the grammar action `performIncludeSourceFile` is invoked with the parameter `"#0"`. When `performIncludeSourceFile` is invoked it instructs the lexer to get the next set of tokens using the parameter as filename to be read.

Now imagine two files named `#0` and `#1` with content: `#0:"E F G #1 L M"` and `#1:"H I J K"`. 

Using ANTLR to retrieve all the tokens would give:

- [@0,0:0=`'A'`,<1>,1:0] <== offset zero from original file
- [@1,2:2=`'B'`,<1>,1:2] <== original file
- [@2,4:4=`'C'`,<1>,1:4] <== original file
- [@3,6:6=`'D'`,<1>,1:6] <== original file
- [@4,0:0=`'E'`,<1>,1:0] <== offset zero from file `#0`
- [@5,2:2=`'F'`,<1>,1:2] <== File `#0`
- [@6,4:4=`'G'`,<1>,1:4] <== File `#0`
- [@7,0:0=`'H'`,<1>,1:0] <== offset zero from file `#1`
- [@8,2:2=`'I'`,<1>,1:2] <== File `#1`
- [@9,4:4=`'J'`,<1>,1:4] <== File `#1`
- [@10,6:6=`'K'`,<1>,1:6] <== File `#1`
- [@11,9:9=`'L'`,<1>,1:9] <== File `#0`
- [@12,11:11=`'M'`,<1>,1:11] <== File `#0`
- [@13,11:11=`'N'`,<1>,1:11] <== original file
- [@14,13:13=`'O'`,<1>,1:13] <== original file
- [@15,15:15=`'P'`,<1>,1:15] <== original file
- [@16,16:15=`'<EOF>'`,<-1>,1:16] <== original file `DONE`

Sample program:
```java
L lex = new L(input);
CommonTokenStream tokens = new CommonTokenStream(lex);
tokens.fill();
for (Token t : tokens.getTokens()) {
    System.out.print(t);
    if (t instanceof CommonToken) { System.out.print(","+((CommonToken)t).getInputStream().getSourceName()); }
    System.out.println("");
}
```


## Authors and major contributors

* [Terence Parr](http://www.cs.usfca.edu/~parrt/), parrt@cs.usfca.edu
ANTLR project lead and supreme dictator for life
[University of San Francisco](http://www.usfca.edu/)
* [Sam Harwell](http://tunnelvisionlabs.com/) (Tool co-author, Java and C# target)
* Eric Vergnaud (Javascript, Python2, Python3 targets and significant work on C# target)
* Henrik Sorensen (Freedom Hacker leading the rebellion adding include support to ANTLR)

## Useful information

* [Release notes](https://github.com/antlr/antlr4/releases)
* [Getting started with v4](https://theantlrguy.atlassian.net/wiki/display/ANTLR4/Getting+Started+with+ANTLR+v4)
* [Official site](http://www.antlr.org/)
* [Documentation](https://theantlrguy.atlassian.net/wiki/display/ANTLR4/ANTLR+4+Documentation)
* [FAQ](https://theantlrguy.atlassian.net/wiki/display/ANTLR4/ANTLR+v4+FAQ)
* [API](http://www.antlr.org/api/Java/index.html)
* [ANTLR v3](http://www.antlr3.org/)
* [v3 to v4 Migration guide, differences](https://theantlrguy.atlassian.net/wiki/pages/viewpage.action?pageId=1900596)

You might also find the following wiki pages useful, particularly if you want to mess around with the various target languages.
 
* [How to build ANTLR itself](https://github.com/antlr/antlr4/wiki/How-to-build-ANTLR-itself)
* [How we create and deploy an ANTLR release](https://github.com/antlr/antlr4/wiki/Cutting-an-ANTLR-4-release)

## The Definitive ANTLR 4 Reference

Programmers run into parsing problems all the time. Whether it’s a data format like JSON, a network protocol like SMTP, a server configuration file for Apache, a PostScript/PDF file, or a simple spreadsheet macro language—ANTLR v4 and this book will demystify the process. ANTLR v4 has been rewritten from scratch to make it easier than ever to build parsers and the language applications built on top. This completely rewritten new edition of the bestselling Definitive ANTLR Reference shows you how to take advantage of these new features.

You can buy the book [The Definitive ANTLR 4 Reference](http://amzn.com/1934356999) at amazon or an [electronic version at the publisher's site](https://pragprog.com/book/tpantlr2/the-definitive-antlr-4-reference).

You will find the [Book source code](http://pragprog.com/titles/tpantlr2/source_code) useful.


## Additional grammars
[This repository](https://github.com/antlr/grammars-v4) is a collection of grammars without actions where the
root directory name is the all-lowercase name of the language parsed
by the grammar. For example, java, cpp, csharp, c, etc...
