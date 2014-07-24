// Sensor and pins variables
int pulsePin = 0;
int blinkPin = 13;
int fadePin = 5;
int fadeRate = 0;

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

void setup(){
  
  // Update pin that will blink to your heartbeat
  pinMode(blinkPin,OUTPUT);
  
  // Update pin that will fade to your heartbeat!
  pinMode(fadePin,OUTPUT);
  
  // Start Serial
  Serial.begin(115200);
  
  // Sets up to read Pulse Sensor signal every 2mS
  interruptSetup();
}

void loop(){
  
  // If heart beat is found
  if (QS == true) {
        
        // Set 'fadeRate' Variable to 255 to fade LED with pulse
        fadeRate = 255;
        
        // Print heart rate with a 'B' prefix        
        sendDataToProcessing('B',BPM);
        
        // Reset the Quantified Self flag for next time      
        QS = false;                       
     }
  
  // Fade LED
  ledFadeToBeat();
  
  // Wait 20 ms
  delay(20);
}


// LED fade
void ledFadeToBeat(){
    fadeRate -= 15;                         //  set LED fade value
    fadeRate = constrain(fadeRate,0,255);   //  keep LED fade value from going into negative numbers!
    analogWrite(fadePin,fadeRate);          //  fade LED
  }


// Print data
void sendDataToProcessing(char symbol, int data ){
    Serial.print(symbol);                // symbol prefix tells Processing what type of data is coming
    Serial.println(data);                // the data to send culminating in a carriage return
  }







