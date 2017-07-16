/**
  * Created by Pim Nijdam on 15/07/2017.
  */
import javax.sound.midi.{MidiMessage, MidiSystem, Receiver, ShortMessage, Transmitter}

class Checker extends Receiver {
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
    val noteOff = status == 0x80 || (status == 0x80 && velocity == 0)
    if (noteOn) {
      println(s"${noteName(key)} ")
    }
  }
}

object Main {
  def main(args: Array[String]) {
    // Use default transmitter and receiver
    val receiver: Receiver = MidiSystem.getReceiver
    val transmitter: Transmitter = MidiSystem.getTransmitter

    // Play middle C
    val msg : ShortMessage = new ShortMessage()
    msg.setMessage(ShortMessage.NOTE_ON, 0, 60, 50)

    receiver.send(msg, -1)

    transmitter.setReceiver(new Checker)
    //TODO: generate sequence from transmitter, provide a checker implementation, provide a melody generator
  }
}
