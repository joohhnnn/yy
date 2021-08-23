package com.clw.recognition;

interface IRecognitionFloat
{
    boolean getState();
	void setState(boolean state);

    boolean getWakeupFromAsrState();
    void setWakeupFromAsrState(boolean state);
}
