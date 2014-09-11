// Robot test via aREST + Serial

// Libraries
#include <SPI.h>
#include "Adafruit_BLE_UART.h"
#include <aREST.h>

// BLE pins
#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2     // This should be an interrupt pin, on Uno thats #2 or #3
#define ADAFRUITBLE_RST 9

// Motor pins
int speed_motor1 = 6;  
int speed_motor2 = 5;
int direction_motor1 = 7;
int direction_motor2 = 4;

// Sensor pins
int distance_sensor = A3;

// BLE instance
Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

// Create aREST instance
aREST rest = aREST();

// Variable to be exposed to the API
int distance;

void setup(void)
{  
  // Start Serial
  Serial.begin(115200);
  BTLEserial.begin();
  
  // Expose variables to REST API
  rest.variable("distance",&distance);
  
  // Expose functions
  rest.function("forward",forward);
  rest.function("backward",backward);
  rest.function("left",left);
  rest.function("right",right);
  rest.function("stop",stop);
  
  // Give name and ID to device
  rest.set_id("001");
  rest.set_name("mobile_robot");
}

aci_evt_opcode_t laststatus = ACI_EVT_DISCONNECTED;

void loop() {  
  
  // Measure distance
  distance = measure_distance(distance_sensor);
  
  // Tell the nRF8001 to do whatever it should be working on.
  BTLEserial.pollACI();
  
  // Ask what is our current status
  aci_evt_opcode_t status = BTLEserial.getState();
  // If the status changed....
  if (status != laststatus) {
    // print it out!
    if (status == ACI_EVT_DEVICE_STARTED) {
        Serial.println(F("* Advertising started"));
    }
    if (status == ACI_EVT_CONNECTED) {
        Serial.println(F("* Connected!"));
    }
    if (status == ACI_EVT_DISCONNECTED) {
        Serial.println(F("* Disconnected or advertising timed out"));
    }
    // OK set the last status change to this one
    laststatus = status;
  }
  
  // Handle REST calls
  if (status == ACI_EVT_CONNECTED) {
    rest.handle(BTLEserial);
  }
  
}

// Forward
int forward(String command) {

  send_motor_command(speed_motor1,direction_motor1,200,1);
  send_motor_command(speed_motor2,direction_motor2,200,1);
  return 1;
}

// Backward
int backward(String command) {
  
  send_motor_command(speed_motor1,direction_motor1,200,0);
  send_motor_command(speed_motor2,direction_motor2,200,0);
  return 1;
}

// Left
int left(String command) {
  
  send_motor_command(speed_motor1,direction_motor1,150,0);
  send_motor_command(speed_motor2,direction_motor2,150,1);
  return 1;
}

// Right
int right(String command) {
  
  send_motor_command(speed_motor1,direction_motor1,150,1);
  send_motor_command(speed_motor2,direction_motor2,150,0);
  return 1;
}

// Stop
int stop(String command) {
  
  send_motor_command(speed_motor1,direction_motor1,0,1);
  send_motor_command(speed_motor2,direction_motor2,0,1);
  return 1;
}

// Function to command a given motor of the robot
void send_motor_command(int speed_pin, int direction_pin, int pwm, boolean dir)
{
  analogWrite(speed_pin,pwm); // Set PWM control, 0 for stop, and 255 for maximum speed
  digitalWrite(direction_pin,dir);
}

// Measure distance from the ultrasonic sensor
int measure_distance(int pin){
  
  unsigned int Distance=0;
  unsigned long DistanceMeasured=pulseIn(pin,LOW);
  
  if(DistanceMeasured==50000){              // the reading is invalid.
      Serial.print("Invalid");    
   }
    else{
      Distance=DistanceMeasured/50;      // every 50us low level stands for 1cm
   }
   
  return Distance;
}
