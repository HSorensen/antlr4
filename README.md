# Fork of ANTLR v4

**ANTLR** (ANother Tool for Language Recognition) is a powerful parser generator for reading, processing, executing, or translating structured text or binary files. It's widely used to build languages, tools, and frameworks. From a grammar, ANTLR generates a parser that can build parse trees and also generates a listener interface (or visitor) that makes it easy to respond to the recognition of phrases of interest.

## About this fork

This fork is an attempt to solve inclusion of source into the scanning step using ANTLR natively. Currently Java is the only supported target.

## Major Differences from mainline ANTLR

- Include Source Support
- Serializable ParseTree
- ParseTree.getText( ) with separator

### Include Source Support: Background

Analyzing grammars for programming languages like **C**, **PL/I** or **COBOL** requires support for handling inclusion of source into the scanning stream. C uses `#include`, PL/I uses `%INCLUDE` and COBOL uses `COPY`.
ANTLR does not support pulling additional source into its lexer scan directly; but it can be achieved by expanding the source code before invoking ANTLR. This fork is adding new grammar lexer actions to address the inclusion of source code as part of the lexer grammar itself. 

The current version is very much WIP and focuses on Java target platform. If you use this fork, expect things to break until the interface has solidified. 

Once the inclusion of files work properly it is possible to add support for macro expansion as well by using same concept.

#### ~~version 1: a hasty hack~~
* ~~[rfc v1 issue #305](https://github.com/antlr/antlr4/issues/305)~~
* ~~[rfc v1 pull request #306](https://github.com/antlr/antlr4/pull/306)~~

#### version 2: much improved simplified interface
* [rfc v2 pull request #979](https://github.com/antlr/antlr4/pull/979)

### Include Source Support: How does it work

A new lexer grammar action has been defined:

* performIncludeSourceFile

Sample lexer grammar:

```antlr
    lexer grammar L;
    I : 'A'..'Z' ;
    CP: '#' ('0'|'1') { performIncludeSourceFile(getText()); skip(); };
    WS: (' '|'\n') -> skip ;
```

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
        // Show filename where token originates from
        if (t instanceof CommonToken) { System.out.print(","+((CommonToken)t).getInputStream().getSourceName()); }
        System.out.println("");
    }
```

When dealing with locating files to include it is quite often necessary to search through multiple directories, or to add extensions to the string the lexer has recognized. To enable these kind of filename manipulations the lexer provides a method `setLexerScannerIncludeSource` that can be used to override the default behavior.

```java 
    lex.setLexerScannerIncludeSource(new IncludeScannerSource());
    public class IncludeScannerSource extends LexerScannerIncludeSourceImpl implements LexerScannerIncludeSource {
      @Override
      public CharStream embedSource(String fName, String substituteFrom, String substituteTo) {
        String fileName = "COBOL/COPYBOOKS/"+fName;
        return super.embedSource(fileName, substituteFrom, substituteTo);
      }
    }
```

### Include Source Support: Implementation considerations
Looking at the richness of the ANTLR implementation it can be daunting to try to locate a safe spot where the Lexer can be taught to read the next set of tokens from a new file, and once the new file is completely read, continue reading from the original file as if nothing had happened. Examining at how ANTLR reads the whole file into a buffer makes things a bit easier and it should reduce any side effects the IO system might have on a solution. Another consideration is to avoid interference with the decision making part of ANTLR. These considerations led to the `Lexer.nextToken()` method as I figured all the necessary decisions and actions needed on the current token most be completed and the Lexer is ready to receive next token. 

Create a new lexer grammar action `Lexer.performIncludeSourceFile(...)` that will capture the string the lexer has found and will set a flag that a request happened:
```java
    public void performIncludeSourceFile(String fileName)
    { 
        _hitInclude = true; // instruct scanner to prepare for switch of scan source
        _includeFileName = fileName;
    }
```


In `Lexer.nextToken()` check if `_hitInclude` is set and act accordingly
```java
    if (_hitInclude) {
        // store current lexer state, and open _includeFileName for reading.
        pushLexerScannerState( );
        _hitInclude=false;                  
    }
```                 

Also in `Lexer.nextToken()` check if `hitEOF` is set and if the `EOF` is from a stacked file or from the original input file:
```java
    if (_hitEOF) {
        // check if any input has been stacked
        if (!_lexerScannerStateStack.isEmpty()) {
            popLexerScannerState( );
            _hitEOF = false;
        } else {
            emitEOF();
            return _token;
        }
    }
```

After some experimenting only a small set of lexer attributes are needed to maintain the lexer state before and after the inclusion of the new file as implemented by `Lexer.pushLexerScannerState()` and `Lexer.popLexerScannerState()`:
```java
    CharStream input;
    Pair<TokenSource, CharStream> tokenFactorySourcePair;
    int line;
    int charPosInLine;
``` 


In `Lexer.pushLexerScannerState()` the current state of the relevant lexer attributes are pushed to a simple stack and the `CharStream` for new filename is returned by the `_lexerScannerIncludeSource.embedSource(...)` method:
```java
    public void pushLexerScannerState() {
        // store current lexer scanner state
        _lexerScannerStateStack.push(new LexerScannerIncludeStateStackItem(_input
                                                                         , _tokenFactorySourcePair
                                                                         , getInterpreter().getLine()
                                                                         , getInterpreter().getCharPositionInLine()));
        // open _includeFileName ...
        this._input = _lexerScannerIncludeSource.embedSource(_includeFileName,_includeSubstFrom,_includeSubstTo);
        this._tokenFactorySourcePair = new Pair<TokenSource, CharStream>(this, _input); 
        this._input.seek(0); // ensure position is set
        getInterpreter().reset();
    }
```


When `EOF` is met for the new file then `Lexer.popLexerScannerState()` will restore the lexer attributes from the stack:
```java
    public void popLexerScannerState() {
        LexerScannerIncludeStateStackItem stackItem=_lexerScannerStateStack.pop();
        // restore _input, _tokenFactorySourcePair, line and charPosInLine
        this._input=stackItem.getInput();
        this._tokenFactorySourcePair=stackItem.getTokenFactorySourcePair();
        getInterpreter().setLine(stackItem.getLine());
        getInterpreter().setCharPositionInLine(stackItem.getCharPosInLine());        
    }
```

There are of course other ways to implement the include feature into ANTLR proper. This version works very well for me and let me know if it does or does not work for you.

### Serializable Parse Tree

When looking into serializing the ParseTree and only 7 classes needed to implement the Serializable interface:
```java
public class RuleContext implements RuleNode, Serializable { }
public class TerminalNodeImpl implements TerminalNode, Serializable { }
public abstract class ATNSimulator implements Serializable { }
public abstract class Lexer extends Recognizer<Integer, LexerATNSimulator> implements TokenSource, Serializable { }
public class CommonTokenFactory implements TokenFactory<CommonToken>, Serializable { }
public class ANTLRInputStream implements CharStream, Serializable { }
public class IntegerList implements Serializable { }
```

With these changes it becomes straight forward to store the parse tree:
```java
        parser.setBuildParseTree(true);
        ParseTree tree = parser.program();
        OutputStream outputStream = new FileOutputStream("program.parsetree");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(tree);
        objectOutputStream.close();
```

and retrieving the parse tree
```java
        InputStream inputStream = new FileInputStream("program.parsetree");
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        ParseTree tree=(ParseTree) objectInputStream.readObject();
        objectInputStream.close();
```

Using the Apache Commons Compression routines can yield really good compression ratio of 90%.
Write compressed parse tree
```java
        parser.setBuildParseTree(true);
        ParseTree tree = parser.program();
        OutputStream xzOutputStream = new FileOutputStream("program.parsetree.xz");
        CompressorOutputStream xzOut = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.XZ, xzOutputStream);
        ObjectOutputStream xzObjectOutputStream = new ObjectOutputStream(xzOut);
        xzObjectOutputStream.writeObject(tree);
        xzObjectOutputStream.close();
```

and retrieve a compressed parse tree
```java
        InputStream inputStream = new FileInputStream("program.parsetree.xz");
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.XZ, inputStream);
        ObjectInputStream objectInputStream = new ObjectInputStream(input);
        ParseTree tree=(ParseTree) objectInputStream.readObject();
        objectInputStream.close();
```



Some background:
When running several static source code analysis using many different tree visitors and tree walkers, some of the analysis if done in foreground other in background.
For languages that supports inclusion of source files it is common to have literally 100's of files that all need to be parsed together, with the serialized parse-tree there is just one file to maintain. Alternatively to serializing the parse tree all these files has to be maintained together and recreate the parse tree every time it is needed.

This feature probably make most sense for the use case to support inclusion of source code see [ANTLR fork](https://github.com/HSorensen/antlr4/tree/lexerinclude) or [commit ddf9a331](https://github.com/HSorensen/antlr4/commit/ddf9a3311379870f01122fa1a850c329a5bdca34)

#### Sample serialization with large parse tree
When reusing a parse tree it can be significant faster to use the serialized parse tree instead of recreating the parse tree from the source code:
- Creating the parse tree: 31s
- Compressed parse tree (2.3mb) write 21s / read 2s
- Uncompressed parse tree (25.6mb) write 88s / read 22s

### ParseTree.getText( ) with separator
When using a tree visitor to analyze a parse tree it can be convenient to use getText( ) to see the original source code within a given context. The standard getText( ) method simply returns all the tokens as text but for complicated statements that is not useful.

Standard way:

`IFCODE='05'OR'06'OR'07'MOVE'Y'TOBULKELSEMOVE' 'TOBULK`

Using getText(" ") with space as separator:

`IF CODE = '05' OR '06' OR '07' MOVE 'Y' TO BULK ELSE MOVE ' ' TO BULK`

See [commit 21e68192](https://github.com/HSorensen/antlr4/commit/21e681921f6edc8ed0c0fa9e349bfa3e5fbb1b1e) for implementation details.

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
