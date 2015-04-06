scala-edf
=====

Scala parser for file formats EDF and EDF+ (http://www.edfplus.info).

Port of https://github.com/MIOB/EDF4J project.

License
-------

This project is licensed under the terms of the MIT license. See LICENSE file.

Usage
=====

The parser is available in the file EDFParser.scala

Example usage
-------------

    val is = new BufferedInputStream(new FileInputStream(new File(`pathToEdfFile`)))
    val result = EDFParser.parseEDF(is)

Example program
---------------

An example program is available in the file EDF.scala.

It parses selected EDF or EDF+ file and prints its result to the Console.
