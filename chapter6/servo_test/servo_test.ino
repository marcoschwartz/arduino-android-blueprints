// Required libraries
#include <Servo.h> 
 
// Create servo object
Servo myservo;

// Servo position
int pos = 0;   

void setup() 
{ 
  // Attaches the servo on pin 7 to the servo object 
  myservo.attach(7);  
} 
 
 
void loop() 
{ 
  // Goes from 0 degrees to 180 degrees 
  for(pos = 0; pos < 180; pos += 1)
  {                                  
    myservo.write(pos);              
    delay(15);                       
  } 
  
  // Goes from 180 degrees to 0 degrees 
  for(pos = 180; pos>=1; pos-=1)     
  {                                
    myservo.write(pos);              
    delay(15);                  
  } 
} 
