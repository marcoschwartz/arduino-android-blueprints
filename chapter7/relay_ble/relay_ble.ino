// Control Arduino board from BLE

// Libraries
#include <SPI.h>
#include "Adafruit_BLE_UART.h"
#include <aREST.h>
#include "DHT.h"

// Pins
#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2     // This should be an interrupt pin, on Uno thats #2 or #3
#define ADAFRUITBLE_RST 9

// Relay pin
const int relay_pin = 7;

// Create aREST instance
aREST rest = aREST();

// BLE instance
Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

void setup(void)
{  
  // Start Serial
  Serial.begin(115200);

  // Start BLE
  BTLEserial.begin();
 
  // Give name and ID to device
  rest.set_id("001");
  rest.set_name("relay_control"); 
  
   // Init relay pin
  pinMode(relay_pin,OUTPUT);
}

void loop() {  
  
  // Tell the nRF8001 to do whatever it should be working on.
  BTLEserial.pollACI();
  
  // Ask what is our current status
  aci_evt_opcode_t status = BTLEserial.getState();
  
  // Handle REST calls
  if (status == ACI_EVT_CONNECTED) {
    rest.handle(BTLEserial);
  }
 }
