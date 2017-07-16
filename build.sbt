name := "EarTrainerKeyboard"

version := "0.1"
scalaVersion := "2.12.1"

// Needed to find midi devices. Running in the sbt process doesn't work for some reason
fork in run:= true
connectInput in run := true
