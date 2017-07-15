/**
  * Created by Pim Nijdam on 15/07/2017.
  */
import javax.sound.midi._

object Main {
  def main(args: Array[String]) {
    println("Hello world")
    val info= MidiSystem.getMidiDeviceInfo
    println("Found " + info.length + " devices")

    val devices = info.map {MidiSystem.getMidiDevice}
    val inCandidates = devices.filter(_.getMaxReceivers != 0 )
    val outCandidates = devices.filter(_.getMaxTransmitters != 0 )
    println("Out Candidates: ")
    outCandidates.foreach((device) =>println(device.getDeviceInfo.getName))
    println("In Candidates: ")
    inCandidates.foreach((device) =>println(device.getDeviceInfo.getName))

    // Play middle C on default receiver
    val msg : ShortMessage = new ShortMessage()
    msg.setMessage(ShortMessage.NOTE_ON, 0, 60, 93)

    val default = MidiSystem.getReceiver
    println("Default output: " + MidiSystem.getReceiver)
    default.send(msg, -1)
  }
}
