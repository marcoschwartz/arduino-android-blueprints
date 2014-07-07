// A demo of the aREST library

// Libraries
#include <aREST.h>
#include "DHT.h"

// DHT sensor
#define DHTPIN 7
#define DHTTYPE DHT11

// Create aREST instance
aREST rest = aREST();

// DHT instance
DHT dht(DHTPIN, DHTTYPE);

// Variables to be exposed to the API
int temperature;
int humidity;

void setup(void)
{  
  // Start Serial
  Serial.begin(115200);
  
  // Expose variables to REST API
  rest.variable("temperature",&temperature);
  rest.variable("humidity",&humidity);
  
  // Give name and ID to device
  rest.set_id("001");
  rest.set_name("arduino_project");
  
  // Start temperature sensor
  dht.begin();
  
}

void loop() {  
  
  // Measure from DHT
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  
  temperature = (int)t;
  humidity = (int)h;
  
  // Handle REST calls
  rest.handle(Serial);
  
}
