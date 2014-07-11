// Robot test via aREST + Serial

// Libraries
#include <aREST.h>

// Motor pins
int speed_motor1 = 6;  
int speed_motor2 = 5;
int direction_motor1 = 7;
int direction_motor2 = 4;

// Sensor pins
int distance_sensor = A3;

// Create aREST instance
aREST rest = aREST();

// Variable to be exposed to the API
int distance;

void setup(void)
{  
  // Start Serial
  Serial.begin(115200);
  
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

void loop() {  
  
  // Measure distance
  distance = measure_distance(distance_sensor);
  
  // Handle REST calls
  rest.handle(Serial);  
  
}

// Forward
int forward(String command) {
  
  send_motor_command(speed_motor1,direction_motor1,100,1);
  send_motor_command(speed_motor2,direction_motor2,100,1);
  return 1;
}

// Backward
int backward(String command) {
  
  send_motor_command(speed_motor1,direction_motor1,100,0);
  send_motor_command(speed_motor2,direction_motor2,100,0);
  return 1;
}

// Left
int left(String command) {
  
  send_motor_command(speed_motor1,direction_motor1,75,0);
  send_motor_command(speed_motor2,direction_motor2,75,1);
  return 1;
}

// Right
int right(String command) {
  
  send_motor_command(speed_motor1,direction_motor1,75,1);
  send_motor_command(speed_motor2,direction_motor2,75,0);
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
