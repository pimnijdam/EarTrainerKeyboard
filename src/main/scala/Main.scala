/**
  * Created by Pim Nijdam on 15/07/2017.
  */
import java.util.concurrent.LinkedBlockingQueue
import javax.sound.midi.{MidiMessage, MidiSystem, MidiUnavailableException, Receiver, ShortMessage, Transmitter}

import scala.collection.mutable.Queue

class Note(name: String)

sealed trait Key
case class MajorKey(keyNote:Note) extends Key
case class MinorKey(keyNote:Note) extends Key

/** Convert midi messages to notes */
class MidiSequencer extends Receiver {
  val sequence: Queue[Note] = new Queue[Note]
  val bseq = new LinkedBlockingQueue[Note]()
  private def noteName (key: Int): String = {
    val name: List[String] = List("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val note = key % 12
    val octave = key / 12 - 2

    return s"${name(note)}${octave}"
  }

  override def close(): Unit = {}

  override def send(message: MidiMessage, timeStamp: Long): Unit = {
    val status = message.getStatus
    val key = message.getMessage()(1)
    val velocity = message.getMessage()(2)
    val noteOn = status == 0x90 && velocity > 0
    if (noteOn) {
      val note = new Note(s"${noteName(key)}")
      println(s"played: ${key}")
      sequence += note
      bseq.put(note)
    }
  }
}

class Teacher (receiver: Receiver, transmitter: Transmitter) {
  val sequencer: MidiSequencer = new MidiSequencer
  transmitter.setReceiver(sequencer)

  for (note <- sequencer.sequence) {
    // this doesn't work. Use some proper consumer/produces scala way
    println(note)

    // Play middle C
    val msg : ShortMessage = new ShortMessage()
    msg.setMessage(ShortMessage.NOTE_ON, 0, 60, 50)
    receiver.send(msg, -1)
  }
}

object Main extends App {
  override def main(args: Array[String]) {
    // Use default transmitter and receiver
    try {
      val receiver: Receiver = MidiSystem.getReceiver
      val transmitter: Transmitter = MidiSystem.getTransmitter
      val Teacher = new Teacher(receiver, transmitter)
    } catch {
      case _: MidiUnavailableException => {println("No midi keyboard found")}
      }
  }
}
