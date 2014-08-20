// Code for the wireless smart lamp project

// Import required libraries
#include <Adafruit_CC3000.h>
#include <SPI.h>
#include <aREST.h>
#include <avr/wdt.h>

// Relay state
const int relay_pin = 8;

// Define measurement variables
float amplitude_current;
float effective_value;
float effective_voltage = 230.; // Set voltage to 230V (Europe) or 110V (US)
float zero_sensor;
float effective_power;

// These are the pins for the CC3000 chip if you are using a breakout board
#define ADAFRUIT_CC3000_IRQ   3
#define ADAFRUIT_CC3000_VBAT  5
#define ADAFRUIT_CC3000_CS    10

// Create CC3000 instance
Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT,
                                         SPI_CLOCK_DIV2);
// Create aREST instance
aREST rest = aREST();

// Your WiFi SSID and password                                         
#define WLAN_SSID       "yourWiFiSSID"
#define WLAN_PASS       "yourWiFiPassword"
#define WLAN_SECURITY   WLAN_SEC_WPA2

// The port to listen for incoming TCP connections 
#define LISTEN_PORT           80

// Server instance
Adafruit_CC3000_Server restServer(LISTEN_PORT);

// Variables to be exposed to the API
int power;

void setup(void)
{  
  // Start Serial
  Serial.begin(115200);
 
  // Init variables and expose them to REST API
  rest.variable("power",&power);
  
  // Set relay pin to output
  pinMode(relay_pin,OUTPUT);
  
  // Calibrate sensor with null current
  zero_sensor = getSensorValue(A0);
   
  // Give name and ID to device
  rest.set_id("001");
  rest.set_name("smart_lamp");
  
  // Set up CC3000 and get connected to the wireless network.
  if (!cc3000.begin())
  {
    while(1);
  }
  
  if (!cc3000.connectToAP(WLAN_SSID, WLAN_PASS, WLAN_SECURITY)) {
    while(1);
  }
  
  while (!cc3000.checkDHCP())
  {
    delay(100);
  }
  
  // Display connection details
  displayConnectionDetails();
   
  // Start server
  restServer.begin();
  Serial.println(F("Listening for connections..."));
  
  // Enable watchdog
  wdt_enable(WDTO_4S);
}

void loop() {
  
  // Perform power measurement
  float sensor_value = getSensorValue(A0);
  wdt_reset();
    
  // Convert to current
  amplitude_current = (float)(sensor_value-zero_sensor)/1024*5/185*1000000;
  effective_value = amplitude_current/1.414;
  effective_power = abs(effective_value*effective_voltage/1000.);
  power = (int) effective_power;
  
  // Handle REST calls
  Adafruit_CC3000_ClientRef client = restServer.available();
  rest.handle(client);
  
  // Check connection
  if(!cc3000.checkConnected()){while(1){}}
  wdt_reset();
  
}

// Function to display connection details
bool displayConnectionDetails(void)
{
  uint32_t ipAddress, netmask, gateway, dhcpserv, dnsserv;
  
  if(!cc3000.getIPAddress(&ipAddress, &netmask, &gateway, &dhcpserv, &dnsserv))
  {
    Serial.println(F("Unable to retrieve the IP Address!\r\n"));
    return false;
  }
  else
  {
    Serial.print(F("\nIP Addr: ")); cc3000.printIPdotsRev(ipAddress);
    Serial.print(F("\nNetmask: ")); cc3000.printIPdotsRev(netmask);
    Serial.print(F("\nGateway: ")); cc3000.printIPdotsRev(gateway);
    Serial.print(F("\nDHCPsrv: ")); cc3000.printIPdotsRev(dhcpserv);
    Serial.print(F("\nDNSserv: ")); cc3000.printIPdotsRev(dnsserv);
    Serial.println();
    return true;
  }
}

// Get the reading from the current sensor
float getSensorValue(int pin)
{
  int sensorValue;
  float avgSensor = 0;
  int nb_measurements = 100;
  for (uint8_t i = 0; i < nb_measurements; i++) {
    sensorValue = analogRead(pin);
    avgSensor = avgSensor + float(sensorValue);
  }	  
  avgSensor = avgSensor/float(nb_measurements);
  return avgSensor;
}
