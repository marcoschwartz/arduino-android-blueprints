// Required libraries
#include <SPI.h>
#include <PN532_SPI.h>
#include <PN532.h>
#include <NfcAdapter.h>

// NFC instances
PN532_SPI pn532spi(SPI, 10);
NfcAdapter nfc = NfcAdapter(pn532spi);

void setup(void) {
  
  // Start Serial
  Serial.begin(9600);
  
  // Start NFC chip
  nfc.begin();
  Serial.println("NFC shield started");
}

void loop(void) {
  
  // Start scan 
  Serial.println("\nScan a NFC tag\n");
  if (nfc.tagPresent())
  {
    NfcTag tag = nfc.read();
    tag.print();
  }
  delay(5000);
}
