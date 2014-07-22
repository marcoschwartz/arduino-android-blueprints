// Required libraries
#include <SPI.h>
#include "Adafruit_BLE_UART.h"
#include <aREST.h>
#include <Servo.h> 

// Pins
#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2     // This should be an interrupt pin, on Uno thats #2 or #3
#define ADAFRUITBLE_RST 9
 
// Create servo object
Servo myservo;

// Create aREST instance
aREST rest = aREST();

// Servo position
int pos = 0;   

// BLE instance
Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

void setup() 
{ 
   // Start Serial
  Serial.begin(115200);
  
  // Attaches the servo on pin 7 to the servo object 
  myservo.attach(7);  
  
  // Start BLE
  BTLEserial.begin();
  
  // Give name and ID to device
  rest.set_id("001");
  rest.set_name("servo_control"); 
  
  // Expose function to API
  rest.function("servo",servoControl);
} 
 
 
void loop() 
{ 
  // Tell the nRF8001 to do whatever it should be working on.
  BTLEserial.pollACI();
  
  // Ask what is our current status
  aci_evt_opcode_t status = BTLEserial.getState();
  
  // Handle REST calls
  if (status == ACI_EVT_CONNECTED) {
    rest.handle(BTLEserial);
  }
} 

// Control servo from REST API
int servoControl(String command) {
  
  // Get position from command
  int pos = command.toInt();
  Serial.println(pos);
                            
  myservo.write(pos);              
  
  return 1;
}
