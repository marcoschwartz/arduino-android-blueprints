// NFC Peer to Peer Light Controller
// Don Coleman

#include "SPI.h"
#include "PN532_SPI.h"
#include "snep.h"
#include "NdefMessage.h"

#define LIGHT_PIN 8
// maybe use more explicit text like "light on"?
#define LIGHT_ON "oWnHV6uXre"
#define LIGHT_OFF "C19HNuqNU4"

PN532_SPI pn532spi(SPI, 10);
SNEP nfc(pn532spi);
uint8_t ndefBuf[128];

void setup() {
    Serial.begin(9600);
    Serial.println("NFC Peer to Peer Light Switch");
    
    pinMode(LIGHT_PIN, OUTPUT);
}

void loop(void) {
  
  Serial.println("Waiting for message from Peer");
  int msgSize = nfc.read(ndefBuf, sizeof(ndefBuf));
  if (msgSize > 0) {
    
    NdefMessage message  = NdefMessage(ndefBuf, msgSize);
      
    // Make sure there is at least one NDEF Record
    if (message.getRecordCount() > 0) {

      NdefRecord record = message.getRecord(0);
      Serial.println("Got first record");
  
      // Check the TNF and Record Type
      if (record.getTnf() == TNF_MIME_MEDIA && record.getType() == "application/com.arduinoandroid.arduinonfc") {
        Serial.println("Type is OK");
    
        // get the bytes from the payload
        int payloadLength = record.getPayloadLength();
        byte payload[payloadLength];
        record.getPayload(payload);

        // convert the payload to a string
        String payloadAsString = "";
        for (int c = 0; c < payloadLength; c++) {
          payloadAsString += (char)payload[c];
        }
        
        Serial.print("Payload is ");Serial.println(payloadAsString);
        
        // Modify the state of the light, based on the tag contents
        if (payloadAsString == LIGHT_ON) {
            digitalWrite(LIGHT_PIN, HIGH);
        } else {
            digitalWrite(LIGHT_PIN, LOW);    
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

