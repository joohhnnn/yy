package com.txznet.dvr;

interface IDVRInterface {

   boolean getFrameAtTime(String inFile , String outFile,  long time);

}