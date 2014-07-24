// Required libraries
#include <SPI.h>
#include <PN532_SPI.h>
#include <PN532.h>
#include <NfcAdapter.h>

// NFC instances
PN532_SPI pn532spi(SPI, 10);
NfcAdapter nfc = NfcAdapter(pn532spi);

// Relay pin
const int relay_pin = 8;

// Keys
String open_key = "oWnHV6uXre";
String close_key = "C19HNuqNU4";

void setup(void) {
  
  // Start Serial
  Serial.begin(9600);
  
  // Start NFC chip
  Serial.println("NFC shield started");
  nfc.begin();
  
  // Declare relay pin as output
  pinMode(relay_pin,OUTPUT);
  
}

void loop(void) {
  
  // Start scan 
  Serial.println("\nScan a NFC tag\n");
  if (nfc.tagPresent())
  {
    NfcTag tag = nfc.read();
    tag.print();
    String tag_content = tag.getUidString();
    if (tag_content == open_key) {
      digitalWrite(relay_pin,HIGH);
    }
    if (tag_content == close_key) {
      digitalWrite(relay_pin,LOW);
    }
  }
  delay(5000);
}
