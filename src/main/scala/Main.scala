/**
  * Created by Pim Nijdam on 15/07/2017.
  */
import javax.sound.midi.{MidiMessage, MidiSystem, MidiUnavailableException, Receiver, ShortMessage, Transmitter}

import scala.concurrent.Channel
import scala.util.Random

class Note(val key: Int) {

  val name: String = noteName(key)
  def keyIn = (k: Int) => (key - k) % 12

  private def noteName (key: Int): String = {
    val name: List[String] = List("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val note = key % 12
    val octave = key / 12 - 2

    return s"${name(note)}${octave}"
  }
}

sealed trait Key
case class MajorKey(keyNote:Note) extends Key
case class MinorKey(keyNote:Note) extends Key

/** Convert midi messages to notes */
class MidiSequencer extends Receiver {
  val sequence = new Channel[Note]


  override def close(): Unit = {}

  override def send(message: MidiMessage, timeStamp: Long): Unit = {
    val status = message.getStatus
    val key = message.getMessage()(1)
    val velocity = message.getMessage()(2)
    val noteOn = status == 0x90 && velocity > 0
    if (noteOn) {
      val note = new Note(key)
      sequence .write(note)
    }
  }
}

//type Melody = Seq[(Int, Note)]


class Teacher (receiver: Receiver, transmitter: Transmitter) {
  val sequencer: MidiSequencer = new MidiSequencer
  transmitter.setReceiver(sequencer)

  def run: Unit = {
    val majorKey = Seq(0,2,4,5,7,9,11,12)

    val keyNote = new Note(65) //F3
    var purposeNote = keyNote
    var total = 0
    var correct = 0
    while (true) {

      Thread.sleep(1500)
      val msg : ShortMessage = new ShortMessage()
      msg.setMessage(ShortMessage.NOTE_ON, 0, purposeNote.key, 50)
      receiver.send(msg, -1)
      Thread.sleep(500)
      val msgOff : ShortMessage = new ShortMessage()
      msgOff.setMessage(ShortMessage.NOTE_OFF, 0, purposeNote.key, 50)
      receiver.send(msgOff, -1)

      var noteHit = false
      var attempts = 0
      while (!noteHit) {
        // Check note
        val note = sequencer.sequence.read
        println(note.name)
        noteHit = note.key == purposeNote.key
        attempts += 1
      }
      if (attempts == 1) correct += 1
      total += 1
      println(s"${correct}/${total} ~ ${correct*100/total}%")

      purposeNote = new Note(keyNote.key + Random.shuffle(majorKey).head)
    }
  }
}

object Main extends App {
  override def main(args: Array[String]) {
    // Use default transmitter and receiver
    try {
      val receiver: Receiver = MidiSystem.getReceiver
      val transmitter: Transmitter = MidiSystem.getTransmitter
      val teacher = new Teacher(receiver, transmitter)
      teacher.run
    } catch {
      case _: MidiUnavailableException => {println("No midi keyboard found")}
      }
  }
}
