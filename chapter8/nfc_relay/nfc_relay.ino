// NFC relay controller
// This code is based on the code from Don Coleman

// Include libraries
#include "SPI.h"
#include "PN532_SPI.h"
#include "snep.h"
#include "NdefMessage.h"

// Relay pin
#define RELAY_PIN 8

// Code for the On states
#define RELAY_ON "oWnHV6uXre"

// Create NFC object instance
PN532_SPI pn532spi(SPI, 10);
SNEP nfc(pn532spi);

// NFC buffer
uint8_t ndefBuf[128];

void setup() {
  
    // Start Serial communications
    Serial.begin(9600);
    Serial.println("NFC Peer to Peer Light Switch");
    
    // Declare relay pin as output
    pinMode(RELAY_PIN, OUTPUT);
}

void loop(void) {
  
  // Wait for NFC message
  Serial.println("Waiting for message from Peer");
  int msgSize = nfc.read(ndefBuf, sizeof(ndefBuf));
  if (msgSize > 0) {
    
    // Read message
    NdefMessage message  = NdefMessage(ndefBuf, msgSize);
      
    // Make sure there is at least one NDEF Record
    if (message.getRecordCount() > 0) {

      NdefRecord record = message.getRecord(0);
      Serial.println("Got first record");
  
      // Check the TNF and Record Type
      if (record.getTnf() == TNF_MIME_MEDIA && record.getType() == "application/com.arduinoandroid.arduinonfc") {
        Serial.println("Type is OK");
    
        // Get the bytes from the payload
        int payloadLength = record.getPayloadLength();
        byte payload[payloadLength];
        record.getPayload(payload);

        // Convert the payload to a String
        String payloadAsString = "";
        for (int c = 0; c < payloadLength; c++) {
          payloadAsString += (char)payload[c];
        }
        
        // Print out the data on the Serial monitor
        Serial.print("Payload is ");Serial.println(payloadAsString);
        
        // Modify the state of the light, based on the tag contents
        if (payloadAsString == RELAY_ON) {
            digitalWrite(RELAY_PIN, HIGH);
        } else {
            digitalWrite(RELAY_PIN, LOW);    
        }
      } else {
        Serial.print("Expecting TNF 'Mime Media' (0x02) with type 'application/com.arduinoandroid.arduinonfc' but found TNF ");
        Serial.print(record.getTnf(), HEX);
        Serial.print(" type ");
        Serial.println(record.getType());
      }
    }
  }
}

