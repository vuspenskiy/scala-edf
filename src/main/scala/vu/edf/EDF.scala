package vu.edf

import java.io.{File, FileInputStream}
import javax.swing.{JFileChooser, UIManager}
import vu._

/**
 * @author v.uspenskiy
 * @since 29/07/14 16:40
 *
 * Ported https://github.com/MIOB/EDF4J
 */
object EDF extends App {

  val maybeFile =
     if (args.length == 0) {
       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
       val fileChooser = new JFileChooser()
       if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
         Option(fileChooser.getSelectedFile)
       else
         None
     } else
       Some(new File(args(0)))

  for(file <- maybeFile) {
    using(new FileInputStream(file)) { is => EDFParser.parseEDF(is)} match {
      case Some(result) =>
        Console.println("Header: \n" + result.header.productIterator.map(_.toString).mkString(",\n") + "\n")

        Console.println("Channels: ")
        for ((channelHeader, channelData) <- result.header.channels zip result.signal.channelsData) {
          Console.println("Channel data: " + channelHeader.productIterator.map(_.toString).mkString(", "))
          Console.println("Values In Units: " + channelData.valuesInUnits.take(100).mkString(", ") +
            (if (channelData.valuesInUnits.size > 100) "..." else ""))
          Console.println
        }

        if (result.annotations.nonEmpty) {
          Console.println("Annotations: ")
          for (annotation <- result.annotations) {
            Console.println(s" - ${annotation.onSet} -- ${annotation.duration}: ${annotation.annotations.mkString(", ")}")
          }
        }

      case None =>
        Console.println("Couldn't parse EDF")
    }
  }
}
