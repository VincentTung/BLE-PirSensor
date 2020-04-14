#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <M5StickC.h>
#include <Wire.h>

using namespace std;

#define DEVICE_NAME         "PIR_SENSOR"
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define DEVICE_BLE_ADDRESS  "D8:A0:1D:55:66:3A"
#define TIME_INTERVAL       500


char str[25];
BLECharacteristic *pCharacteristic;

/**
 * 
 * init ble
 * 
 * */
void initBLE(){
  BLEDevice::init(DEVICE_NAME);
  BLEServer *pServer = BLEDevice::createServer();
  BLEService *pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_NOTIFY
                                       );
  //set default value                                     
  pCharacteristic->setValue("0");  
  //start service                              
  pService->start();

  BLEAdvertising *pAdvertising = pServer->getAdvertising();
  pAdvertising->start();



}

void setup() {
  M5.begin();
  M5.Lcd.setRotation(3);
  M5.Lcd.fillScreen(BLACK);
  M5.Lcd.println("PIR TEST");

  pinMode(36,INPUT_PULLUP);
  initBLE();
}
void closeLED(){
  pinMode(10, OUTPUT);
  digitalWrite(10, HIGH);
}

void openLED(){
  pinMode(10, OUTPUT);
  digitalWrite(10, LOW);
}
void loop() {
  M5.Lcd.setCursor(60, 20, 4);
  int signal = digitalRead(36);
  M5.Lcd.println(signal);

  if(signal ==1){ 
    itoa(signal, str, 10);
    pCharacteristic->setValue(str);
    pCharacteristic->notify();
  }
  delay(TIME_INTERVAL);
}
