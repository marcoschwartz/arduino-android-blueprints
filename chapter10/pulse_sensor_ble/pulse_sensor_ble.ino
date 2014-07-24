// Libraries
#include <SPI.h>
#include "Adafruit_BLE_UART.h"
#include <aREST.h>

// Sensor and pins variables
int pulsePin = 0;
int blinkPin = 13;
int fadePin = 5;
int fadeRate = 0;

// Pins
#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2     // This should be an interrupt pin, on Uno thats #2 or #3
#define ADAFRUITBLE_RST 9

// Create aREST instance
aREST rest = aREST();

// BLE instance
Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

// Pulse rate variable
volatile int BPM;    

// Raw signal
volatile int Signal;

// Interval between beats
volatile int IBI = 600;

// Becomes true when the pulse is high
volatile boolean Pulse = false;

// Becomes true when Arduino finds a pulse
volatile boolean QS = false;

// Variable to be returned by Bluetooth LE
int bpm = 0;

void setup(){
 
  // Start Serial
  Serial.begin(115200);
  
  // Start BLE
  BTLEserial.begin();
  
  // Give name and ID to device
  rest.set_id("1");
  rest.set_name("pulse_sensor"); 
  
  // Expose variables to API
  rest.variable("bpm",&bpm);
  
  // Sets up to read Pulse Sensor signal every 2mS
  interruptSetup();
}

void loop(){
  
  // Assign BPM variable
  bpm = BPM;
     
  // Tell the nRF8001 to do whatever it should be working on.
  BTLEserial.pollACI();
  
  // Ask what is our current status
  aci_evt_opcode_t status = BTLEserial.getState();
  
  // Handle REST calls
  if (status == ACI_EVT_CONNECTED) {
    rest.handle(BTLEserial);
  }
}




